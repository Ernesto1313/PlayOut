package com.ernesto.playout.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ernesto.playout.data.model.InstalacionCustom
import kotlinx.coroutines.flow.Flow

@Dao
interface InstalacionCustomDao {
    @Query("SELECT * FROM instalaciones_custom")
    fun getAll(): Flow<List<InstalacionCustom>>

    @Insert
    suspend fun insert(instalacion: InstalacionCustom): Long
}
