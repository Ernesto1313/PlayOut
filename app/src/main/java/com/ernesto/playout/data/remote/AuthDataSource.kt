package com.ernesto.playout.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ernesto.playout.data.model.UserProfile
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthDataSource @Inject constructor() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val currentUserId: String? get() = auth.currentUser?.uid
    val isEmailVerified: Boolean get() = auth.currentUser?.isEmailVerified == true
    val isLoggedIn: Boolean get() = auth.currentUser != null

    suspend fun isUsernameAvailable(username: String): Boolean {
        val snapshot = db.collection("users")
            .whereEqualTo("username", username)
            .get().await()
        return snapshot.isEmpty
    }

    suspend fun register(email: String, password: String, username: String): Result<Unit> {
        return try {
            // Check username uniqueness first
            if (!isUsernameAvailable(username)) {
                return Result.failure(Exception("Username already taken"))
            }
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("Registration failed"))

            // Create user profile in Firestore
            val profile = UserProfile(uid = uid, email = email, username = username, reviewCount = 0)
            db.collection("users").document(uid).set(profile).await()

            // Send verification email
            result.user?.sendEmailVerification()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun getUserProfile(): UserProfile? {
        val uid = currentUserId ?: return null
        return try {
            val doc = db.collection("users").document(uid).get().await()
            doc.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun resendVerificationEmail(): Result<Unit> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
