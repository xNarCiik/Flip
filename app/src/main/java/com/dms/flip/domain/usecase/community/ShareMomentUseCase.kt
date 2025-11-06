package com.dms.flip.domain.usecase.community

import android.net.Uri
import com.dms.flip.domain.model.Pleasure
import com.dms.flip.domain.repository.community.FeedRepository
import javax.inject.Inject

/**
 * Use case pour partager un moment (plaisir du jour) avec la communauté
 */
class ShareMomentUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    suspend operator fun invoke(
        pleasure: Pleasure,
        comment: String,
        photoUri: Uri? = null
    ) {
        feedRepository.createPost(
            content = comment,
            pleasureCategory = pleasure.category.name,
            pleasureTitle = pleasure.title,
            photoUri = photoUri
        )
    }
}
