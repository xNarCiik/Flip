package com.dms.flip.data.firebase.mapper

import com.dms.flip.data.firebase.dto.CommentDto
import com.dms.flip.data.firebase.dto.PostDto
import com.dms.flip.data.firebase.dto.PublicProfileDto
import com.dms.flip.data.firebase.dto.RecentActivityDto
import com.dms.flip.domain.model.community.Friend
import com.dms.flip.domain.model.community.PublicProfile
import com.dms.flip.domain.model.community.RelationshipStatus
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Date

class CommunityMappersTest {

    @Test
    fun postDtoToDomain_mapsFieldsCorrectly() {
        val friend = Friend(id = "friend1", username = "Alice", handle = "@alice")
        val dto = PostDto(
            authorId = friend.id,
            content = "Hello",
            timestamp = Date(),
            pleasureCategory = "WELLNESS",
            pleasureTitle = "Yoga",
            likesCount = 2,
            commentsCount = 1
        )
        val commentDto = CommentDto(
            userId = "commenter",
            username = "Bob",
            userHandle = "@bob",
            content = "Nice!",
            timestamp = Date()
        )

        val post = dto.toDomain(
            id = "post",
            author = friend,
            comments = listOf(commentDto.toDomain("comment")),
            isLiked = true
        )

        assertThat(post.id).isEqualTo("post")
        assertThat(post.friend).isEqualTo(friend)
        assertThat(post.pleasureCategory?.name).isEqualTo("WELLNESS")
        assertThat(post.comments).hasSize(1)
        assertThat(post.isLiked).isTrue()
    }

    @Test
    fun recentActivityDtoToDomain_returnsNullWhenCategoryInvalid() {
        val dto = RecentActivityDto(
            pleasureTitle = "Test",
            category = "UNKNOWN",
            completedAt = Date(),
            isCompleted = false
        )

        val activity = dto.toDomain("id")

        assertThat(activity).isNull()
    }

    @Test
    fun publicProfileDtoToDomain_mapsStats() {
        val dto = PublicProfileDto(
            username = "Alice",
            handle = "@alice",
            avatarUrl = "avatar",
            bio = "bio",
            stats = mapOf("friends" to 3, "daysCompleted" to 4, "currentStreak" to 5)
        )
        val recent = listOfNotNull(
            RecentActivityDto(
                pleasureTitle = "Yoga",
                category = "WELLNESS",
                completedAt = Date(),
                isCompleted = true
            ).toDomain("recent")
        )

        val profile: PublicProfile = dto.toDomain("user", recent, RelationshipStatus.NONE)

        assertThat(profile.friendsCount).isEqualTo(3)
        assertThat(profile.daysCompleted).isEqualTo(4)
        assertThat(profile.currentStreak).isEqualTo(5)
        assertThat(profile.recentActivities).hasSize(1)
    }
}
