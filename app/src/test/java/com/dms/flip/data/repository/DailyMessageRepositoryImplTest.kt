package com.dms.flip.data.repository

import com.dms.flip.data.local.LocalDailyMessagesDataSource
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DailyMessageRepositoryImplTest {

    private lateinit var localDataSource: LocalDailyMessagesDataSource
    private lateinit var repository: DailyMessageRepositoryImpl

    @Before
    fun setUp() {
        localDataSource = mock()
        repository = DailyMessageRepositoryImpl(localDataSource)
    }

    @Test
    fun `getDailyMessages returns values from data source`() {
        val messages = listOf("message 1", "message 2")
        whenever(localDataSource.getDailyMessages()).thenReturn(messages)

        val result = repository.getDailyMessages()

        assertThat(result).isEqualTo(messages)
        verify(localDataSource).getDailyMessages()
    }

    @Test
    fun `getRandomDailyMessage returns element from data source`() {
        val messages = listOf("only message")
        whenever(localDataSource.getDailyMessages()).thenReturn(messages)

        val result = repository.getRandomDailyMessage()

        assertThat(result).isEqualTo("only message")
        verify(localDataSource, times(1)).getDailyMessages()
    }

    @Test
    fun `getRandomDailyMessage returns value contained in list`() {
        val messages = listOf("a", "b", "c")
        whenever(localDataSource.getDailyMessages()).thenReturn(messages)

        val result = repository.getRandomDailyMessage()

        assertThat(messages).contains(result)
    }
}
