package com.playtube.protube.video.music.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.playtube.protube.video.music.database.feed.dao.FeedDAO
import com.playtube.protube.video.music.database.feed.dao.FeedGroupDAO
import com.playtube.protube.video.music.database.feed.model.FeedEntity
import com.playtube.protube.video.music.database.feed.model.FeedGroupEntity
import com.playtube.protube.video.music.database.feed.model.FeedGroupSubscriptionEntity
import com.playtube.protube.video.music.database.feed.model.FeedLastUpdatedEntity
import com.playtube.protube.video.music.database.history.dao.SearchHistoryDAO
import com.playtube.protube.video.music.database.history.dao.StreamHistoryDAO
import com.playtube.protube.video.music.database.history.model.SearchHistoryEntry
import com.playtube.protube.video.music.database.history.model.StreamHistoryEntity
import com.playtube.protube.video.music.database.playlist.dao.PlaylistDAO
import com.playtube.protube.video.music.database.playlist.dao.PlaylistRemoteDAO
import com.playtube.protube.video.music.database.playlist.dao.PlaylistStreamDAO
import com.playtube.protube.video.music.database.playlist.model.PlaylistEntity
import com.playtube.protube.video.music.database.playlist.model.PlaylistRemoteEntity
import com.playtube.protube.video.music.database.playlist.model.PlaylistStreamEntity
import com.playtube.protube.video.music.database.remotekiosk.dao.RemoteKioskVideoDAO
import com.playtube.protube.video.music.database.remotekiosk.model.RemoteKioskVideoEntity
import com.playtube.protube.video.music.database.stream.dao.StreamDAO
import com.playtube.protube.video.music.database.stream.dao.StreamStateDAO
import com.playtube.protube.video.music.database.stream.model.StreamEntity
import com.playtube.protube.video.music.database.stream.model.StreamStateEntity
import com.playtube.protube.video.music.database.subscription.SubscriptionDAO
import com.playtube.protube.video.music.database.subscription.SubscriptionEntity

@TypeConverters(Converters::class)
@Database(
    version = Migrations.DB_VER_10,
    entities = [
        SubscriptionEntity::class,
        SearchHistoryEntry::class,
        StreamEntity::class,
        StreamHistoryEntity::class,
        StreamStateEntity::class,
        PlaylistEntity::class,
        PlaylistStreamEntity::class,
        PlaylistRemoteEntity::class,
        FeedEntity::class,
        FeedGroupEntity::class,
        FeedGroupSubscriptionEntity::class,
        FeedLastUpdatedEntity::class,
        RemoteKioskVideoEntity::class
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun feedDAO(): FeedDAO
    abstract fun feedGroupDAO(): FeedGroupDAO
    abstract fun playlistDAO(): PlaylistDAO
    abstract fun playlistRemoteDAO(): PlaylistRemoteDAO
    abstract fun playlistStreamDAO(): PlaylistStreamDAO
    abstract fun searchHistoryDAO(): SearchHistoryDAO
    abstract fun streamDAO(): StreamDAO
    abstract fun streamHistoryDAO(): StreamHistoryDAO
    abstract fun streamStateDAO(): StreamStateDAO
    abstract fun subscriptionDAO(): SubscriptionDAO
    abstract fun remoteKioskVideoDAO(): RemoteKioskVideoDAO

    companion object {
        const val DATABASE_NAME: String = "newpipe.db"
    }
}
