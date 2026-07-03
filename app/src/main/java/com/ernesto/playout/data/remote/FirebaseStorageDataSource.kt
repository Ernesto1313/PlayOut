package com.ernesto.playout.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStorageDataSource @Inject constructor() {
    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadPhoto(localPath: String, proposalId: String, suffix: String): String {
        val file = java.io.File(localPath)
        if (!file.exists()) return ""
        val ref = storage.reference
            .child("proposals/$proposalId/$suffix.jpg")
        ref.putFile(Uri.fromFile(file)).await()
        return ref.downloadUrl.await().toString()
    }
}
