package com.playtube.protube.video.music.database.stream

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Ignore
import com.playtube.protube.video.music.database.LocalItem
import com.playtube.protube.video.music.database.history.model.StreamHistoryEntity
import com.playtube.protube.video.music.database.stream.model.StreamEntity
import com.playtube.protube.video.music.database.stream.model.StreamStateEntity
import com.playtube.protube.video.music.util.image.ImageStrategy
import java.time.OffsetDateTime
import org.schabi.newpipe.extractor.stream.StreamInfoItem

data class StreamStatisticsEntry(
    @Embedded
    val streamEntity: StreamEntity,

    @ColumnInfo(name = StreamStateEntity.Companion.STREAM_PROGRESS_MILLIS, defaultValue = "0")
    val progressMillis: Long,

    @ColumnInfo(name = StreamHistoryEntity.Companion.JOIN_STREAM_ID)
    val streamId: Long,

    @ColumnInfo(name = STREAM_LATEST_DATE)
    val latestAccessDate: OffsetDateTime,

    @ColumnInfo(name = STREAM_WATCH_COUNT)
    val watchCount: Long
) : LocalItem {

    override val localItemType: LocalItem.LocalItemType
        get() = LocalItem.LocalItemType.STATISTIC_STREAM_ITEM

    @Ignore
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

    companion object {
        const val STREAM_LATEST_DATE = "latestAccess"
        const val STREAM_WATCH_COUNT = "watchCount"
    }
}
