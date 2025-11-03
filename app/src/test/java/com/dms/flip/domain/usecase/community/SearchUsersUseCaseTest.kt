package com.dms.flip.domain.usecase.community

import com.dms.flip.domain.model.community.UserSearchResult
import com.dms.flip.domain.repository.community.SearchRepository
import com.dms.flip.domain.util.Result
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SearchUsersUseCaseTest {

    private lateinit var searchRepository: SearchRepository
    private lateinit var useCase: SearchUsersUseCase

    @Before
    fun setUp() {
        searchRepository = mock()
        useCase = SearchUsersUseCase(searchRepository)
    }

    @Test
    fun `invoke returns Ok when repository succeeds`() = runTest {
        val query = "john"
        val expected = listOf(
            UserSearchResult(
                id = "1",
                username = "John Doe",
                handle = "@john"
            )
        )
        whenever(searchRepository.searchUsers(query, 20)).thenReturn(expected)

        val result = useCase(query)

        assertThat(result).isEqualTo(Result.Ok(expected))
        verify(searchRepository).searchUsers(query, 20)
    }

    @Test
    fun `invoke returns Err when repository throws`() = runTest {
        val query = "error"
        val exception = IllegalStateException("boom")
        whenever(searchRepository.searchUsers(query, 20)).thenThrow(exception)

        val result = useCase(query)

        assertThat(result).isInstanceOf(Result.Err::class.java)
        val err = result as Result.Err
        assertThat(err.throwable).isEqualTo(exception)
        verify(searchRepository).searchUsers(query, 20)
    }
}
