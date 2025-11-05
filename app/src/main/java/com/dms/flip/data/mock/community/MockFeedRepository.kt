package com.dms.flip.data.mock.community

import com.dms.flip.domain.model.community.Post
import com.dms.flip.domain.model.community.Paged
import com.dms.flip.domain.model.community.PostComment
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

    override suspend fun createPost(
        content: String,
        pleasureCategory: String?,
        pleasureTitle: String?,
        photoUrl: String?
    ) {
        dataSource.createPost(content, pleasureCategory, pleasureTitle, photoUrl)
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
