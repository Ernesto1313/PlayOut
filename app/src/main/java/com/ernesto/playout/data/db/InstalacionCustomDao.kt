package com.ernesto.playout.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ernesto.playout.data.model.InstalacionCustom
import kotlinx.coroutines.flow.Flow

@Dao
interface InstalacionCustomDao {
    @Query("SELECT * FROM instalaciones_custom")
    fun getAll(): Flow<List<InstalacionCustom>>

    @Query("SELECT * FROM instalaciones_custom WHERE fid = :fid")
    fun getById(fid: Int): Flow<InstalacionCustom?>

    @Query("SELECT * FROM instalaciones_custom WHERE fid = :fid")
    suspend fun getByIdOnce(fid: Int): InstalacionCustom?

    @Query("SELECT MAX(fid) FROM instalaciones_custom")
    suspend fun getMaxFid(): Int?

    @Delete
    suspend fun delete(instalacion: InstalacionCustom)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(instalacion: InstalacionCustom)

    @Update
    suspend fun update(instalacion: InstalacionCustom)
}
