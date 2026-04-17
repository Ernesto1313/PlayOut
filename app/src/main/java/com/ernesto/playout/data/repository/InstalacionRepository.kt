package com.ernesto.playout.data.repository

import com.ernesto.playout.data.db.InstalacionDao
import com.ernesto.playout.data.model.Instalacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class InstalacionRepository @Inject constructor(
    private val dao: InstalacionDao
) {

    fun getAllInstalaciones(): Flow<List<Instalacion>> = dao.getAll()

    fun getInstalacionById(fid: Int): Flow<Instalacion?> = flow {
        emit(dao.getById(fid))
    }

    fun getInstalacionesByCategoria(categoria: String): Flow<List<Instalacion>> =
        dao.getByCategoria(categoria)
}
