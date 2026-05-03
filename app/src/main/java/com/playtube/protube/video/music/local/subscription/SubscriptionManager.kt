package com.playtube.protube.video.music.local.subscription

import android.content.Context
import com.playtube.protube.video.music.NewPipeDatabase
import com.playtube.protube.video.music.database.feed.model.FeedGroupEntity
import com.playtube.protube.video.music.database.stream.model.StreamEntity
import com.playtube.protube.video.music.database.subscription.NotificationMode
import com.playtube.protube.video.music.database.subscription.SubscriptionDAO
import com.playtube.protube.video.music.database.subscription.SubscriptionEntity
import com.playtube.protube.video.music.local.feed.FeedDatabaseManager
import com.playtube.protube.video.music.local.feed.service.FeedUpdateInfo
import com.playtube.protube.video.music.util.ExtractorHelper
import com.playtube.protube.video.music.util.image.ImageStrategy
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.schabi.newpipe.extractor.channel.ChannelInfo
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class SubscriptionManager(context: Context) {
    private val database = NewPipeDatabase.getInstance(context)
    private val subscriptionTable = database.subscriptionDAO()
    private val feedDatabaseManager = FeedDatabaseManager(context)

    fun subscriptionTable(): SubscriptionDAO = subscriptionTable
    fun subscriptions() = subscriptionTable.getAll()

    fun getSubscriptions(
        currentGroupId: Long = FeedGroupEntity.Companion.GROUP_ALL_ID,
        filterQuery: String = "",
        showOnlyUngrouped: Boolean = false
    ): Flowable<List<SubscriptionEntity>> {
        return when {
            filterQuery.isNotEmpty() -> {
                return if (showOnlyUngrouped) {
                    subscriptionTable.getSubscriptionsOnlyUngroupedFiltered(
                        currentGroupId,
                        filterQuery
                    )
                } else {
                    subscriptionTable.getSubscriptionsFiltered(filterQuery)
                }
            }

            showOnlyUngrouped -> subscriptionTable.getSubscriptionsOnlyUngrouped(currentGroupId)

            else -> subscriptionTable.getAll()
        }
    }

    fun upsertAll(infoList: List<Pair<ChannelInfo, ChannelTabInfo>>) {
        val listEntities = infoList.map { SubscriptionEntity.Companion.from(it.first) }
        subscriptionTable.upsertAll(listEntities)

        database.runInTransaction {
            infoList.forEachIndexed { index, info ->
                val streams = info.second.relatedItems.filterIsInstance<StreamInfoItem>()
                feedDatabaseManager.upsertAll(listEntities[index].uid, streams)
            }
        }
    }

    fun updateChannelInfo(info: ChannelInfo): Completable = subscriptionTable.getSubscription(info.serviceId, info.url)
        .flatMapCompletable {
            Completable.fromRunnable {
                it.apply {
                    name = info.name
                    avatarUrl = ImageStrategy.imageListToDbUrl(info.avatars)
                    description = info.description
                    subscriberCount = info.subscriberCount
                }
                subscriptionTable.update(it)
            }
        }

    fun updateNotificationMode(serviceId: Int, url: String, @NotificationMode mode: Int): Completable {
        return subscriptionTable().getSubscription(serviceId, url)
            .flatMapCompletable { entity: SubscriptionEntity ->
                Completable.fromAction {
                    entity.notificationMode = mode
                    subscriptionTable().update(entity)
                }.apply {
                    if (mode != NotificationMode.Companion.DISABLED) {
                        // notifications have just been enabled, mark all streams as "old"
                        andThen(rememberAllStreams(entity))
                    }
                }
            }
    }

    fun updateFromInfo(info: FeedUpdateInfo) {
        val subscriptionEntity = subscriptionTable.getSubscription(info.uid)

        subscriptionEntity.name = info.name

        // some services do not provide an avatar URL
        info.avatarUrl?.let { subscriptionEntity.avatarUrl = it }

        // these two fields are null if the feed info was fetched using the fast feed method
        info.description?.let { subscriptionEntity.description = it }
        info.subscriberCount?.let { subscriptionEntity.subscriberCount = it }

        subscriptionTable.update(subscriptionEntity)
    }

    fun deleteSubscription(serviceId: Int, url: String): Completable {
        return Completable.fromCallable { subscriptionTable.deleteSubscription(serviceId, url) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun insertSubscription(subscriptionEntity: SubscriptionEntity) {
        subscriptionTable.insert(subscriptionEntity)
    }

    fun deleteSubscription(subscriptionEntity: SubscriptionEntity) {
        subscriptionTable.delete(subscriptionEntity)
    }

    /**
     * Fetches the list of videos for the provided channel and saves them in the database, so that
     * they will be considered as "old"/"already seen" streams and the user will never be notified
     * about any one of them.
     */
    private fun rememberAllStreams(subscription: SubscriptionEntity): Completable {
        return ExtractorHelper.getChannelInfo(subscription.serviceId, subscription.url, false)
            .flatMap { info ->
                ExtractorHelper.getChannelTab(subscription.serviceId, info.tabs.first(), false)
            }
            .map { channel -> channel.relatedItems.filterIsInstance<StreamInfoItem>().map { stream ->
                StreamEntity(
                    stream
                )
            } }
            .flatMapCompletable { entities ->
                Completable.fromAction {
                    database.streamDAO().upsertAll(entities)
                }
            }.onErrorComplete()
    }
}
