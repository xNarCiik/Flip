package com.dms.flip.domain.usecase.community

import android.net.Uri
import com.dms.flip.domain.repository.community.PostRepository
import com.dms.flip.data.repository.StorageRepository
import com.dms.flip.domain.model.Pleasure
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

/**
 * Use case pour partager un moment (plaisir du jour) avec la communauté
 */
class ShareMomentUseCase @Inject constructor(
    private val postRepository: PostRepository,
    private val storageRepository: StorageRepository,
    private val firebaseAuth: FirebaseAuth
) {
    suspend operator fun invoke(
        pleasure: Pleasure,
        comment: String,
        photoUri: Uri? = null
    ) {
        val userId = firebaseAuth.currentUser?.uid 
            ?: throw IllegalStateException("User not authenticated")

        // Upload de la photo si présente
        val photoUrl = photoUri?.let { uri ->
            storageRepository.uploadPostImage(userId, uri)
        }

        // Créer le post
        postRepository.createPost(
            authorId = userId,
            content = comment,
            pleasureCategory = pleasure.category.name,
            pleasureTitle = pleasure.title,
            photoUrl = photoUrl
        )
    }
}
