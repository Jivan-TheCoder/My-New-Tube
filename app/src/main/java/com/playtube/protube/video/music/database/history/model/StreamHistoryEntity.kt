package com.playtube.protube.video.music.database.history.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import com.playtube.protube.video.music.database.stream.model.StreamEntity
import java.time.OffsetDateTime

/**
 * @param streamUid the stream id this history item will refer to
 * @param accessDate the last time the stream was accessed
 * @param repeatCount the total number of views this stream received
 */
@Entity(
    tableName = StreamHistoryEntity.Companion.STREAM_HISTORY_TABLE,
    primaryKeys = [StreamHistoryEntity.Companion.JOIN_STREAM_ID, StreamHistoryEntity.Companion.STREAM_ACCESS_DATE],
    indices = [Index(value = [StreamHistoryEntity.Companion.JOIN_STREAM_ID])],
    foreignKeys = [
        ForeignKey(
            entity = StreamEntity::class,
            parentColumns = arrayOf(StreamEntity.Companion.STREAM_ID),
            childColumns = arrayOf(StreamHistoryEntity.Companion.JOIN_STREAM_ID),
            onDelete = CASCADE,
            onUpdate = CASCADE
        )
    ]
)
data class StreamHistoryEntity(
    @ColumnInfo(name = JOIN_STREAM_ID)
    val streamUid: Long,

    @ColumnInfo(name = STREAM_ACCESS_DATE)
    var accessDate: OffsetDateTime,

    @ColumnInfo(name = STREAM_REPEAT_COUNT)
    var repeatCount: Long
) {
    companion object {
        const val STREAM_HISTORY_TABLE: String = "stream_history"
        const val STREAM_ACCESS_DATE: String = "access_date"
        const val JOIN_STREAM_ID: String = "stream_id"
        const val STREAM_REPEAT_COUNT: String = "repeat_count"
    }
}
