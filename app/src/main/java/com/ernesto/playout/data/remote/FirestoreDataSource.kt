package com.ernesto.playout.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.ernesto.playout.data.model.Facility
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor() {

    private val db = FirebaseFirestore.getInstance()

    suspend fun fetchAllFacilities(): List<Facility> {
        val snapshot = db.collection("facilities").get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                val photoUrls = doc.get("photoUrls") as? List<*>
                val facility = Facility(
                    fid = (doc.getLong("fid") ?: return@mapNotNull null).toInt(),
                    name = doc.getString("name"),
                    photo = photoUrls?.firstOrNull()?.toString(),
                    sport = doc.getString("sport"),
                    description = doc.getString("description"),
                    condition = doc.getLong("condition")?.toInt(),
                    water = doc.getLong("water")?.toInt(),
                    seats = doc.getLong("seats")?.toInt(),
                    experience = doc.getLong("experience")?.toInt(),
                    longitude = doc.getDouble("longitude"),
                    latitude = doc.getDouble("latitude"),
                    photoUrlsJson = photoUrls?.joinToString(",") { it.toString() },
                    neighbourhood = doc.getString("neighbourhood")
                )
                Log.d("PlayOut_Firebase", "Facility ${facility.fid} photo: ${facility.photo}")
                facility
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun submitProposal(facility: Map<String, Any>): String {
        val ref = db.collection("proposals").add(facility).await()
        return ref.id
    }
}
