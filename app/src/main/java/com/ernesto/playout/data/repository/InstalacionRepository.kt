package com.ernesto.playout.data.repository

import com.ernesto.playout.data.db.InstalacionCustomDao
import com.ernesto.playout.data.db.InstalacionDao
import com.ernesto.playout.data.model.Instalacion
import com.ernesto.playout.data.model.InstalacionCustom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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

    fun getInstalacionById(fid: Int): Flow<Instalacion?> {
        return if (fid >= 10000) {
            instalacionCustomDao.getById(fid).map { custom ->
                custom?.let {
                    Instalacion(
                        fid = it.fid,
                        nombre_sitio = it.nombre_sitio,
                        foto = it.foto,
                        categoria = it.categoria,
                        descripcion = it.descripcion,
                        estado = it.estado,
                        agua = it.agua,
                        asientos = it.asientos,
                        experiencia_uso = it.experiencia_uso,
                        longitud = it.longitud,
                        latitud = it.latitud
                    )
                }
            }
        } else {
            flow { emit(dao.getById(fid)) }
        }
    }

    fun getInstalacionesByCategoria(categoria: String): Flow<List<Instalacion>> =
        dao.getByCategoria(categoria)

    fun getAllCustomInstalaciones(): Flow<List<InstalacionCustom>> =
        instalacionCustomDao.getAll()

    suspend fun insertCustom(inst: InstalacionCustom): Long {
        val maxFid = instalacionCustomDao.getMaxFid() ?: 9999
        val newFid = if (maxFid < 10000) 10000 else maxFid + 1
        val withFid = inst.copy(fid = newFid)
        instalacionCustomDao.insert(withFid)
        return newFid.toLong()
    }

    suspend fun deleteCustom(inst: InstalacionCustom) = instalacionCustomDao.delete(inst)

    fun getCustomById(fid: Int): Flow<InstalacionCustom?> = instalacionCustomDao.getById(fid)

    suspend fun updateCustom(inst: InstalacionCustom) = instalacionCustomDao.update(inst)
}
