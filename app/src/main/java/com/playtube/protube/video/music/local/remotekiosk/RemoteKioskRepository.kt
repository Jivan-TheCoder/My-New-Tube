package com.playtube.protube.video.music.local.remotekiosk

import android.content.Context
import android.net.Uri
import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.playtube.protube.video.music.AppMode
import com.playtube.protube.video.music.DownloaderImpl
import com.playtube.protube.video.music.NewPipeDatabase
import com.playtube.protube.video.music.database.remotekiosk.model.RemoteKioskVideoEntity
import com.playtube.protube.video.music.fragments.list.kiosk.RemoteKioskInfo
import io.reactivex.rxjava3.core.Single
import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3.Request
import org.schabi.newpipe.extractor.ServiceList.YouTube

object RemoteKioskRepository {
    @JvmStatic
    fun loadKioskInfo(
        context: Context,
        kioskId: String,
        kioskName: String
    ): Single<RemoteKioskInfo> = Single.fromCallable {
        val dao = NewPipeDatabase.getInstance(context).remoteKioskVideoDAO()
        val primaryUrl = AppMode.getPrimaryKioskJsonUrl(kioskId)
        val backupUrl = AppMode.getBackupKioskJsonUrl(kioskId)

        val primaryItems = runCatching {
            parseRemoteData(kioskId, fetchJson(primaryUrl))
        }.getOrDefault(emptyList())

        val backupItems = runCatching {
            parseRemoteData(kioskId, fetchJson(backupUrl))
        }.getOrDefault(emptyList())

        // Prefer backup when available: raw GitHub tends to reflect latest commits sooner
        // than CDN mirrors, while still falling back safely when backup is unavailable.
        val fetchedItems = when {
            backupItems.isNotEmpty() -> backupItems
            primaryItems.isNotEmpty() -> primaryItems
            else -> emptyList()
        }

        if (fetchedItems.isNotEmpty()) {
            dao.replaceForKiosk(kioskId, fetchedItems)
        }

        val cachedItems = dao.getByKioskId(kioskId)
        if (cachedItems.isEmpty()) {
            throw IOException("No remote kiosk data available for $kioskId")
        }

        val infoItems = cachedItems.map(RemoteKioskVideoEntity::toStreamInfoItem)
        RemoteKioskInfo(
            YouTube.getServiceId(),
            kioskId,
            AppMode.getKioskOpenUrl(kioskId),
            kioskName,
            infoItems
        )
    }

