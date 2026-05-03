package com.playtube.protube.video.music.database.playlist.model

import android.text.TextUtils
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.playtube.protube.video.music.database.LocalItem
import com.playtube.protube.video.music.database.playlist.PlaylistLocalItem
import com.playtube.protube.video.music.util.NO_SERVICE_ID
import com.playtube.protube.video.music.util.image.ImageStrategy
import org.schabi.newpipe.extractor.playlist.PlaylistInfo

@Entity(
    tableName = PlaylistRemoteEntity.Companion.REMOTE_PLAYLIST_TABLE,
    indices = [
        Index(
            value = [PlaylistRemoteEntity.Companion.REMOTE_PLAYLIST_SERVICE_ID, PlaylistRemoteEntity.Companion.REMOTE_PLAYLIST_URL],
            unique = true
        )
    ]
)
data class PlaylistRemoteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = REMOTE_PLAYLIST_ID)
    override var uid: Long = 0,

    @ColumnInfo(name = REMOTE_PLAYLIST_SERVICE_ID)
    val serviceId: Int = NO_SERVICE_ID,

    @ColumnInfo(name = REMOTE_PLAYLIST_NAME)
    override val orderingName: String?,

    @ColumnInfo(name = REMOTE_PLAYLIST_URL)
    val url: String?,

    @ColumnInfo(name = REMOTE_PLAYLIST_THUMBNAIL_URL)
    override val thumbnailUrl: String?,

    @ColumnInfo(name = REMOTE_PLAYLIST_UPLOADER_NAME)
    val uploader: String?,

    @ColumnInfo(name = REMOTE_PLAYLIST_DISPLAY_INDEX)
    override var displayIndex: Long = -1, // Make sure the new item is on the top

    @ColumnInfo(name = REMOTE_PLAYLIST_STREAM_COUNT)
    val streamCount: Long?
) : PlaylistLocalItem {

    constructor(playlistInfo: PlaylistInfo) : this(
        serviceId = playlistInfo.serviceId,
        orderingName = playlistInfo.name,
        url = playlistInfo.url,
        thumbnailUrl = ImageStrategy.imageListToDbUrl(
            playlistInfo.thumbnails.ifEmpty { playlistInfo.uploaderAvatars }
        ),
        uploader = playlistInfo.uploaderName,
        streamCount = playlistInfo.streamCount
    )

    override val localItemType: LocalItem.LocalItemType
        get() = LocalItem.LocalItemType.PLAYLIST_REMOTE_ITEM

    /**
     * Returns boolean comparing the online playlist and the local copy.
     * (False if info changed such as playlist name or track count)
     */
    @Ignore
    fun isIdenticalTo(info: PlaylistInfo): Boolean {
        return this.serviceId == info.serviceId && this.streamCount == info.streamCount &&
            TextUtils.equals(this.orderingName, info.name) &&
            TextUtils.equals(this.url, info.url) &&
            // we want to update the local playlist data even when either the remote thumbnail
            // URL changes, or the preferred image quality setting is changed by the user
            TextUtils.equals(thumbnailUrl, ImageStrategy.imageListToDbUrl(info.thumbnails)) &&
            TextUtils.equals(this.uploader, info.uploaderName)
    }

    companion object {
        const val REMOTE_PLAYLIST_TABLE = "remote_playlists"
        const val REMOTE_PLAYLIST_ID = "uid"
        const val REMOTE_PLAYLIST_SERVICE_ID = "service_id"
        const val REMOTE_PLAYLIST_NAME = "name"
        const val REMOTE_PLAYLIST_URL = "url"
        const val REMOTE_PLAYLIST_THUMBNAIL_URL = "thumbnail_url"
        const val REMOTE_PLAYLIST_UPLOADER_NAME = "uploader"
        const val REMOTE_PLAYLIST_DISPLAY_INDEX = "display_index"
        const val REMOTE_PLAYLIST_STREAM_COUNT = "stream_count"
    }
}
