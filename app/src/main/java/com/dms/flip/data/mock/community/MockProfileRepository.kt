package com.dms.flip.data.mock.community

import com.dms.flip.domain.model.community.PublicProfile
import com.dms.flip.domain.repository.community.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockProfileRepository @Inject constructor(
    private val dataSource: MockCommunityDataSource
) : ProfileRepository {

    override suspend fun getPublicProfile(userId: String): PublicProfile {
        val baseProfile = dataSource.getPublicProfile(userId)
        val relationship = dataSource.determineRelationship(userId)
        return baseProfile.copy(relationshipStatus = relationship)
    }
}
