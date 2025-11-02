package com.dms.flip.data.mock.community

import com.dms.flip.data.model.PleasureCategory
import com.dms.flip.domain.model.community.Friend
import com.dms.flip.domain.model.community.FriendPleasure
import com.dms.flip.domain.model.community.FriendRequest
import com.dms.flip.domain.model.community.FriendRequestSource
import com.dms.flip.domain.model.community.PleasureStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockCommunityDataSource @Inject constructor() {

    private val knownUsers = mutableMapOf(
        "friend_alex" to Friend(
            id = "friend_alex",
            username = "Alexandre Dupont",
            handle = "@alexandre",
            avatarUrl = "https://example.com/avatar/alexandre.png",
            streak = 7,
            isOnline = true,
            currentPleasure = FriendPleasure(
                title = "Session de lecture du soir",
                category = PleasureCategory.CULTURE,
                status = PleasureStatus.IN_PROGRESS
            ),
            favoriteCategory = PleasureCategory.CULTURE
        ),
        "friend_lea" to Friend(
            id = "friend_lea",
            username = "Léa Bernard",
            handle = "@lea",
            avatarUrl = "https://example.com/avatar/lea.png",
            streak = 12,
            isOnline = false,
            currentPleasure = FriendPleasure(
                title = "Yoga matinal",
                category = PleasureCategory.WELLNESS,
                status = PleasureStatus.COMPLETED
            ),
            favoriteCategory = PleasureCategory.WELLNESS
        ),
        "friend_quentin" to Friend(
            id = "friend_quentin",
            username = "Quentin Moreau",
            handle = "@quentin",
            avatarUrl = "https://example.com/avatar/quentin.png",
            streak = 3,
            isOnline = true,
            currentPleasure = FriendPleasure(
                title = "Course au parc",
                category = PleasureCategory.OUTDOOR,
                status = PleasureStatus.IN_PROGRESS
            ),
            favoriteCategory = PleasureCategory.OUTDOOR
        ),
        "friend_sarah" to Friend(
            id = "friend_sarah",
            username = "Sarah Lopez",
            handle = "@sarah",
            avatarUrl = "https://example.com/avatar/sarah.png",
            streak = 21,
            isOnline = false,
            currentPleasure = FriendPleasure(
                title = "Atelier peinture",
                category = PleasureCategory.CREATIVE,
                status = PleasureStatus.IN_PROGRESS
            ),
            favoriteCategory = PleasureCategory.CREATIVE
        ),
        "friend_thomas" to Friend(
            id = "friend_thomas",
            username = "Thomas Garcia",
            handle = "@thomas",
            avatarUrl = "https://example.com/avatar/thomas.png",
            streak = 9,
            isOnline = true,
            currentPleasure = FriendPleasure(
                title = "Cours de guitare",
                category = PleasureCategory.CULTURE,
                status = PleasureStatus.IN_PROGRESS
            ),
            favoriteCategory = PleasureCategory.CULTURE
        )
    )

    private val _friends = MutableStateFlow(
        listOf(
            knownUsers.getValue("friend_alex"),
            knownUsers.getValue("friend_lea"),
            knownUsers.getValue("friend_quentin")
        )
    )
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()

    private val now = System.currentTimeMillis()

    private val _pendingReceived = MutableStateFlow(
        listOf(
            FriendRequest(
                id = "request_${UUID.randomUUID()}",
                userId = "friend_sarah",
                username = knownUsers.getValue("friend_sarah").username,
                handle = knownUsers.getValue("friend_sarah").handle,
                avatarUrl = knownUsers.getValue("friend_sarah").avatarUrl,
                requestedAt = now - TimeUnit.HOURS.toMillis(3),
                source = FriendRequestSource.SUGGESTION
            )
        )
    )
    val pendingReceived: StateFlow<List<FriendRequest>> = _pendingReceived.asStateFlow()

    private val _pendingSent = MutableStateFlow(
        listOf(
            FriendRequest(
                id = "request_${UUID.randomUUID()}",
                userId = "friend_thomas",
                username = knownUsers.getValue("friend_thomas").username,
                handle = knownUsers.getValue("friend_thomas").handle,
                avatarUrl = knownUsers.getValue("friend_thomas").avatarUrl,
                requestedAt = now - TimeUnit.HOURS.toMillis(6),
                source = FriendRequestSource.SEARCH
            )
        )
    )
    val pendingSent: StateFlow<List<FriendRequest>> = _pendingSent.asStateFlow()

    fun addFriend(friend: Friend) {
        registerUser(friend)
        _friends.update { current ->
            current.filterNot { it.id == friend.id } + friend
        }
    }

    fun removeFriend(friendId: String) {
        _friends.update { current -> current.filterNot { it.id == friendId } }
    }

    fun addPendingReceived(request: FriendRequest) {
        registerUser(
            Friend(
                id = request.userId,
                username = request.username,
                handle = request.handle,
                avatarUrl = request.avatarUrl
            )
        )
        _pendingReceived.update { current ->
            current.filterNot { it.userId == request.userId } + request
        }
    }

    fun removePendingReceived(requestId: String) {
        _pendingReceived.update { current -> current.filterNot { it.id == requestId } }
    }

    fun addPendingSent(request: FriendRequest) {
        registerUser(
            Friend(
                id = request.userId,
                username = request.username,
                handle = request.handle,
                avatarUrl = request.avatarUrl
            )
        )
        _pendingSent.update { current ->
            current.filterNot { it.userId == request.userId } + request
        }
    }

    fun removePendingSent(requestId: String) {
        _pendingSent.update { current -> current.filterNot { it.id == requestId } }
    }

    fun getUser(userId: String): Friend =
        knownUsers.getOrPut(userId) { createPlaceholderUser(userId) }

    fun nextRequestId(): String = "request_${UUID.randomUUID()}"

    private fun registerUser(friend: Friend) {
        knownUsers[friend.id] = friend
    }

    private fun createPlaceholderUser(userId: String): Friend {
        val sanitizedId = userId.lowercase().replace("[^a-z0-9]".toRegex(), "")
        val handleSuffix = sanitizedId.takeIf { it.isNotBlank() } ?: "ami"
        val username = "Invité ${handleSuffix.takeLast(4).ifBlank { "Flip" }}"
        return Friend(
            id = userId,
            username = username,
            handle = "@${handleSuffix.take(12)}",
            avatarUrl = null,
            favoriteCategory = PleasureCategory.OTHER
        )
    }
}
