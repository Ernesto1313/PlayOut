package com.ernesto.playout.data.remote

import android.util.Log
import com.ernesto.playout.data.db.CustomFacilityDao
import com.ernesto.playout.data.db.FacilityDao
import com.ernesto.playout.data.model.Facility
import com.ernesto.playout.data.util.RatingsCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSyncManager @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val facilityDao: FacilityDao,
    private val customFacilityDao: CustomFacilityDao,
    private val reviewsDataSource: ReviewsDataSource
) {
    fun syncFacilities() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("PlayOut_Firebase", "Syncing facilities from Firestore...")
                val facilities = firestoreDataSource.fetchAllFacilities()
                val approved = firestoreDataSource.fetchApprovedProposals()
                Log.d("PlayOut_Firebase", "Approved proposals count: ${approved.size}")
                approved.forEach {
                    Log.d("PlayOut_Firebase", "Approved proposal: fid=${it.fid} sport=${it.sport} name=${it.name}")
                }
                approved.forEach { approvedFacility ->
                    try {
                        val existing = customFacilityDao.getByIdOnce(approvedFacility.fid)
                        if (existing != null) {
                            val updated = existing.copy(
                                sport = approvedFacility.sport,
                                description = approvedFacility.description,
                                condition = approvedFacility.condition,
                                water = approvedFacility.water,
                                seats = approvedFacility.seats,
                                experience = approvedFacility.experience,
                                photo = approvedFacility.photo
                            )
                            customFacilityDao.update(updated)
                            Log.d("PlayOut_Firebase",
                                "Updated local custom facility ${approvedFacility.fid} with approved data")
                        }
                    } catch (e: Exception) {
                        Log.e("PlayOut_Firebase", "Failed to update local facility: ${e.message}")
                    }
                }
                val allFacilities = facilities + approved
                facilityDao.deleteAll()
                facilityDao.insertAll(allFacilities)
                Log.d("PlayOut_Photo", "First synced facility photo: ${facilities.firstOrNull()?.photo}")
                Log.d("PlayOut_Photo", "First synced facility photoUrlsJson: ${facilities.firstOrNull()?.photoUrlsJson}")
                Log.d("PlayOut_Firebase", "Sync complete: ${allFacilities.size} facilities")

                val allReviews = reviewsDataSource.getAllReviews()
                RatingsCache.update(allReviews)
                Log.d("PlayOut_Firebase", "Ratings cache updated: ${allReviews.size} reviews")
            } catch (e: Exception) {
                Log.e("PlayOut_Firebase", "Sync failed: ${e.message}")
            }
        }
    }
}
