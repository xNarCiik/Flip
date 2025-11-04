package com.dms.flip.data.repository

import android.content.Context
import android.net.Uri
import com.dms.flip.ui.util.compressImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.FileInputStream
import javax.inject.Inject

class StorageRepository @Inject constructor(
    private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val firestore: FirebaseFirestore
) {
    suspend fun uploadUserAvatar(imageUri: Uri): String {
        val userId = firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("Utilisateur non connecté")

        val timestamp = System.currentTimeMillis()
        val storageRef = storage.reference.child("avatars/$userId/$timestamp.jpg")

        val compressedFile = compressImage(context, imageUri)
        val stream = FileInputStream(compressedFile)
        try {
            storageRef.putStream(stream).await()
        } finally {
            stream.close()
            compressedFile.delete()
        }

        // Récupération du lien de téléchargement
        val downloadUrl = storageRef.downloadUrl.await().toString()

        // Mise à jour du Firestore
        firestore.collection("users").document(userId)
            .update("avatarUrl", downloadUrl).await()

        return downloadUrl
    }

    suspend fun uploadPostImage(userId: String, imageUri: Uri): String {
        val timestamp = System.currentTimeMillis()
        val storageRef = storage.reference.child("posts/$userId/post_$timestamp.jpg")

        val compressedFile = compressImage(context, imageUri)
        val stream = FileInputStream(compressedFile)
        try {
            storageRef.putStream(stream).await()
        } finally {
            stream.close()
            compressedFile.delete()
        }

        // Récupération du lien de téléchargement
        return storageRef.downloadUrl.await().toString()
    }
}
