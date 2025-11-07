package com.dms.flip.data.mock.community

import android.net.Uri
import com.dms.flip.domain.model.community.Post
import com.dms.flip.domain.model.community.Paged
import com.dms.flip.domain.model.community.PostComment
import com.dms.flip.domain.model.community.PublicProfile
import com.dms.flip.domain.repository.community.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockFeedRepository @Inject constructor(
    private val dataSource: MockCommunityDataSource
) : FeedRepository {

    override fun observeFriendsFeed(limit: Int, cursor: String?): Flow<Paged<Post>> =
        dataSource.feedPosts.map { posts ->
            if (limit <= 0) {
                Paged(emptyList(), null)
            } else {
                val limited = posts.take(limit)
                val nextCursor = posts.getOrNull(limit)?.id
                Paged(limited, nextCursor)
            }
        }

    override suspend fun getPublicProfile(userId: String): PublicProfile {
        val baseProfile = dataSource.getPublicProfile(userId)
        val relationship = dataSource.determineRelationship(userId)
        return baseProfile.copy(relationshipStatus = relationship)
    }

    override suspend fun fetchComments(
        postId: String,
        limit: Int
    ): List<PostComment> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshPost(postId: String): Post? {
        TODO("Not yet implemented")
    }

    override suspend fun createPost(
        content: String,
        pleasureCategory: String?,
        pleasureTitle: String?,
        photoUri: Uri?
    ) {
        dataSource.createPost(content, pleasureCategory, pleasureTitle, photoUri)
    }

    override suspend fun toggleLike(postId: String) {
        dataSource.togglePostLike(postId)
    }

    override suspend fun addComment(postId: String, content: String): PostComment {
        val comment = dataSource.createComment(content)
        dataSource.addCommentToPost(postId, comment)
        return comment
    }

    override suspend fun deleteComment(postId: String, commentId: String) {
        val userId = dataSource.getCurrentUser().id
        dataSource.removeCommentFromPost(postId, commentId, userId)
    }

    override suspend fun deletePost(postId: String) {
        val userId = dataSource.getCurrentUser().id
        dataSource.removePost(postId, userId)
    }
}
