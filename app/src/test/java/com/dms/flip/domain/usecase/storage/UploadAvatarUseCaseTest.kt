package com.dms.flip.domain.usecase.storage

import android.net.Uri
import com.dms.flip.data.repository.StorageRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UploadAvatarUseCaseTest {

    private lateinit var storageRepository: StorageRepository
    private lateinit var useCase: UploadAvatarUseCase

    @Before
    fun setUp() {
        storageRepository = mock()
        useCase = UploadAvatarUseCase(storageRepository)
    }

    @Test
    fun `invoke delegates to repository and returns uploaded url`() = runTest {
        // Given
        val imageUri = mock<Uri>()
        val uploadedUrl = "https://example.com/avatar.png"
        whenever(storageRepository.uploadUserAvatar(imageUri)).thenReturn(uploadedUrl)

        // When
        val result = useCase(imageUri)

        // Then
        assertThat(result).isEqualTo(uploadedUrl)
        verify(storageRepository).uploadUserAvatar(imageUri)
    }
}
