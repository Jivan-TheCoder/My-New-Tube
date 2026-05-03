package com.playtube.protube.video.music.database.remotekiosk.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.playtube.protube.video.music.util.image.ImageStrategy
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamType

@Entity(
    tableName = RemoteKioskVideoEntity.TABLE_NAME,
    primaryKeys = [RemoteKioskVideoEntity.KIOSK_ID, RemoteKioskVideoEntity.POSITION_INDEX],
    indices = [Index(value = [RemoteKioskVideoEntity.KIOSK_ID])]
)
data class RemoteKioskVideoEntity(
    @ColumnInfo(name = KIOSK_ID)
    val kioskId: String,
    @ColumnInfo(name = POSITION_INDEX)
    val positionIndex: Int,
    @ColumnInfo(name = SERVICE_ID)
    val serviceId: Int,
    @ColumnInfo(name = VIDEO_ID)
    val videoId: String,
    @ColumnInfo(name = URL)
    val url: String,
    @ColumnInfo(name = TITLE)
    val title: String,
    @ColumnInfo(name = UPLOADER)
    val uploader: String,
    @ColumnInfo(name = THUMBNAIL_URL)
    val thumbnailUrl: String?,
    @ColumnInfo(name = VIEW_COUNT)
    val viewCount: Long?,
    @ColumnInfo(name = DURATION_SECONDS)
    val durationSeconds: Long?
) {
    fun toStreamInfoItem(): StreamInfoItem {
        val item = StreamInfoItem(serviceId, url, title, StreamType.VIDEO_STREAM)
        item.uploaderName = uploader
        item.duration = durationSeconds ?: 0L
        if (viewCount != null) {
            item.viewCount = viewCount
        }
        item.thumbnails = ImageStrategy.dbUrlToImageList(thumbnailUrl)
        return item
    }

    companion object {
        const val TABLE_NAME = "remote_kiosk_videos"
        const val KIOSK_ID = "kiosk_id"
        const val POSITION_INDEX = "position_index"
        const val SERVICE_ID = "service_id"
        const val VIDEO_ID = "video_id"
        const val URL = "url"
        const val TITLE = "title"
        const val UPLOADER = "uploader"
        const val THUMBNAIL_URL = "thumbnail_url"
        const val VIEW_COUNT = "view_count"
        const val DURATION_SECONDS = "duration_seconds"
    }
}
