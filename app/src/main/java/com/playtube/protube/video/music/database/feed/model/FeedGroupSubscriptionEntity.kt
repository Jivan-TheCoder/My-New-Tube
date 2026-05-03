package com.playtube.protube.video.music.database.feed.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.playtube.protube.video.music.database.subscription.SubscriptionEntity

@Entity(
    tableName = FeedGroupSubscriptionEntity.Companion.FEED_GROUP_SUBSCRIPTION_TABLE,
    primaryKeys = [FeedGroupSubscriptionEntity.Companion.GROUP_ID, FeedGroupSubscriptionEntity.Companion.SUBSCRIPTION_ID],
    indices = [Index(FeedGroupSubscriptionEntity.Companion.SUBSCRIPTION_ID)],
    foreignKeys = [
        ForeignKey(
            entity = FeedGroupEntity::class,
            parentColumns = [FeedGroupEntity.ID],
            childColumns = [FeedGroupSubscriptionEntity.Companion.GROUP_ID],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
            deferred = true
        ),

        ForeignKey(
            entity = SubscriptionEntity::class,
            parentColumns = [SubscriptionEntity.Companion.SUBSCRIPTION_UID],
            childColumns = [FeedGroupSubscriptionEntity.Companion.SUBSCRIPTION_ID],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
            deferred = true
        )
    ]
)
data class FeedGroupSubscriptionEntity(
    @ColumnInfo(name = GROUP_ID)
    var feedGroupId: Long,

    @ColumnInfo(name = SUBSCRIPTION_ID)
    var subscriptionId: Long
) {

    companion object {
        const val FEED_GROUP_SUBSCRIPTION_TABLE = "feed_group_subscription_join"

        const val GROUP_ID = "group_id"
        const val SUBSCRIPTION_ID = "subscription_id"
    }
}
