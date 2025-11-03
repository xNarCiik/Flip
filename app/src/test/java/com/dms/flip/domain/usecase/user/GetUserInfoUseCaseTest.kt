package com.dms.flip.domain.usecase.user

import com.dms.flip.domain.model.UserInfo
import com.dms.flip.domain.repository.UserRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetUserInfoUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var useCase: GetUserInfoUseCase

    @Before
    fun setUp() {
        userRepository = mock()
        useCase = GetUserInfoUseCase(userRepository)
    }

    @Test
    fun `invoke returns user info flow from repository`() = runTest {
        // Given
        val expectedUser = UserInfo(username = "Bob")
        whenever(userRepository.getUserInfo()).thenReturn(flowOf(expectedUser))

        // When
        val result = useCase().first()

        // Then
        assertThat(result).isEqualTo(expectedUser)
    }

    @Test
    fun `invoke returns null when repository emits null`() = runTest {
        // Given
        whenever(userRepository.getUserInfo()).thenReturn(flowOf(null))

        // When
        val result = useCase().first()

        // Then
        assertThat(result).isNull()
    }
}
