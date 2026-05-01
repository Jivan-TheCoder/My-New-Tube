package org.schabi.newpipe

import com.google.firebase.remoteconfig.FirebaseRemoteConfig

object AppMode {
    @JvmField
    var USE_EXTRACTOR: Boolean = true

    private val DEFAULT_CDN_BASE = SecureConfig.getDefaultCdnBase()
    private val DEFAULT_RAW_BASE = SecureConfig.getDefaultRawBase()
    private var remotePrimaryBase = DEFAULT_CDN_BASE
    private var remoteBackupBase = DEFAULT_RAW_BASE

    private var fileTrending = "Trending.json"
    private var fileLive = "Live.json"
    private var fileTrendingGaming = "Trending.json"
    private var fileTrendingMusic = "Trending.json"
    private var fileTrendingMovies = "TrendingMoviesAndShows.json"
    private var fileTrendingPodcasts = "TrendingPodcastsEpisodes.json"

    private const val YOUTUBE_GAMING_URL = "https://www.youtube.com/gaming"
    private const val YOUTUBE_MUSIC_URL = "https://www.youtube.com/feed/music"
    private const val YOUTUBE_HOME_URL = "https://www.youtube.com"

    @JvmStatic
    fun getPrimaryKioskJsonUrl(kioskId: String): String {
        return "$remotePrimaryBase/${getKioskJsonFileName(kioskId)}"
    }

    @JvmStatic
    fun getBackupKioskJsonUrl(kioskId: String): String {
        return "$remoteBackupBase/${getKioskJsonFileName(kioskId)}"
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
            "Trending" -> fileTrending

            "live" -> fileLive

            // Current remote setup serves category-wise content from one file.
            "trending_gaming" -> fileTrendingGaming

            "trending_music" -> fileTrendingMusic

            "trending_movies_and_shows" -> fileTrendingMovies

            "trending_podcasts_episodes" -> fileTrendingPodcasts

            else -> kioskId.replace(Regex("[^A-Za-z0-9_-]"), "_") + ".json"
        }
    }

    @JvmStatic
    @JvmOverloads
    fun applyRemoteConfig(remoteConfig: FirebaseRemoteConfig, source: String = "Unknown") {
        val useExtractor = remoteConfig.getBoolean("remote_use_extractor")
        USE_EXTRACTOR = useExtractor

        val rcMainLink = remoteConfig.getString("main_link").trim()
        val rcBackupLink = remoteConfig.getString("backup_link").trim()

        remotePrimaryBase = if (rcMainLink.isNotBlank()) rcMainLink else DEFAULT_CDN_BASE
        remoteBackupBase = if (rcBackupLink.isNotBlank()) rcBackupLink else DEFAULT_RAW_BASE

        val rcFileHome = remoteConfig.getString("file_home").trim()
        val rcFileLive = remoteConfig.getString("file_live").trim()
        val rcFileGame = remoteConfig.getString("file_game").trim()
        val rcFileMusic = remoteConfig.getString("file_music").trim()
        val rcFileMovie = remoteConfig.getString("file_movie").trim()
        val rcFilePodcast = remoteConfig.getString("file_podcast").trim()

        fileTrending = if (rcFileHome.isNotBlank()) rcFileHome else "Trending.json"
        fileLive = if (rcFileLive.isNotBlank()) rcFileLive else "Live.json"
        fileTrendingGaming = if (rcFileGame.isNotBlank()) rcFileGame else "Trending.json"
        fileTrendingMusic = if (rcFileMusic.isNotBlank()) rcFileMusic else "Trending.json"
        fileTrendingMovies = if (rcFileMovie.isNotBlank()) rcFileMovie else "TrendingMoviesAndShows.json"
        fileTrendingPodcasts = if (rcFilePodcast.isNotBlank()) rcFilePodcast else "TrendingPodcastsEpisodes.json"
    }
}
