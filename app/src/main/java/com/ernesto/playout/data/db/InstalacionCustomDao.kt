package com.ernesto.playout.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
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

    @Delete
    suspend fun delete(instalacion: InstalacionCustom)

    @Insert
    suspend fun insert(instalacion: InstalacionCustom): Long

    @Update
    suspend fun update(instalacion: InstalacionCustom)
}
