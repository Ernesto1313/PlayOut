package com.ernesto.playout.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.ernesto.playout.data.model.Facility
import com.ernesto.playout.data.model.Proposal
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor() {

    private val db = FirebaseFirestore.getInstance()

    suspend fun fetchAllFacilities(): List<Facility> {
        val snapshot = db.collection("facilities").get(Source.SERVER).await()
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

    suspend fun submitProposal(proposal: Proposal): String {
        val data = hashMapOf(
            "name" to proposal.name,
            "sport" to proposal.sport,
            "description" to proposal.description,
            "condition" to proposal.condition,
            "water" to proposal.water,
            "seats" to proposal.seats,
            "experience" to proposal.experience,
            "longitude" to proposal.longitude,
            "latitude" to proposal.latitude,
            "photoUrls" to proposal.photoUrls,
            "neighbourhood" to proposal.neighbourhood,
            "status" to "pending",
            "localFid" to proposal.localFid,
            "userId" to proposal.userId,
            "username" to proposal.username,
            "submittedAt" to com.google.firebase.Timestamp.now()
        )
        val ref = db.collection("proposals").add(data).await()
        return ref.id
    }

    suspend fun getUserProposals(localFids: List<Int>): List<Proposal> {
        if (localFids.isEmpty()) return emptyList()
        val snapshot = db.collection("proposals")
            .whereIn("localFid", localFids)
            .get().await()
        return snapshot.documents.map { doc ->
            Proposal(
                id = doc.id,
                name = doc.getString("name") ?: "",
                sport = doc.getString("sport") ?: "",
                status = doc.getString("status") ?: "pending",
                localFid = doc.getLong("localFid")?.toInt() ?: 0
            )
        }
    }

    suspend fun fetchApprovedProposals(): List<Facility> {
        val snapshot = db.collection("proposals")
            .whereEqualTo("status", "approved")
            .get(Source.SERVER)
            .await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                val photoUrls = doc.get("photoUrls") as? List<*>
                Facility(
                    fid = doc.getLong("localFid")?.toInt() ?: return@mapNotNull null,
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
                    neighbourhood = doc.getString("neighbourhood"),
                    photoUrlsJson = photoUrls?.joinToString(",") { it.toString() }
                )
            } catch (e: Exception) { null }
        }
    }
}
