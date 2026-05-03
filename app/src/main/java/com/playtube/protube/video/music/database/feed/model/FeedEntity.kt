package com.playtube.protube.video.music.database.feed.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.playtube.protube.video.music.database.stream.model.StreamEntity
import com.playtube.protube.video.music.database.subscription.SubscriptionEntity

@Entity(
    tableName = FeedEntity.Companion.FEED_TABLE,
    primaryKeys = [FeedEntity.Companion.STREAM_ID, FeedEntity.Companion.SUBSCRIPTION_ID],
    indices = [Index(FeedEntity.Companion.SUBSCRIPTION_ID)],
    foreignKeys = [
        ForeignKey(
            entity = StreamEntity::class,
            parentColumns = [StreamEntity.Companion.STREAM_ID],
            childColumns = [FeedEntity.Companion.STREAM_ID],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
            deferred = true
        ),
        ForeignKey(
            entity = SubscriptionEntity::class,
            parentColumns = [SubscriptionEntity.Companion.SUBSCRIPTION_UID],
            childColumns = [FeedEntity.Companion.SUBSCRIPTION_ID],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
            deferred = true
        )
    ]
)
data class FeedEntity(
    @ColumnInfo(name = STREAM_ID)
    var streamId: Long,

    @ColumnInfo(name = SUBSCRIPTION_ID)
    var subscriptionId: Long
) {

    companion object {
        const val FEED_TABLE = "feed"

        const val STREAM_ID = "stream_id"
        const val SUBSCRIPTION_ID = "subscription_id"
    }
}
