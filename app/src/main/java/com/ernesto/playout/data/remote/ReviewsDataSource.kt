package com.ernesto.playout.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.ernesto.playout.data.model.Review
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewsDataSource @Inject constructor() {
    private val db = FirebaseFirestore.getInstance()

    // Get all reviews for a facility
    suspend fun getReviewsForFacility(fid: Int): List<Review> {
        val snapshot = db.collection("reviews")
            .whereEqualTo("facilityFid", fid)
            .get(Source.SERVER)
            .await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                Review(
                    id = doc.id,
                    facilityFid = doc.getLong("facilityFid")?.toInt() ?: 0,
                    userId = doc.getString("userId") ?: "",
                    username = doc.getString("username") ?: "",
                    stars = doc.getLong("stars")?.toInt() ?: 0,
                    condition = doc.getLong("condition")?.toInt() ?: 0,
                    comment = doc.getString("comment") ?: "",
                    photoUrls = (doc.get("photoUrls") as? List<*>)?.map { it.toString() } ?: emptyList(),
                    timestamp = doc.getLong("timestamp") ?: 0L,
                    isInitial = doc.getBoolean("isInitial") ?: false
                )
            } catch (e: Exception) { null }
        }
    }

    // Check if a user has already reviewed a facility
    suspend fun hasUserReviewed(fid: Int, userId: String): Boolean {
        val snapshot = db.collection("reviews")
            .whereEqualTo("facilityFid", fid)
            .whereEqualTo("userId", userId)
            .get(Source.SERVER)
            .await()
        return !snapshot.isEmpty
    }

    // Submit a new review
    suspend fun submitReview(review: Review): String {
        val data = hashMapOf(
            "facilityFid" to review.facilityFid,
            "userId" to review.userId,
            "username" to review.username,
            "stars" to review.stars,
            "condition" to review.condition,
            "comment" to review.comment,
            "photoUrls" to review.photoUrls,
            "timestamp" to System.currentTimeMillis(),
            "isInitial" to review.isInitial
        )
        val ref = db.collection("reviews").add(data).await()
        return ref.id
    }

    // Update an existing review (resets timestamp to now)
    suspend fun updateReview(reviewId: String, stars: Int, condition: Int,
                             comment: String, photoUrls: List<String>) {
        db.collection("reviews").document(reviewId).update(mapOf(
            "stars" to stars,
            "condition" to condition,
            "comment" to comment,
            "photoUrls" to photoUrls,
            "timestamp" to System.currentTimeMillis()
        )).await()
    }

    // Delete a review
    suspend fun deleteReview(reviewId: String) {
        db.collection("reviews").document(reviewId).delete().await()
    }

    // Get all reviews by a user
    suspend fun getReviewsByUser(userId: String): List<Review> {
        val snapshot = db.collection("reviews")
            .whereEqualTo("userId", userId)
            .get(Source.SERVER)
            .await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                Review(
                    id = doc.id,
                    facilityFid = doc.getLong("facilityFid")?.toInt() ?: 0,
                    username = doc.getString("username") ?: "",
                    stars = doc.getLong("stars")?.toInt() ?: 0,
                    condition = doc.getLong("condition")?.toInt() ?: 0,
                    comment = doc.getString("comment") ?: "",
                    timestamp = doc.getLong("timestamp") ?: 0L
                )
            } catch (e: Exception) { null }
        }
    }
}
