package com.playtube.protube.video.music.database.playlist

import androidx.room.ColumnInfo
import com.playtube.protube.video.music.database.playlist.model.PlaylistEntity

/**
 * This class adds a field to [PlaylistMetadataEntry] that contains an integer representing
 * how many times a specific stream is already contained inside a local playlist. Used to be able
 * to grey out playlists which already contain the current stream in the playlist append dialog.
 * @see com.playtube.protube.video.music.local.playlist.LocalPlaylistManager.getPlaylistDuplicates
 */
data class PlaylistDuplicatesEntry(
    @ColumnInfo(name = PlaylistEntity.Companion.PLAYLIST_ID)
    override val uid: Long,

    @ColumnInfo(name = PlaylistEntity.Companion.PLAYLIST_THUMBNAIL_URL)
    override val thumbnailUrl: String?,

    @ColumnInfo(name = PlaylistEntity.Companion.PLAYLIST_THUMBNAIL_PERMANENT)
    override val isThumbnailPermanent: Boolean?,

    @ColumnInfo(name = PlaylistEntity.Companion.PLAYLIST_THUMBNAIL_STREAM_ID)
    override val thumbnailStreamId: Long?,

    @ColumnInfo(name = PlaylistEntity.Companion.PLAYLIST_DISPLAY_INDEX)
    override var displayIndex: Long?,

    @ColumnInfo(name = PLAYLIST_STREAM_COUNT)
    override val streamCount: Long,

    @ColumnInfo(name = PlaylistEntity.Companion.PLAYLIST_NAME)
    override val orderingName: String?,

    @ColumnInfo(name = PLAYLIST_TIMES_STREAM_IS_CONTAINED)
    val timesStreamIsContained: Long
) : PlaylistMetadataEntry(
    uid = uid,
    orderingName = orderingName,
    thumbnailUrl = thumbnailUrl,
    isThumbnailPermanent = isThumbnailPermanent,
    thumbnailStreamId = thumbnailStreamId,
    displayIndex = displayIndex,
    streamCount = streamCount
) {
    companion object {
        const val PLAYLIST_TIMES_STREAM_IS_CONTAINED: String = "timesStreamIsContained"
    }
}
