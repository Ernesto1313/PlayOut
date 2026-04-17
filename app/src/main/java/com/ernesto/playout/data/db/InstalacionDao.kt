package com.ernesto.playout.data.db

import androidx.room.Dao
import androidx.room.Query
import com.ernesto.playout.data.model.Instalacion
import kotlinx.coroutines.flow.Flow

@Dao
interface InstalacionDao {

    @Query("SELECT * FROM instalaciones")
    fun getAll(): Flow<List<Instalacion>>

    @Query("SELECT * FROM instalaciones WHERE fid = :id")
    suspend fun getById(id: Int): Instalacion?

    @Query("SELECT * FROM instalaciones WHERE categoria = :categoria")
    fun getByCategoria(categoria: String): Flow<List<Instalacion>>
}
