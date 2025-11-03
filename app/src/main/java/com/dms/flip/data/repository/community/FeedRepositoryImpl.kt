package com.dms.flip.data.repository.community

import com.dms.flip.data.firebase.dto.CommentDto
import com.dms.flip.data.firebase.mapper.toDomain
import com.dms.flip.data.firebase.source.FeedSource
import com.dms.flip.data.firebase.source.ProfileSource
import com.dms.flip.domain.model.community.Friend
import com.dms.flip.domain.model.community.FriendPost
import com.dms.flip.domain.model.community.Paged
import com.dms.flip.domain.model.community.PostComment
import com.dms.flip.domain.repository.community.FeedRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val feedSource: FeedSource,
    private val profileSource: ProfileSource
) : FeedRepository {

    override fun observeFriendsFeed(limit: Int, cursor: String?): Flow<Paged<FriendPost>> {
        val uid = auth.currentUser?.uid ?: return flowOf(Paged(emptyList(), null))
        return feedSource.observeFriendsFeed(uid, limit, cursor)
            .map { page ->
                val posts = coroutineScope {
                    page.items.map { document ->
                        async {
                            val author = resolveFriend(document.data.authorId)
                            val comments = feedSource.getComments(document.id)
                                .map { (id, dto) -> dto.toDomain(id) }
                            val liked = feedSource.isPostLiked(document.id, uid)
                            document.data.toDomain(
                                id = document.id,
                                author = author,
                                comments = comments,
                                isLiked = liked
                            )
                        }
                    }.awaitAll()
                }
                Paged(posts, page.nextCursor)
            }
    }

    private suspend fun resolveFriend(userId: String): Friend {
        val profile = profileSource.getPublicProfile(userId)
        val username = profile?.username ?: ""
        val handle = profile?.handle ?: ""
        val avatar = profile?.avatarUrl
        val streak = profile?.stats?.get("currentStreak") ?: 0
        return Friend(
            id = userId,
            username = username,
            handle = handle,
            avatarUrl = avatar,
            streak = streak
        )
    }

    override suspend fun toggleLike(postId: String, like: Boolean) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        feedSource.toggleLike(postId, uid, like)
    }

    override suspend fun addComment(postId: String, content: String): PostComment {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val profile = profileSource.getPublicProfile(uid)
        val comment = CommentDto(
            userId = uid,
            username = profile?.username ?: "",
            userHandle = profile?.handle ?: "",
            avatarUrl = profile?.avatarUrl,
            content = content
        )
        val (id, dto) = feedSource.addComment(postId, comment)
        return dto.toDomain(id)
    }
}
