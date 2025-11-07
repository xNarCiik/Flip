package com.dms.flip.domain.usecase.community

import com.dms.flip.domain.model.community.Paged
import com.dms.flip.domain.model.community.Post
import com.dms.flip.domain.model.community.PostComment
import com.dms.flip.domain.repository.community.FeedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFriendsFeed @Inject constructor(
    private val repo: FeedRepository
) {
    operator fun invoke(limit: Int, cursor: String?): Flow<Paged<Post>> =
        repo.observeFriendsFeed(limit, cursor)
}

class FetchComments @Inject constructor(
    private val repo: FeedRepository
) {
    suspend operator fun invoke(postId: String, limit: Int): List<PostComment> =
        repo.fetchComments(postId, limit)
}

class ToggleLike @Inject constructor(
    private val repo: FeedRepository
) {
    suspend operator fun invoke(postId: String) = repo.toggleLike(postId)
}

class AddComment @Inject constructor(
    private val repo: FeedRepository
) {
    suspend operator fun invoke(postId: String, content: String): PostComment =
        repo.addComment(postId, content)
}

class DeleteComment @Inject constructor(
    private val repo: FeedRepository
) {
    suspend operator fun invoke(postId: String, commentId: String) =
        repo.deleteComment(postId, commentId)
}

class DeletePost @Inject constructor(
    private val feedRepository: FeedRepository
) {
    suspend operator fun invoke(postId: String) = feedRepository.deletePost(postId)
}

class RefreshPost @Inject constructor(
    private val repo: FeedRepository
) {
    suspend operator fun invoke(postId: String): Post? =
        repo.refreshPost(postId)
}

data class FeedUseCases @Inject constructor(
    val observeFriendsFeed: ObserveFriendsFeed,
    val fetchComments: FetchComments,
    val toggleLike: ToggleLike,
    val addComment: AddComment,
    val deletePost: DeletePost,
    val deleteComment: DeleteComment,
    val refreshPost: RefreshPost
)
