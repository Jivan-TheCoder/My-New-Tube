package com.playtube.protube.video.music.database.playlist

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.playtube.protube.video.music.database.LocalItem
import com.playtube.protube.video.music.database.playlist.model.PlaylistStreamEntity
import com.playtube.protube.video.music.database.stream.model.StreamEntity
import com.playtube.protube.video.music.database.stream.model.StreamStateEntity
import com.playtube.protube.video.music.util.image.ImageStrategy
import org.schabi.newpipe.extractor.stream.StreamInfoItem

data class PlaylistStreamEntry(
    @Embedded
    val streamEntity: StreamEntity,

    @ColumnInfo(name = StreamStateEntity.Companion.STREAM_PROGRESS_MILLIS, defaultValue = "0")
    val progressMillis: Long,

    @ColumnInfo(name = PlaylistStreamEntity.Companion.JOIN_STREAM_ID)
    val streamId: Long,

    @ColumnInfo(name = PlaylistStreamEntity.Companion.JOIN_INDEX)
    val joinIndex: Int
) : LocalItem {

    override val localItemType: LocalItem.LocalItemType
        get() = LocalItem.LocalItemType.PLAYLIST_STREAM_ITEM

    @Throws(IllegalArgumentException::class)
    fun toStreamInfoItem(): StreamInfoItem {
        return StreamInfoItem(
            streamEntity.serviceId,
            streamEntity.url,
            streamEntity.title,
            streamEntity.streamType
        ).apply {
            duration = streamEntity.duration
            uploaderName = streamEntity.uploader
            uploaderUrl = streamEntity.uploaderUrl
            thumbnails = ImageStrategy.dbUrlToImageList(streamEntity.thumbnailUrl)
        }
    }
}
