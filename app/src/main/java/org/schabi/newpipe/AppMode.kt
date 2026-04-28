package org.schabi.newpipe

object AppMode {
    @JvmField
    var USE_EXTRACTOR: Boolean = true

    private const val CDN_BASE = "https://cdn.jsdelivr.net/gh/Jivan-TheCoder/My-New-Tube@dev/data"
    private const val RAW_BASE = "https://raw.githubusercontent.com/Jivan-TheCoder/My-New-Tube/dev/data"
    private const val YOUTUBE_GAMING_URL = "https://www.youtube.com/gaming"
    private const val YOUTUBE_MUSIC_URL = "https://www.youtube.com/feed/music"
    private const val YOUTUBE_HOME_URL = "https://www.youtube.com"

    @JvmStatic
    fun getPrimaryKioskJsonUrl(kioskId: String): String {
        return "$CDN_BASE/${getKioskJsonFileName(kioskId)}"
    }

    @JvmStatic
    fun getBackupKioskJsonUrl(kioskId: String): String {
        return "$RAW_BASE/${getKioskJsonFileName(kioskId)}"
    }

    @JvmStatic
    fun getKioskOpenUrl(kioskId: String): String {
        return when (kioskId) {
            "trending_gaming" -> YOUTUBE_GAMING_URL
            "trending_music" -> YOUTUBE_MUSIC_URL
            else -> YOUTUBE_HOME_URL
        }
    }

    @JvmStatic
    fun getKioskJsonFileName(kioskId: String): String {
        return when (kioskId) {
            "Trending" -> "Trending.json"

            "live" -> "Live.json"

            // Current remote setup serves category-wise content from one file.
            "trending_gaming" -> "Trending.json"

            "trending_music" -> "Trending.json"

            "trending_movies_and_shows" -> "TrendingMoviesAndShows.json"

            "trending_podcasts_episodes" -> "TrendingPodcastsEpisodes.json"

            else -> kioskId.replace(Regex("[^A-Za-z0-9_-]"), "_") + ".json"
        }
    }
}
