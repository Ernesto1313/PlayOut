package com.ernesto.playout.data.repository

import com.ernesto.playout.data.db.InstalacionCustomDao
import com.ernesto.playout.data.db.InstalacionDao
import com.ernesto.playout.data.model.Instalacion
import com.ernesto.playout.data.model.InstalacionCustom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class InstalacionRepository @Inject constructor(
    private val dao: InstalacionDao,
    private val instalacionCustomDao: InstalacionCustomDao
) {

    fun getAllInstalaciones(): Flow<List<Instalacion>> =
        combine(dao.getAll(), instalacionCustomDao.getAll()) { assets, customs ->
            val customMapped = customs.map { c ->
                Instalacion(
                    fid = c.fid,
                    nombre_sitio = c.nombre_sitio,
                    foto = c.foto,
                    categoria = c.categoria,
                    descripcion = c.descripcion,
                    estado = c.estado,
                    agua = c.agua,
                    asientos = c.asientos,
                    experiencia_uso = c.experiencia_uso,
                    longitud = c.longitud,
                    latitud = c.latitud
                )
            }
            assets + customMapped
        }

    fun getInstalacionById(fid: Int): Flow<Instalacion?> = flow {
        val inst = if (fid >= 10000) {
            instalacionCustomDao.getById(fid)
                ?.let { c ->
                    Instalacion(c.fid, c.nombre_sitio, c.foto, c.categoria, c.descripcion,
                        c.estado, c.agua, c.asientos, c.experiencia_uso, c.longitud, c.latitud)
                }
        } else {
            dao.getById(fid)
        }
        emit(inst)
    }

    fun getInstalacionesByCategoria(categoria: String): Flow<List<Instalacion>> =
        dao.getByCategoria(categoria)

    fun getAllCustomInstalaciones(): Flow<List<InstalacionCustom>> =
        instalacionCustomDao.getAll()

    suspend fun insertCustom(inst: InstalacionCustom) = instalacionCustomDao.insert(inst)

    suspend fun deleteCustom(inst: InstalacionCustom) = instalacionCustomDao.delete(inst)
}
