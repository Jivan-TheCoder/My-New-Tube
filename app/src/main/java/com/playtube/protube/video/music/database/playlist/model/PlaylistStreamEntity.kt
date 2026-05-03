package com.playtube.protube.video.music.database.playlist.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import com.playtube.protube.video.music.database.LocalItem
import com.playtube.protube.video.music.database.stream.model.StreamEntity

@Entity(
    tableName = PlaylistStreamEntity.Companion.PLAYLIST_STREAM_JOIN_TABLE,
    primaryKeys = [PlaylistStreamEntity.Companion.JOIN_PLAYLIST_ID, PlaylistStreamEntity.Companion.JOIN_INDEX],
    indices = [
        Index(value = [PlaylistStreamEntity.Companion.JOIN_PLAYLIST_ID, PlaylistStreamEntity.Companion.JOIN_INDEX], unique = true),
        Index(value = [PlaylistStreamEntity.Companion.JOIN_STREAM_ID])
    ],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = arrayOf(PlaylistEntity.Companion.PLAYLIST_ID),
            childColumns = arrayOf(PlaylistStreamEntity.Companion.JOIN_PLAYLIST_ID),
            onDelete = CASCADE,
            onUpdate = CASCADE,
            deferred = true
        ),
        ForeignKey(
            entity = StreamEntity::class,
            parentColumns = arrayOf(StreamEntity.Companion.STREAM_ID),
            childColumns = arrayOf(PlaylistStreamEntity.Companion.JOIN_STREAM_ID),
            onDelete = CASCADE,
            onUpdate = CASCADE,
            deferred = true
        )
    ]
)
data class PlaylistStreamEntity(
    @ColumnInfo(name = JOIN_PLAYLIST_ID)
    val playlistUid: Long,

    @ColumnInfo(name = JOIN_STREAM_ID)
    val streamUid: Long,

    @ColumnInfo(name = JOIN_INDEX)
    val index: Int
) : LocalItem {

    override val localItemType: LocalItem.LocalItemType
        get() = LocalItem.LocalItemType.PLAYLIST_STREAM_ITEM

    companion object {
        const val PLAYLIST_STREAM_JOIN_TABLE = "playlist_stream_join"
        const val JOIN_PLAYLIST_ID = "playlist_id"
        const val JOIN_STREAM_ID = "stream_id"
        const val JOIN_INDEX = "join_index"
    }
}
