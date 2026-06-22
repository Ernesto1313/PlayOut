package com.ernesto.playout.data.db

import androidx.room.Dao
import androidx.room.Query
import com.ernesto.playout.data.model.Facility
import kotlinx.coroutines.flow.Flow

@Dao
interface FacilityDao {

    @Query("SELECT * FROM facilities")
    fun getAll(): Flow<List<Facility>>

    @Query("SELECT * FROM facilities WHERE fid = :id")
    suspend fun getById(id: Int): Facility?

    @Query("SELECT * FROM facilities WHERE sport = :sport")
    fun getBySport(sport: String): Flow<List<Facility>>
}
