package com.playtube.protube.video.music.database.stream.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.playtube.protube.video.music.database.BasicDAO
import com.playtube.protube.video.music.database.stream.model.StreamStateEntity
import io.reactivex.rxjava3.core.Flowable

@Dao
interface StreamStateDAO : BasicDAO<StreamStateEntity> {

    @Query("SELECT * FROM " + StreamStateEntity.Companion.STREAM_STATE_TABLE)
    override fun getAll(): Flowable<List<StreamStateEntity>>

    @Query("DELETE FROM " + StreamStateEntity.Companion.STREAM_STATE_TABLE)
    override fun deleteAll(): Int

    override fun listByService(serviceId: Int): Flowable<List<StreamStateEntity>> {
        throw UnsupportedOperationException()
    }

    @Query("SELECT * FROM " + StreamStateEntity.Companion.STREAM_STATE_TABLE + " WHERE " + StreamStateEntity.Companion.JOIN_STREAM_ID + " = :streamId")
    fun getState(streamId: Long): Flowable<MutableList<StreamStateEntity>>

    @Query("DELETE FROM " + StreamStateEntity.Companion.STREAM_STATE_TABLE + " WHERE " + StreamStateEntity.Companion.JOIN_STREAM_ID + " = :streamId")
    fun deleteState(streamId: Long): Int

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    fun silentInsertInternal(streamState: StreamStateEntity)

    @Transaction
    fun upsert(stream: StreamStateEntity): Long {
        silentInsertInternal(stream)
        return update(stream).toLong()
    }
}