    @Throws(IOException::class)
    private fun fetchJson(url: String): String {
        val freshUrl = addCacheBuster(url)
        val request = Request.Builder()
            .url(freshUrl)
            .get()
            .header("Cache-Control", "no-cache")
            .header("Pragma", "no-cache")
            .build()
        DownloaderImpl.getInstance().client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code} for $freshUrl")
            }
            val body = response.body.string()
            return body.takeIf { it.isNotBlank() }
                ?: throw IOException("Empty response for $freshUrl")
        }
    }

    private fun addCacheBuster(url: String): String {
        val minuteBucket = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis())
        val separator = if (url.contains("?")) "&" else "?"
        return "$url${separator}t=$minuteBucket"
    }

    private fun parseRemoteData(kioskId: String, rawJson: String): List<RemoteKioskVideoEntity> {
        val anyJson = JsonParser.any().from(rawJson)
        val items: JsonArray = extractItemsForKiosk(kioskId, anyJson)

        val mapped = mutableListOf<RemoteKioskVideoEntity>()
        for ((index, rawItem) in items.withIndex()) {
            val entity = parseItem(kioskId, index, rawItem)
            if (entity != null) {
                mapped.add(entity)
            }
        }
        return mapped
    }

    private fun extractItemsForKiosk(kioskId: String, anyJson: Any?): JsonArray {
        return when (anyJson) {
            is JsonArray -> anyJson

            is JsonObject -> {
                // Generic direct formats.
                extractItemsFromSectionValue(anyJson.get("videos"))
                    ?: extractItemsFromSectionValue(anyJson.get("items"))
                    ?: extractItemsFromSectionValue(anyJson.get("data"))
                    ?: extractItemsFromSectionValue(anyJson.get(kioskId))
                    // Category object formats, e.g. { "Music": { "data": [...] } }.
                    ?: sectionCandidates(kioskId).firstNotNullOfOrNull { key ->
                        extractItemsFromSectionValue(anyJson.get(key))
                    }
                    // Safe final fallback across commonly used category keys.
                    ?: fallbackCategoryKeys().firstNotNullOfOrNull { key ->
                        extractItemsFromSectionValue(anyJson.get(key))
                    }
                    ?: JsonArray()
            }

            else -> JsonArray()
        }
    }

    private fun extractItemsFromSectionValue(value: Any?): JsonArray? {
        return when (value) {
            is JsonArray -> value

            is JsonObject -> {
                value.getArray("data")
                    ?: value.getArray("items")
                    ?: value.getArray("videos")
            }

            else -> null
        }
    }

    private fun sectionCandidates(kioskId: String): List<String> {
        return when (kioskId) {
            "trending_music" -> listOf("Music", "music", "trending_music")
            "trending_gaming" -> listOf("Gaming", "Games", "gaming", "trending_gaming")
            "trending_movies_and_shows" -> listOf("Movies", "movies", "trending_movies")
            "trending_podcasts_episodes" -> listOf("Podcasts", "podcasts", "trending_podcasts")
            else -> listOf(kioskId)
        }
    }

    private fun fallbackCategoryKeys(): List<String> {
        return listOf(
            "Music", "music",
            "Gaming", "Games", "gaming",
            "Movies", "movies",
            "Podcasts", "podcasts"
        )
    }

    private fun parseItem(
        kioskId: String,
        index: Int,
        rawItem: Any?
    ): RemoteKioskVideoEntity? {
        return when (rawItem) {
            is String -> {
                val videoId = normalizeVideoId(rawItem)
                if (videoId.isBlank()) return null
                buildEntity(
                    kioskId = kioskId,
                    index = index,
                    videoId = videoId,
                    url = buildWatchUrl(videoId),
                    title = videoId,
                    uploader = "",
                    thumbnailUrl = buildDefaultThumbnail(videoId),
                    viewCount = null,
                    durationSeconds = null
                )
            }

            is JsonObject -> {
                val idFromFields = firstNonBlank(
                    rawItem.getString("videoId"),
                    rawItem.getString("id"),
                    extractVideoIdFromUrl(rawItem.getString("url")),
                    extractVideoIdFromUrl(rawItem.getString("watchUrl")),
                    extractVideoIdFromUrl(rawItem.getString("redirect_link")),
                    extractVideoIdFromUrl(rawItem.getString("redirectLink")),
                    extractVideoIdFromUrl(rawItem.getString("link"))
                )
                val videoId = normalizeVideoId(idFromFields)
                if (videoId.isBlank()) return null

                val resolvedUrl = firstNonBlank(
                    rawItem.getString("url"),
                    rawItem.getString("watchUrl"),
                    rawItem.getString("redirect_link"),
                    rawItem.getString("redirectLink"),
                    rawItem.getString("link"),
                    buildWatchUrl(videoId)
                )

                val title = firstNonBlank(
                    rawItem.getString("title"),
                    rawItem.getString("name"),
                    videoId
                )

                val uploader = firstNonBlank(
                    rawItem.getString("uploader"),
                    rawItem.getString("channel_name"),
                    rawItem.getString("channelName"),
                    rawItem.getString("channelTitle"),
                    ""
                )

                val thumbnail = firstNonBlank(
                    rawItem.getString("thumbnailUrl"),
                    rawItem.getString("thumbnail"),
                    rawItem.getString("icon_url"),
                    rawItem.getString("iconUrl"),
                    buildDefaultThumbnail(videoId)
                )

                val viewCount = parseLongSafely(rawItem.get("viewCount"))
                    ?: parseLongSafely(rawItem.get("views"))
                val durationSeconds = parseDurationSeconds(rawItem)

                buildEntity(
                    kioskId = kioskId,
                    index = index,
                    videoId = videoId,
                    url = resolvedUrl,
                    title = title,
                    uploader = uploader,
                    thumbnailUrl = thumbnail,
                    viewCount = viewCount,
                    durationSeconds = durationSeconds
                )
            }

            else -> null
        }
    }

    private fun buildEntity(
        kioskId: String,
        index: Int,
        videoId: String,
        url: String,
        title: String,
        uploader: String,
        thumbnailUrl: String?,
        viewCount: Long?,
        durationSeconds: Long?
    ): RemoteKioskVideoEntity {
        return RemoteKioskVideoEntity(
            kioskId = kioskId,
            positionIndex = index,
            serviceId = YouTube.getServiceId(),
            videoId = videoId,
            url = if (url.startsWith("http")) url else buildWatchUrl(videoId),
            title = title,
            uploader = uploader,
            thumbnailUrl = thumbnailUrl,
            viewCount = viewCount,
            durationSeconds = durationSeconds
        )
    }

    private fun parseDurationSeconds(item: JsonObject): Long? {
        val numeric = parseLongSafely(item.get("durationSeconds"))
            ?: parseLongSafely(item.get("duration"))
        if (numeric != null) return numeric

        val durationText = firstNonBlank(
            item.getString("durationText"),
            item.getString("duration"),
            item.getString("lengthText")
        )
        if (durationText.isBlank()) return null

        val parts = durationText.split(":")
            .mapNotNull { it.trim().toLongOrNull() }
        if (parts.isEmpty()) return null
        return when (parts.size) {
            3 -> parts[0] * 3600 + parts[1] * 60 + parts[2]
            2 -> parts[0] * 60 + parts[1]
            else -> parts[0]
        }
    }

    private fun parseLongSafely(any: Any?): Long? {
        return when (any) {
            null -> null
            is Number -> any.toLong()
            is String -> parseViewCountString(any)
            else -> null
        }
    }

    private fun parseViewCountString(raw: String): Long? {
        val cleaned = raw.trim().lowercase()
        if (cleaned.isBlank()) return null

        val compact = cleaned.replace(",", "").replace(" ", "")
        val match = Regex("""([0-9]+(?:\.[0-9]+)?)""").find(compact) ?: return null
        val numeric = match.groupValues[1].toDoubleOrNull() ?: return null

        val multiplier = when {
            compact.contains("cr") || compact.contains("crore") -> 10_000_000.0
            compact.contains("m") || compact.contains("million") -> 1_000_000.0
            compact.contains("k") || compact.contains("thousand") -> 1_000.0
            compact.contains("b") || compact.contains("bn") || compact.contains("billion") ->
                1_000_000_000.0
            compact.contains("lakh") || compact.contains("lac") -> 100_000.0
            else -> 1.0
        }

        return (numeric * multiplier).toLong()
    }

    private fun normalizeVideoId(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        if (raw.contains("http")) {
            return extractVideoIdFromUrl(raw) ?: ""
        }
        return raw.trim()
    }

    private fun extractVideoIdFromUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        val uri = Uri.parse(url)
        uri.getQueryParameter("v")?.takeIf { it.isNotBlank() }?.let { return it }

        val last = uri.lastPathSegment?.trim().orEmpty()
        if (last.isNotBlank() && !last.equals("watch", true)) {
            return last
        }
        return null
    }

    private fun buildWatchUrl(videoId: String): String {
        return "https://www.youtube.com/watch?v=$videoId"
    }

    private fun buildDefaultThumbnail(videoId: String): String {
        return "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
    }

    private fun firstNonBlank(vararg values: String?): String {
        return values.firstOrNull { !it.isNullOrBlank() }?.trim().orEmpty()
    }
}
