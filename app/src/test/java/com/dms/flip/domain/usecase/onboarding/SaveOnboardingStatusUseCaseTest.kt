package com.dms.flip.domain.usecase.onboarding

import com.dms.flip.domain.model.Pleasure
import com.dms.flip.domain.repository.onboarding.OnboardingRepository
import com.dms.flip.domain.usecase.settings.SetDailyReminderStateUseCase
import com.dms.flip.domain.usecase.settings.SetReminderTimeUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class SaveOnboardingStatusUseCaseTest {

    private lateinit var onboardingRepository: OnboardingRepository
    private lateinit var setDailyReminderState: SetDailyReminderStateUseCase
    private lateinit var setReminderTime: SetReminderTimeUseCase
    private lateinit var useCase: SaveOnboardingStatusUseCase

    @Before
    fun setUp() {
        onboardingRepository = mock()
        setDailyReminderState = mock()
        setReminderTime = mock()
        useCase = SaveOnboardingStatusUseCase(
            onboardingRepository = onboardingRepository,
            setDailyReminderStateUseCase = setDailyReminderState,
            setReminderTimeUseCase = setReminderTime,
        )
    }

    @Test
    fun `invoke saves onboarding data and updates reminders`() = runTest {
        // Given
        val pleasures = listOf(Pleasure(id = "1"))
        val username = "alice"
        val notificationsEnabled = true
        val reminderTimeValue = "09:30"

        // When
        useCase(
            username = username,
            pleasures = pleasures,
            notificationEnabled = notificationsEnabled,
            reminderTime = reminderTimeValue,
        )

        // Then
        verify(onboardingRepository).saveOnboardingStatus(username = username, pleasures = pleasures)
        verify(setDailyReminderState).invoke(notificationsEnabled)
        verify(setReminderTime).invoke(reminderTimeValue)
    }
}
