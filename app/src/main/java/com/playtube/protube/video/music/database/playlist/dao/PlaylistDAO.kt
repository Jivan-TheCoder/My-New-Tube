package com.playtube.protube.video.music.database.playlist.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.playtube.protube.video.music.database.BasicDAO
import com.playtube.protube.video.music.database.playlist.model.PlaylistEntity
import io.reactivex.rxjava3.core.Flowable

@Dao
interface PlaylistDAO : BasicDAO<PlaylistEntity> {

    @Query("SELECT * FROM playlists")
    override fun getAll(): Flowable<List<PlaylistEntity>>

    @Query("DELETE FROM playlists")
    override fun deleteAll(): Int

    override fun listByService(serviceId: Int): Flowable<List<PlaylistEntity>> {
        throw UnsupportedOperationException()
    }

    @Query("SELECT * FROM playlists WHERE uid = :playlistId")
    fun getPlaylist(playlistId: Long): Flowable<MutableList<PlaylistEntity>>

    @Query("DELETE FROM playlists WHERE uid = :playlistId")
    fun deletePlaylist(playlistId: Long): Int

    @get:Query("SELECT COUNT(*) FROM playlists")
    val count: Flowable<Long>

    @Transaction
    fun upsertPlaylist(playlist: PlaylistEntity): Long {
        if (playlist.uid == -1L) {
            // This situation is probably impossible.
            return insert(playlist)
        } else {
            update(playlist)
            return playlist.uid
        }
    }
}
