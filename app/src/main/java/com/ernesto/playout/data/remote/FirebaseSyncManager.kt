package com.ernesto.playout.data.remote

import android.util.Log
import com.ernesto.playout.data.db.FacilityDao
import com.ernesto.playout.data.model.Facility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSyncManager @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val facilityDao: FacilityDao
) {
    fun syncFacilities() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("PlayOut_Firebase", "Syncing facilities from Firestore...")
                val facilities = firestoreDataSource.fetchAllFacilities()
                facilityDao.deleteAll()
                facilityDao.insertAll(facilities)
                Log.d("PlayOut_Photo", "First synced facility photo: ${facilities.firstOrNull()?.photo}")
                Log.d("PlayOut_Photo", "First synced facility photoUrlsJson: ${facilities.firstOrNull()?.photoUrlsJson}")
                Log.d("PlayOut_Firebase", "Sync complete: ${facilities.size} facilities")
            } catch (e: Exception) {
                Log.e("PlayOut_Firebase", "Sync failed: ${e.message}")
            }
        }
    }
}
