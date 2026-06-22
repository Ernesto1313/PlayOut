package com.ernesto.playout.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ernesto.playout.data.model.CustomFacility
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomFacilityDao {
    @Query("SELECT * FROM custom_facilities")
    fun getAll(): Flow<List<CustomFacility>>

    @Query("SELECT * FROM custom_facilities WHERE fid = :fid")
    fun getById(fid: Int): Flow<CustomFacility?>

    @Query("SELECT * FROM custom_facilities WHERE fid = :fid")
    suspend fun getByIdOnce(fid: Int): CustomFacility?

    @Query("SELECT MAX(fid) FROM custom_facilities")
    suspend fun getMaxFid(): Int?

    @Delete
    suspend fun delete(facility: CustomFacility)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(facility: CustomFacility)

    @Update
    suspend fun update(facility: CustomFacility)
}
