package com.dms.flip.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.dms.flip.ui.util.compressImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
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

    /**
     * ✅ Uploade l'image vers le chemin prévisible
     * Ne retourne pas d'URL, car le backend s'en charge.
     */
    suspend fun uploadPostImage(userId: String, postId: String, imageUri: Uri) {
        val storagePath = "posts/$userId/$postId/original.jpg"
        val storageRef = storage.reference.child(storagePath)

        var compressedFile: File? = null
        var stream: FileInputStream? = null
        try {
            compressedFile = compressImage(context, imageUri)
            stream = FileInputStream(compressedFile)
            storageRef.putStream(stream).await()
        } finally {
            stream?.close()
            compressedFile?.delete()
        }
    }

    /**
     * Supprime les images d'un post (original + miniature)
     * Utilisée pour le rollback si la création du post échoue.
     */
    suspend fun deletePostImage(userId: String, postId: String) {
        val originalPath = "posts/$userId/$postId/original.jpg"
        val thumbPath = "posts/$userId/$postId/thumb.jpg"

        try {
            storage.reference.child(originalPath).delete().await()
        } catch (e: Exception) {
            Log.w("StorageRepository", "Impossible de supprimer l'image originale (rollback) : $originalPath", e)
        }

        try {
            storage.reference.child(thumbPath).delete().await()
        } catch (_: Exception) {
            Log.i("StorageRepository", "Miniature non trouvée ou échec suppression (rollback) : $thumbPath")
        }
    }
}
