package org.schabi.newpipe.database.remotekiosk.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.schabi.newpipe.database.remotekiosk.model.RemoteKioskVideoEntity

@Dao
abstract class RemoteKioskVideoDAO {
    @Query(
        "SELECT * FROM ${RemoteKioskVideoEntity.TABLE_NAME} " +
            "WHERE ${RemoteKioskVideoEntity.KIOSK_ID} = :kioskId " +
            "ORDER BY ${RemoteKioskVideoEntity.POSITION_INDEX} ASC"
    )
    abstract fun getByKioskId(kioskId: String): List<RemoteKioskVideoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(items: List<RemoteKioskVideoEntity>)

    @Query(
        "DELETE FROM ${RemoteKioskVideoEntity.TABLE_NAME} " +
            "WHERE ${RemoteKioskVideoEntity.KIOSK_ID} = :kioskId"
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
