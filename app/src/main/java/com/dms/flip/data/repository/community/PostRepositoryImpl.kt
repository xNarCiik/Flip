package com.dms.flip.data.repository.community

import com.dms.flip.data.firebase.dto.PostDto
import com.dms.flip.domain.repository.community.PostRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PostRepository {

    companion object {
        private const val POSTS_COLLECTION = "posts"
    }

    override suspend fun createPost(
        authorId: String,
        content: String,
        pleasureCategory: String?,
        pleasureTitle: String?,
        photoUrl: String?
    ): String {
        val postDto = PostDto(
            authorId = authorId,
            content = content,
            pleasureCategory = pleasureCategory,
            pleasureTitle = pleasureTitle,
            photoUrl = photoUrl,
            timestamp = null,
            likeCount = 0,
            commentsCount = 0
        )

        val docRef = firestore.collection(POSTS_COLLECTION)
            .add(postDto)
            .await()

        return docRef.id
    }
}
