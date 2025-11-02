package com.dms.flip.data.mock.community

import com.dms.flip.data.model.PleasureCategory
import com.dms.flip.domain.model.community.Friend
import com.dms.flip.domain.model.community.FriendPost
import com.dms.flip.domain.model.community.FriendPleasure
import com.dms.flip.domain.model.community.FriendRequest
import com.dms.flip.domain.model.community.FriendRequestSource
import com.dms.flip.domain.model.community.FriendSuggestion
import com.dms.flip.domain.model.community.PostComment
import com.dms.flip.domain.model.community.PleasureStatus
import com.dms.flip.domain.model.community.PublicProfile
import com.dms.flip.domain.model.community.RelationshipStatus
import com.dms.flip.domain.model.community.RecentActivity
import com.dms.flip.domain.model.community.UserSearchResult
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

    private val currentUser = Friend(
        id = "user_camille",
        username = "Camille Martin",
        handle = "@camille",
        avatarUrl = "https://example.com/avatar/camille.png",
        streak = 18,
        isOnline = true,
        currentPleasure = FriendPleasure(
            title = "Balade photo du soir",
            category = PleasureCategory.CREATIVE,
            status = PleasureStatus.IN_PROGRESS
        ),
        favoriteCategory = PleasureCategory.CREATIVE
    )

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
        ),
        "friend_marie" to Friend(
            id = "friend_marie",
            username = "Marie Dubois",
            handle = "@marie",
            avatarUrl = "https://example.com/avatar/marie.png",
            streak = 5,
            isOnline = false,
            currentPleasure = FriendPleasure(
                title = "Atelier poterie",
                category = PleasureCategory.CREATIVE,
                status = PleasureStatus.IN_PROGRESS
            ),
            favoriteCategory = PleasureCategory.CREATIVE
        ),
        "friend_nicolas" to Friend(
            id = "friend_nicolas",
            username = "Nicolas Petit",
            handle = "@nicolas",
            avatarUrl = "https://example.com/avatar/nicolas.png",
            streak = 2,
            isOnline = true,
            currentPleasure = FriendPleasure(
                title = "Session de natation",
                category = PleasureCategory.SPORT,
                status = PleasureStatus.IN_PROGRESS
            ),
            favoriteCategory = PleasureCategory.SPORT
        ),
        "friend_claire" to Friend(
            id = "friend_claire",
            username = "Claire Rossi",
            handle = "@claire",
            avatarUrl = "https://example.com/avatar/claire.png",
            streak = 16,
            isOnline = false,
            currentPleasure = FriendPleasure(
                title = "Atelier cuisine italienne",
                category = PleasureCategory.CULINARY,
                status = PleasureStatus.COMPLETED
            ),
            favoriteCategory = PleasureCategory.CULINARY
        ),
        "friend_bastien" to Friend(
            id = "friend_bastien",
            username = "Bastien Fournier",
            handle = "@bastien",
            avatarUrl = "https://example.com/avatar/bastien.png",
            streak = 11,
            isOnline = true,
            currentPleasure = FriendPleasure(
                title = "Atelier théâtre",
                category = PleasureCategory.CULTURE,
                status = PleasureStatus.IN_PROGRESS
            ),
            favoriteCategory = PleasureCategory.CULTURE
        )
    )

    init {
        registerUser(currentUser)
    }

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

    private val _suggestions = MutableStateFlow(
        listOf(
            FriendSuggestion(
                id = "friend_marie",
                username = knownUsers.getValue("friend_marie").username,
                handle = knownUsers.getValue("friend_marie").handle,
                avatarUrl = knownUsers.getValue("friend_marie").avatarUrl,
                mutualFriendsCount = 4
            ),
            FriendSuggestion(
                id = "friend_nicolas",
                username = knownUsers.getValue("friend_nicolas").username,
                handle = knownUsers.getValue("friend_nicolas").handle,
                avatarUrl = knownUsers.getValue("friend_nicolas").avatarUrl,
                mutualFriendsCount = 2
            ),
            FriendSuggestion(
                id = "friend_claire",
                username = knownUsers.getValue("friend_claire").username,
                handle = knownUsers.getValue("friend_claire").handle,
                avatarUrl = knownUsers.getValue("friend_claire").avatarUrl,
                mutualFriendsCount = 6
            )
        )
    )
    val suggestions: StateFlow<List<FriendSuggestion>> = _suggestions.asStateFlow()

    private val _feedPosts = MutableStateFlow(
        listOf(
            FriendPost(
                id = "post_lea_morning_yoga",
                friend = knownUsers.getValue("friend_lea"),
                content = "Séance de yoga matinale terminée. Une énergie incroyable pour commencer la journée !",
                timestamp = now - TimeUnit.HOURS.toMillis(2),
                likesCount = 18,
                commentsCount = 2,
                isLiked = true,
                pleasureCategory = PleasureCategory.WELLNESS,
                pleasureTitle = "Yoga matinal",
                comments = listOf(
                    commentFrom("friend_alex", "Bravo pour ta constance !", now - TimeUnit.HOURS.toMillis(1)),
                    commentFrom("friend_quentin", "On se fait une séance ensemble demain ?", now - TimeUnit.MINUTES.toMillis(45))
                )
            ),
            FriendPost(
                id = "post_alex_evening_reading",
                friend = knownUsers.getValue("friend_alex"),
                content = "J'ai fini 'Le cercle littéraire'. Une vraie pépite à recommander !",
                timestamp = now - TimeUnit.HOURS.toMillis(5),
                likesCount = 24,
                commentsCount = 3,
                pleasureCategory = PleasureCategory.CULTURE,
                pleasureTitle = "Session de lecture",
                comments = listOf(
                    commentFrom("friend_lea", "Je note pour ma prochaine lecture.", now - TimeUnit.HOURS.toMillis(4)),
                    commentFrom("friend_sarah", "Tu vas adorer la suite !", now - TimeUnit.HOURS.toMillis(3)),
                    commentFrom(currentUser.id, "Merci pour l'inspiration, je l'ajoute à ma liste !", now - TimeUnit.HOURS.toMillis(2))
                )
            ),
            FriendPost(
                id = "post_quentin_run",
                friend = knownUsers.getValue("friend_quentin"),
                content = "8km de course au parc ce matin, record battu malgré le vent !",
                timestamp = now - TimeUnit.DAYS.toMillis(1),
                likesCount = 32,
                commentsCount = 1,
                pleasureCategory = PleasureCategory.OUTDOOR,
                pleasureTitle = "Course au parc",
                comments = listOf(
                    commentFrom("friend_thomas", "Tu es prêt pour le semi-marathon !", now - TimeUnit.DAYS.toMillis(1) + TimeUnit.HOURS.toMillis(1))
                )
            )
        )
    )
    val feedPosts: StateFlow<List<FriendPost>> = _feedPosts.asStateFlow()

    private val knownProfiles = mutableMapOf(
        currentUser.id to PublicProfile(
            id = currentUser.id,
            username = currentUser.username,
            handle = currentUser.handle,
            avatarUrl = currentUser.avatarUrl,
            bio = "Toujours à la recherche d'une nouvelle inspiration créative.",
            friendsCount = _friends.value.size,
            daysCompleted = 142,
            currentStreak = currentUser.streak,
            recentActivities = listOf(
                RecentActivity(
                    id = "activity_camille_photo",
                    pleasureTitle = "Balade photo du soir",
                    category = PleasureCategory.CREATIVE,
                    completedAt = now - TimeUnit.HOURS.toMillis(12),
                    isCompleted = false
                ),
                RecentActivity(
                    id = "activity_camille_cafe",
                    pleasureTitle = "Découverte d'un nouveau café",
                    category = PleasureCategory.CULINARY,
                    completedAt = now - TimeUnit.DAYS.toMillis(1),
                    isCompleted = true
                )
            ),
            relationshipStatus = RelationshipStatus.FRIEND
        ),
        "friend_alex" to PublicProfile(
            id = "friend_alex",
            username = knownUsers.getValue("friend_alex").username,
            handle = knownUsers.getValue("friend_alex").handle,
            avatarUrl = knownUsers.getValue("friend_alex").avatarUrl,
            bio = "Lecteur compulsif et amateur de cafés littéraires.",
            friendsCount = 58,
            daysCompleted = 97,
            currentStreak = knownUsers.getValue("friend_alex").streak,
            recentActivities = listOf(
                RecentActivity(
                    id = "activity_alex_reading",
                    pleasureTitle = "Session de lecture du soir",
                    category = PleasureCategory.CULTURE,
                    completedAt = now - TimeUnit.HOURS.toMillis(5),
                    isCompleted = true
                ),
                RecentActivity(
                    id = "activity_alex_tasting",
                    pleasureTitle = "Dégustation de cafés", 
                    category = PleasureCategory.CULINARY,
                    completedAt = now - TimeUnit.DAYS.toMillis(2),
                    isCompleted = true
                )
            )
        ),
        "friend_lea" to PublicProfile(
            id = "friend_lea",
            username = knownUsers.getValue("friend_lea").username,
            handle = knownUsers.getValue("friend_lea").handle,
            avatarUrl = knownUsers.getValue("friend_lea").avatarUrl,
            bio = "Toujours partante pour un cours de yoga au lever du soleil.",
            friendsCount = 74,
            daysCompleted = 163,
            currentStreak = knownUsers.getValue("friend_lea").streak,
            recentActivities = listOf(
                RecentActivity(
                    id = "activity_lea_yoga",
                    pleasureTitle = "Yoga matinal",
                    category = PleasureCategory.WELLNESS,
                    completedAt = now - TimeUnit.HOURS.toMillis(2),
                    isCompleted = true
                ),
                RecentActivity(
                    id = "activity_lea_hike",
                    pleasureTitle = "Randonnée en montagne",
                    category = PleasureCategory.OUTDOOR,
                    completedAt = now - TimeUnit.DAYS.toMillis(3),
                    isCompleted = true
                )
            )
        ),
        "friend_quentin" to PublicProfile(
            id = "friend_quentin",
            username = knownUsers.getValue("friend_quentin").username,
            handle = knownUsers.getValue("friend_quentin").handle,
            avatarUrl = knownUsers.getValue("friend_quentin").avatarUrl,
            bio = "Toujours prêt pour une nouvelle aventure sportive.",
            friendsCount = 36,
            daysCompleted = 52,
            currentStreak = knownUsers.getValue("friend_quentin").streak,
            recentActivities = listOf(
                RecentActivity(
                    id = "activity_quentin_run",
                    pleasureTitle = "Course au parc",
                    category = PleasureCategory.OUTDOOR,
                    completedAt = now - TimeUnit.DAYS.toMillis(1),
                    isCompleted = true
                ),
                RecentActivity(
                    id = "activity_quentin_climb",
                    pleasureTitle = "Escalade en salle",
                    category = PleasureCategory.SPORT,
                    completedAt = now - TimeUnit.DAYS.toMillis(4),
                    isCompleted = true
                )
            )
        )
    )

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

    fun hideSuggestion(userId: String) {
        _suggestions.update { current -> current.filterNot { it.id == userId } }
    }

    fun togglePostLike(postId: String, like: Boolean) {
        _feedPosts.update { posts ->
            posts.map { post ->
                if (post.id == postId) {
                    val targetLikes = if (like) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
                    post.copy(
                        isLiked = like,
                        likesCount = targetLikes
                    )
                } else {
                    post
                }
            }
        }
    }

    fun addCommentToPost(postId: String, comment: PostComment) {
        _feedPosts.update { posts ->
            posts.map { post ->
                if (post.id == postId) {
                    val updatedComments = post.comments + comment
                    post.copy(
                        comments = updatedComments,
                        commentsCount = updatedComments.size
                    )
                } else {
                    post
                }
            }
        }
    }

    fun createComment(content: String): PostComment {
        return PostComment(
            id = nextCommentId(),
            userId = currentUser.id,
            username = currentUser.username,
            userHandle = currentUser.handle,
            avatarUrl = currentUser.avatarUrl,
            content = content,
            timestamp = System.currentTimeMillis()
        )
    }

    fun searchUsers(query: String, limit: Int): List<UserSearchResult> {
        if (query.isBlank()) return emptyList()
        val normalized = query.trim().lowercase()
        return knownUsers.values
            .asSequence()
            .filter { it.id != currentUser.id }
            .filter { friend ->
                friend.username.lowercase().contains(normalized) ||
                        friend.handle.lowercase().contains(normalized)
            }
            .take(limit)
            .map { friend ->
                UserSearchResult(
                    id = friend.id,
                    username = friend.username,
                    handle = friend.handle,
                    avatarUrl = friend.avatarUrl,
                    relationshipStatus = determineRelationship(friend.id)
                )
            }
            .toList()
    }

    fun getPublicProfile(userId: String): PublicProfile {
        val friend = getUser(userId)
        return knownProfiles[userId] ?: createDefaultProfile(friend)
    }

    fun determineRelationship(userId: String): RelationshipStatus {
        return when {
            userId == currentUser.id -> RelationshipStatus.FRIEND
            _friends.value.any { it.id == userId } -> RelationshipStatus.FRIEND
            _pendingSent.value.any { it.userId == userId } -> RelationshipStatus.PENDING_SENT
            _pendingReceived.value.any { it.userId == userId } -> RelationshipStatus.PENDING_RECEIVED
            else -> RelationshipStatus.NONE
        }
    }

    fun getCurrentUser(): Friend = currentUser

    fun getFriendIds(): Set<String> = _friends.value.map { it.id }.toSet()

    fun getPendingReceivedIds(): Set<String> = _pendingReceived.value.map { it.userId }.toSet()

    fun getPendingSentIds(): Set<String> = _pendingSent.value.map { it.userId }.toSet()

    fun getUser(userId: String): Friend =
        knownUsers.getOrPut(userId) { createPlaceholderUser(userId) }

    fun nextRequestId(): String = "request_${UUID.randomUUID()}"

    fun nextCommentId(): String = "comment_${UUID.randomUUID()}"

    private fun registerUser(friend: Friend) {
        knownUsers[friend.id] = friend
        if (!knownProfiles.containsKey(friend.id)) {
            knownProfiles[friend.id] = createDefaultProfile(friend)
        }
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

    private fun createDefaultProfile(friend: Friend): PublicProfile {
        return PublicProfile(
            id = friend.id,
            username = friend.username,
            handle = friend.handle,
            avatarUrl = friend.avatarUrl,
            bio = "Toujours partant pour une nouvelle expérience.",
            friendsCount = (_friends.value.size + 3).coerceAtLeast(10),
            daysCompleted = 42,
            currentStreak = friend.streak,
            recentActivities = friend.currentPleasure?.let { pleasure ->
                listOf(
                    RecentActivity(
                        id = "activity_${friend.id}_current",
                        pleasureTitle = pleasure.title,
                        category = pleasure.category,
                        completedAt = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1),
                        isCompleted = pleasure.status == PleasureStatus.COMPLETED
                    )
                )
            } ?: emptyList(),
            relationshipStatus = determineRelationship(friend.id)
        )
    }

    private fun commentFrom(userId: String, content: String, timestamp: Long): PostComment {
        val user = knownUsers[userId] ?: currentUser
        return PostComment(
            id = "comment_${UUID.randomUUID()}",
            userId = user.id,
            username = user.username,
            userHandle = user.handle,
            avatarUrl = user.avatarUrl,
            content = content,
            timestamp = timestamp
        )
    }
}
