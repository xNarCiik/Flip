package com.dms.flip.domain.usecase.community

import com.dms.flip.domain.model.community.Paged
import com.dms.flip.domain.model.community.FriendPost
import com.dms.flip.domain.model.community.PostComment
import com.dms.flip.domain.repository.community.FeedRepository
import com.dms.flip.domain.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFriendsFeedUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(limit: Int, cursor: String? = null): Flow<Paged<FriendPost>> =
        feedRepository.observeFriendsFeed(limit, cursor)
}

class ToggleLikeUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    suspend operator fun invoke(postId: String, like: Boolean): Result<Unit> =
        runCatching {
            feedRepository.toggleLike(postId, like)
        }.fold(
            onSuccess = { Result.Ok(Unit) },
            onFailure = { Result.Err(it) }
        )
}

class AddCommentUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    suspend operator fun invoke(postId: String, content: String): Result<PostComment> =
        runCatching {
            feedRepository.addComment(postId, content)
        }.fold(
            onSuccess = { Result.Ok(it) },
            onFailure = { Result.Err(it) }
        )
}

class DeleteCommentUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    suspend operator fun invoke(postId: String, commentId: String): Result<Unit> =
        runCatching {
            feedRepository.deleteComment(postId, commentId)
        }.fold(
            onSuccess = { Result.Ok(Unit) },
            onFailure = { Result.Err(it) }
        )
}
