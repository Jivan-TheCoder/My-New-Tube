package com.playtube.protube.video.music.database.remotekiosk.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.playtube.protube.video.music.database.remotekiosk.model.RemoteKioskVideoEntity

@Dao
abstract class RemoteKioskVideoDAO {
    @Query(
        "SELECT * FROM ${RemoteKioskVideoEntity.Companion.TABLE_NAME} " +
            "WHERE ${RemoteKioskVideoEntity.Companion.KIOSK_ID} = :kioskId " +
            "ORDER BY ${RemoteKioskVideoEntity.Companion.POSITION_INDEX} ASC"
    )
    abstract fun getByKioskId(kioskId: String): List<RemoteKioskVideoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(items: List<RemoteKioskVideoEntity>)

    @Query(
        "DELETE FROM ${RemoteKioskVideoEntity.Companion.TABLE_NAME} " +
            "WHERE ${RemoteKioskVideoEntity.Companion.KIOSK_ID} = :kioskId"
    )
    abstract fun deleteByKioskId(kioskId: String)

    @Transaction
    open fun replaceForKiosk(kioskId: String, items: List<RemoteKioskVideoEntity>) {
        deleteByKioskId(kioskId)
        if (items.isNotEmpty()) {
            insertAll(items)
        }
    }
}
