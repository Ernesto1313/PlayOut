package com.ernesto.playout.data.repository

import com.ernesto.playout.data.db.CustomFacilityDao
import com.ernesto.playout.data.db.FacilityDao
import com.ernesto.playout.data.model.CustomFacility
import com.ernesto.playout.data.model.Facility
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FacilityRepository @Inject constructor(
    private val dao: FacilityDao,
    private val customFacilityDao: CustomFacilityDao
) {

    fun getAllFacilities(): Flow<List<Facility>> =
        combine(dao.getAll(), customFacilityDao.getAll()) { assets, customs ->
            val customMapped = customs.map { c ->
                Facility(
                    fid = c.fid,
                    name = c.name,
                    photo = c.photo,
                    sport = c.sport,
                    description = c.description,
                    condition = c.condition,
                    water = c.water,
                    seats = c.seats,
                    experience = c.experience,
                    longitude = c.longitude,
                    latitude = c.latitude
                )
            }
            assets + customMapped
        }

    fun getFacilityById(fid: Int): Flow<Facility?> {
        return if (fid >= 10000) {
            customFacilityDao.getById(fid).map { custom ->
                custom?.let {
                    Facility(
                        fid = it.fid,
                        name = it.name,
                        photo = it.photo,
                        sport = it.sport,
                        description = it.description,
                        condition = it.condition,
                        water = it.water,
                        seats = it.seats,
                        experience = it.experience,
                        longitude = it.longitude,
                        latitude = it.latitude
                    )
                }
            }
        } else {
            flow { emit(dao.getById(fid)) }
        }
    }

    fun getFacilitiesBySport(sport: String): Flow<List<Facility>> =
        dao.getBySport(sport)

    fun getAllCustomFacilities(): Flow<List<CustomFacility>> =
        customFacilityDao.getAll()

    suspend fun insertCustomFacility(facility: CustomFacility): Long {
        val maxFid = customFacilityDao.getMaxFid() ?: 9999
        val newFid = if (maxFid < 10000) 10000 else maxFid + 1
        val withFid = facility.copy(fid = newFid)
        customFacilityDao.insert(withFid)
        return newFid.toLong()
    }

    suspend fun deleteCustomFacility(facility: CustomFacility) = customFacilityDao.delete(facility)

    fun getCustomFacilityById(fid: Int): Flow<CustomFacility?> = customFacilityDao.getById(fid)

    suspend fun updateCustomFacility(facility: CustomFacility) = customFacilityDao.update(facility)
}
