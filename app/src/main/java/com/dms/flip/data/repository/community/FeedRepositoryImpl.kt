package com.dms.flip.data.repository.community

import com.dms.flip.data.firebase.dto.CommentDto
import com.dms.flip.data.firebase.mapper.toDomain
import com.dms.flip.data.firebase.source.FeedSource
import com.dms.flip.data.firebase.source.ProfileSource
import com.dms.flip.domain.model.community.Friend
import com.dms.flip.domain.model.community.Post
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

    override fun observeFriendsFeed(limit: Int, cursor: String?): Flow<Paged<Post>> {
        val uid = auth.currentUser?.uid ?: return flowOf(Paged(emptyList(), null))

        return feedSource.observeFriendsFeed(uid, limit, cursor)
            .map { page ->
                val posts = coroutineScope {
                    page.items.map { document ->
                        async {
                            val author = resolveFriend(document.data.authorId)

                            val commentsDeferred = async {
                                feedSource.getComments(document.id)
                                    .map { (id, dto) -> dto.toDomain(id) }
                            }
                            val likedDeferred = async {
                                feedSource.isPostLiked(document.id, uid)
                            }

                            val comments = commentsDeferred.await()
                            val liked = likedDeferred.await()

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
        return Friend(
            id = userId,
            username = profile?.username.orEmpty(),
            handle = profile?.handle.orEmpty(),
            avatarUrl = profile?.avatarUrl,
            streak = profile?.stats?.get("currentStreak") ?: 0
        )
    }

    override suspend fun createPost(
        content: String,
        pleasureCategory: String?,
        pleasureTitle: String?,
        photoUrl: String?
    ) {
        feedSource.createPost(content, pleasureCategory, pleasureTitle, photoUrl)
    }

    override suspend fun toggleLike(postId: String) {
        feedSource.toggleLike(postId)
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

    override suspend fun deleteComment(postId: String, commentId: String) {
        feedSource.deleteComment(postId, commentId)
    }

    override suspend fun deletePost(postId: String) {
        feedSource.deletePost(postId)
    }
}
