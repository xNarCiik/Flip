package com.dms.flip.data.mock.community

import com.dms.flip.domain.model.community.PleasureCategory
import com.dms.flip.domain.model.community.Friend
import com.dms.flip.domain.model.community.Post
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
        id = "UVavanPylhSnMnH6oGJI9isqypN2",
        username = "Damien Legagnoux",
        handle = "@dams_lgx",
        avatarUrl = "https://firebasestorage.googleapis.com/v0/b/daily-joy-16ce8.firebasestorage.app/o/avatars%2FO9v4fig59HWnnA2J0HeDC6gpwWZ2%2F1762194156609.jpg?alt=media&token=fe22e453-f3b2-4300-8060-621626b86c11",
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
        "friend_emma" to Friend(
            id = "friend_emma",
            username = "La mère michel",
            handle = "@mamamiaa",
            avatarUrl = "https://scontent.cdninstagram.com/v/t51.2885-19/401916050_1385369609057390_7192697213845317191_n.jpg?stp=dst-jpg_s206x206_tt6&_nc_cat=108&ccb=7-5&_nc_sid=bf7eb4&efg=eyJ2ZW5jb2RlX3RhZyI6InByb2ZpbGVfcGljLnd3dy4xMDgwLkMzIn0%3D&_nc_ohc=qVn4MJ0HHO8Q7kNvwE3sWZq&_nc_oc=AdnN5gfuDIdeBymllMgcFHOxMhnigLeek2XkVwqWUTTebGKBhuB6flhsdWlRLtMuGAqHKypBzlCj85vALRT2gqaQ&_nc_zt=24&_nc_ht=scontent.cdninstagram.com&oh=00_Afj5BzdOX_yWaafK_LftZtkDaLQKoDS7764rsMN3rzg83w&oe=690C9EC1",
            streak = 7,
            isOnline = true,
            currentPleasure = FriendPleasure(
                title = "Session de lecture du soir",
                category = PleasureCategory.CULTURE,
                status = PleasureStatus.IN_PROGRESS
            ),
            favoriteCategory = PleasureCategory.CULTURE
        ),
        "friend_dams" to Friend(
            id = "friend_dams",
            username = "Dams",
            handle = "@dams_lgx",
            avatarUrl = "https://instagram.frns1-1.fna.fbcdn.net/v/t51.2885-19/456069928_491069416973778_8102649957987195103_n.jpg?stp=dst-jpg_s320x320_tt6&efg=eyJ2ZW5jb2RlX3RhZyI6InByb2ZpbGVfcGljLmRqYW5nby4xMDgwLmMyIn0&_nc_ht=instagram.frns1-1.fna.fbcdn.net&_nc_cat=106&_nc_oc=Q6cZ2QF9Xmw8N1iCgPnDUx4Kw5eFhwS3qI0R99yeBe-y42tLx3zBCIwn8bY6oBnCdDRrNf45Ar-rBNTqnF0iqNQABwa1&_nc_ohc=WBRH9bfze2oQ7kNvwHjBN7q&_nc_gid=VUXDXVDh4eW4ZxG7unEuRw&edm=APs17CUBAAAA&ccb=7-5&oh=00_AfjL_VrAPaR7xo4kyqWyvnFPkZUfbbOazo6OesOUw6FlwQ&oe=690C8B45&_nc_sid=10d13b",
            streak = 12,
            isOnline = false,
            currentPleasure = FriendPleasure(
                title = "Yoga matinal",
                category = PleasureCategory.WELLNESS,
                status = PleasureStatus.COMPLETED
            ),
            favoriteCategory = PleasureCategory.WELLNESS
        ),
        "friend_kimy" to Friend(
            id = "friend_kimy",
            username = "Kimimi",
            handle = "@kimy_david",
            avatarUrl = "https://instagram.frns1-1.fna.fbcdn.net/v/t51.2885-19/572111130_18544577635056527_2214624452472528223_n.jpg?efg=eyJ2ZW5jb2RlX3RhZyI6InByb2ZpbGVfcGljLmRqYW5nby4xMDgwLmMyIn0&_nc_ht=instagram.frns1-1.fna.fbcdn.net&_nc_cat=111&_nc_oc=Q6cZ2QFNCvY6ONexfO4sERI2xlvmYiLuGlVXDrbduhjZgMD-3p_FljbXdI_UkzkbQ8vGS9Q6DR46J7UlpoGyAbHu8_W3&_nc_ohc=Ap7jGp6iUywQ7kNvwHp_WuH&_nc_gid=d7UwzgG3pzj7Lrftftwovg&edm=AP4sbd4BAAAA&ccb=7-5&oh=00_AfhWzi0TPaWhSrC5Dz4U3WAa8omuqnWYMnEqKAQ2obcKJA&oe=690C9C64&_nc_sid=7a9f4b",
            streak = 3,
            isOnline = true,
            currentPleasure = FriendPleasure(
                title = "Course au parc",
                category = PleasureCategory.OUTDOOR,
                status = PleasureStatus.IN_PROGRESS
            ),
            favoriteCategory = PleasureCategory.OUTDOOR
        ),
        "friend_anthony" to Friend(
            id = "friend_anthony",
            username = "Anthony Arrighi",
            handle = "@anthony.arrighi",
            streak = 21,
            isOnline = false,
            avatarUrl = "https://instagram.frns1-1.fna.fbcdn.net/v/t51.2885-19/502718768_18065806517284093_1519982854029510988_n.jpg?efg=eyJ2ZW5jb2RlX3RhZyI6InByb2ZpbGVfcGljLmRqYW5nby4xMDgwLmMyIn0&_nc_ht=instagram.frns1-1.fna.fbcdn.net&_nc_cat=101&_nc_oc=Q6cZ2QEy3HjmzcGaILUbcmVoA9q6AHMXG1TFUix8HiPDQ_AOrVopsbF71uGlAxTqozBUzUToobuM3mXdUl9z7psxZkuJ&_nc_ohc=uvm_14EYNKAQ7kNvwE5xPEQ&_nc_gid=K1DszhkAKf_TUzYVIt-ioQ&edm=ALGbJPMBAAAA&ccb=7-5&oh=00_AfjzMC3dODdrpoe7MiIcYAJ8omBEYX-bH6mKcoCF5WdZxg&oe=690C8181&_nc_sid=7d3ac5",
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
            streak = 16,
            isOnline = false,
            currentPleasure = FriendPleasure(
                title = "Atelier cuisine italienne",
                category = PleasureCategory.FOOD,
                status = PleasureStatus.COMPLETED
            ),
            favoriteCategory = PleasureCategory.FOOD
        ),
        "friend_bastien" to Friend(
            id = "friend_bastien",
            username = "Bastien Fournier",
            handle = "@bastien",
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

    private val _friends = MutableStateFlow(
        listOf(
            knownUsers.getValue("friend_emma"),
            knownUsers.getValue("friend_dams"),
            knownUsers.getValue("friend_kimy")
        )
    )
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()

    private val now = System.currentTimeMillis()

    private val _pendingReceived = MutableStateFlow(
        listOf(
            FriendRequest(
                id = "request_${UUID.randomUUID()}",
                userId = "friend_claire",
                username = knownUsers.getValue("friend_claire").username,
                handle = knownUsers.getValue("friend_claire").handle,
                avatarUrl = knownUsers.getValue("friend_claire").avatarUrl,
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
            Post(
                id = "post_lea_morning_yoga",
                author = currentUser,
                content = "Séance de méditation terminé \uD83D\uDCA8",
                timestamp = now - TimeUnit.HOURS.toMillis(2),
                likesCount = 18,
                commentsCount = 2,
                isLiked = true,
                pleasureCategory = PleasureCategory.WELLNESS,
                pleasureTitle = "Fumer un pet.. ou deux",
                comments = listOf(
                    commentFrom(
                        "friend_emma",
                        "Bravo pour ta constance !",
                        now - TimeUnit.HOURS.toMillis(1)
                    ),
                    commentFrom(
                        "friend_kimy",
                        "On se fait une séance ensemble demain ?",
                        now - TimeUnit.MINUTES.toMillis(45)
                    )
                )
            ),
            Post(
                id = "post_alex_evening_reading",
                author = knownUsers.getValue("friend_emma"),
                content = "Heureuseeeeee",
                timestamp = now - TimeUnit.HOURS.toMillis(5),
                likesCount = 24,
                commentsCount = 3,
                pleasureCategory = PleasureCategory.SPORT,
                pleasureTitle = "Session de sport avec Ugo \uD83C\uDF51",
                comments = listOf(
                    commentFrom(
                        "friend_dams",
                        "Je note pour ma prochaine lecture.",
                        now - TimeUnit.HOURS.toMillis(4)
                    ),
                    commentFrom(
                        "friend_claire",
                        "Tu vas adorer la suite !",
                        now - TimeUnit.HOURS.toMillis(3)
                    ),
                    commentFrom(
                        currentUser.id,
                        "Merci pour l'inspiration, je l'ajoute à ma liste !",
                        now - TimeUnit.HOURS.toMillis(2)
                    )
                )
            ),
            Post(
                id = "post_kimy",
                author = knownUsers.getValue("friend_kimy"),
                content = "Bon bah c'est repartie, au revoir les copainsss",
                timestamp = now - TimeUnit.DAYS.toMillis(1),
                likesCount = 32,
                commentsCount = 1,
                pleasureCategory = PleasureCategory.OUTDOOR,
                pleasureTitle = "Un week end à l'étranger",
                comments = listOf(
                    commentFrom(
                        "friend_thomas",
                        "Tu es prêt pour le semi-marathon !",
                        now - TimeUnit.DAYS.toMillis(1) + TimeUnit.HOURS.toMillis(1)
                    )
                )
            ),
            Post(
                id = "post_anthony",
                author = knownUsers.getValue("friend_anthony"),
                content = "Oh minceeee",
                timestamp = now - TimeUnit.DAYS.toMillis(1),
                likesCount = 32,
                commentsCount = 1,
                pleasureCategory = PleasureCategory.OUTDOOR,
                pleasureTitle = "Ce soir: 3 litres de vodka",
                comments = listOf(
                    commentFrom(
                        "friend_thomas",
                        "Tu es prêt pour le semi-marathon !",
                        now - TimeUnit.DAYS.toMillis(1) + TimeUnit.HOURS.toMillis(1)
                    )
                )
            )
        )
    )
    val feedPosts: StateFlow<List<Post>> = _feedPosts.asStateFlow()

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
                    category = PleasureCategory.FOOD,
                    completedAt = now - TimeUnit.DAYS.toMillis(1),
                    isCompleted = true
                )
            ),
            relationshipStatus = RelationshipStatus.FRIEND
        ),
        "friend_emma" to PublicProfile(
            id = "friend_emma",
            username = knownUsers.getValue("friend_emma").username,
            handle = knownUsers.getValue("friend_emma").handle,
            avatarUrl = knownUsers.getValue("friend_emma").avatarUrl,
            bio = "Lecteur compulsif et amateur de cafés littéraires.",
            friendsCount = 58,
            daysCompleted = 97,
            currentStreak = knownUsers.getValue("friend_emma").streak,
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
                    category = PleasureCategory.FOOD,
                    completedAt = now - TimeUnit.DAYS.toMillis(2),
                    isCompleted = true
                )
            )
        ),
        "friend_dams" to PublicProfile(
            id = "friend_dams",
            username = knownUsers.getValue("friend_dams").username,
            handle = knownUsers.getValue("friend_dams").handle,
            avatarUrl = knownUsers.getValue("friend_dams").avatarUrl,
            bio = "Toujours partante pour un cours de yoga au lever du soleil.",
            friendsCount = 74,
            daysCompleted = 163,
            currentStreak = knownUsers.getValue("friend_dams").streak,
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
        "friend_kimy" to PublicProfile(
            id = "friend_kimy",
            username = knownUsers.getValue("friend_kimy").username,
            handle = knownUsers.getValue("friend_kimy").handle,
            avatarUrl = knownUsers.getValue("friend_kimy").avatarUrl,
            bio = "Toujours prêt pour une nouvelle aventure sportive.",
            friendsCount = 36,
            daysCompleted = 52,
            currentStreak = knownUsers.getValue("friend_kimy").streak,
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

    init {
        registerUser(currentUser)
    }

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

    fun createPost(
        content: String,
        pleasureCategory: String?,
        pleasureTitle: String?,
        photoUrl: String?
    ) {
    }/*: Post {
        return Post()
    }*/ // TODO

    fun togglePostLike(postId: String) {
        _feedPosts.update { posts ->
            posts.map { post ->
                if (post.id == postId) {
                    val targetLikes =
                        if (!post.isLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(
                            0
                        )
                    post.copy(
                        isLiked = !post.isLiked,
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

    fun removeCommentFromPost(postId: String, commentId: String, userId: String) {
        _feedPosts.update { posts ->
            posts.map { post ->
                if (post.id == postId) {
                    val targetComment = post.comments.firstOrNull { it.id == commentId }
                    if (targetComment == null || targetComment.userId != userId) {
                        post
                    } else {
                        val updatedComments = post.comments.filterNot { it.id == commentId }
                        post.copy(
                            comments = updatedComments,
                            commentsCount = updatedComments.size
                        )
                    }
                } else {
                    post
                }
            }
        }
    }

    fun removePost(postId: String, userId: String) {
        _feedPosts.update { posts ->
            val target = posts.firstOrNull { it.id == postId }
            if (target?.author?.id != userId) {
                posts
            } else {
                posts.filterNot { it.id == postId }
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
        return knownProfiles.getOrPut(userId) { createDefaultProfile(friend) }
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
