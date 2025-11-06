package com.dms.flip.data.repository.community

import com.dms.flip.data.firebase.dto.CommentDto
import com.dms.flip.data.firebase.dto.PostDto
import com.dms.flip.data.firebase.dto.PublicProfileDto
import com.dms.flip.data.firebase.dto.RecentActivityDto
import com.dms.flip.domain.model.community.Paged
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class FeedRepositoryImplTest {

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var feedSource: FakeFeedSource
    private lateinit var profileSource: FakeProfileSource
    private lateinit var repository: FeedRepositoryImpl

    @Before
    fun setUp() {
        auth = mock()
        user = mock()
        whenever(auth.currentUser).thenReturn(user)
        whenever(user.uid).thenReturn("uid")
        feedSource = FakeFeedSource()
        profileSource = FakeProfileSource()
        repository = FeedRepositoryImpl(auth, feedSource, profileSource)
    }

    @Test
    fun observeFriendsFeed_mapsPosts() = runBlocking {
        profileSource.profile = PublicProfileDto(username = "Alice", handle = "@alice", stats = mapOf("currentStreak" to 2))
        feedSource.comments = listOf("comment" to CommentDto(content = "Nice"))
        feedSource.isLiked = true
        feedSource.emit(
            Paged(
                items = listOf(FeedSource.PostDocument("post", PostDto(authorId = "author", content = "Hello", likesCount = 1))),
                nextCursor = null
            )
        )

        val page = repository.observeFriendsFeed(10).first()

        assertThat(page.items).hasSize(1)
        val post = page.items.first()
        assertThat(post.author.username).isEqualTo("Alice")
        assertThat(post.isLiked).isTrue()
        assertThat(post.comments).hasSize(1)
    }

    @Test
    fun addComment_returnsDomainComment() = runBlocking {
        profileSource.profile = PublicProfileDto(username = "Alice", handle = "@alice")
        val comment = repository.addComment("post", "Great")

        assertThat(comment.content).isEqualTo("Great")
        assertThat(comment.username).isEqualTo("Alice")
    }

    private class FakeFeedSource : FeedSource {
        private val flow = MutableSharedFlow<Paged<FeedSource.PostDocument>>(replay = 1)
        var comments: List<Pair<String, CommentDto>> = emptyList()
        var isLiked: Boolean = false

        suspend fun emit(page: Paged<FeedSource.PostDocument>) {
            flow.emit(page)
        }

        override fun observeFriendsFeed(uid: String, limit: Int, cursor: String?) = flow

        override suspend fun toggleLike(postId: String, uid: String) {}

        override suspend fun addComment(postId: String, comment: CommentDto): Pair<String, CommentDto> =
            "id" to comment

        override suspend fun getComments(postId: String, limit: Int): List<Pair<String, CommentDto>> = comments

        override suspend fun isPostLiked(postId: String, uid: String): Boolean = isLiked
        override suspend fun deleteComment(
            postId: String,
            commentId: String,
            uid: String
        ) {
            TODO("Not yet implemented")
        }

        override suspend fun deletePost(postId: String, uid: String) {
            TODO("Not yet implemented")
        }
    }

    private class FakeProfileSource : ProfileSource {
        var profile: PublicProfileDto? = null

        override suspend fun getPublicProfile(userId: String): PublicProfileDto? = profile

        override suspend fun getRecentActivities(
            userId: String,
            limit: Int
        ): List<Pair<String, RecentActivityDto>> = emptyList()
    }
}
