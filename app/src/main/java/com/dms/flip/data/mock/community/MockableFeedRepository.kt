package com.dms.flip.data.mock.community

import android.net.Uri
import com.dms.flip.data.repository.community.FeedRepositoryImpl
import com.dms.flip.domain.model.community.Paged
import com.dms.flip.domain.model.community.Post
import com.dms.flip.domain.model.community.PostComment
import com.dms.flip.domain.model.community.PublicProfile
import com.dms.flip.domain.repository.SettingsRepository
import com.dms.flip.domain.repository.community.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockableFeedRepository @Inject constructor(
    private val real: FeedRepositoryImpl,
    private val mock: MockFeedRepository,
    private val settingsRepository: SettingsRepository,
) : FeedRepository {

    override fun observeFriendsFeed(limit: Int, cursor: String?): Flow<Paged<Post>> =
        settingsRepository.useMockCommunityData.flatMapLatest { useMock ->
            if (useMock) {
                mock.observeFriendsFeed(limit, cursor)
            } else {
                real.observeFriendsFeed(limit, cursor)
            }
        }

    private suspend fun delegate(): FeedRepository {
        return if (settingsRepository.useMockCommunityData.first()) {
            mock
        } else {
            real
        }
    }

    override suspend fun getPublicProfile(userId: String): PublicProfile? =
        delegate().getPublicProfile(userId)

    override suspend fun fetchComments(postId: String, limit: Int): List<PostComment> =
        delegate().fetchComments(postId, limit)

    override suspend fun refreshPost(postId: String): Post? =
        delegate().refreshPost(postId)

    override suspend fun createPost(
        content: String,
        pleasureCategory: String?,
        pleasureTitle: String?,
        photoUri: Uri?,
    ) {
        delegate().createPost(content, pleasureCategory, pleasureTitle, photoUri)
    }

    override suspend fun toggleLike(postId: String) {
        delegate().toggleLike(postId)
    }

    override suspend fun addComment(postId: String, content: String): PostComment =
        delegate().addComment(postId, content)

    override suspend fun deleteComment(postId: String, commentId: String) {
        delegate().deleteComment(postId, commentId)
    }

    override suspend fun deletePost(postId: String) {
        delegate().deletePost(postId)
    }
}
