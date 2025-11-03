package com.dms.flip.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.dms.flip.domain.model.Theme
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

private class InMemoryPreferencesDataStore(
    initialPreferences: Preferences = emptyPreferences()
) : DataStore<Preferences> {
    private val state = MutableStateFlow(initialPreferences)

    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}

class SettingsRepositoryImplTest {

    private lateinit var repository: SettingsRepositoryImpl
    private lateinit var dataStore: InMemoryPreferencesDataStore

    @Before
    fun setUp() {
        dataStore = InMemoryPreferencesDataStore()
        repository = SettingsRepositoryImpl(dataStore)
    }

    @Test
    fun `theme defaults to system`() = runTest(UnconfinedTestDispatcher()) {
        val theme = repository.theme.first()

        assertEquals(Theme.SYSTEM, theme)
    }

    @Test
    fun `setTheme persists and emits new value`() = runTest(UnconfinedTestDispatcher()) {
        repository.setTheme(Theme.DARK)

        val theme = repository.theme.first()

        assertEquals(Theme.DARK, theme)
    }

    @Test
    fun `daily reminder enabled defaults to false`() = runTest(UnconfinedTestDispatcher()) {
        val enabled = repository.dailyReminderEnabled.first()

        assertFalse(enabled)
    }

    @Test
    fun `setDailyReminderEnabled updates stored value`() = runTest(UnconfinedTestDispatcher()) {
        repository.setDailyReminderEnabled(true)

        val enabled = repository.dailyReminderEnabled.first()

        assertTrue(enabled)
    }

    @Test
    fun `reminder time defaults to eleven am`() = runTest(UnconfinedTestDispatcher()) {
        val reminderTime = repository.reminderTime.first()

        assertEquals("11:00", reminderTime)
    }

    @Test
    fun `setReminderTime updates stored reminder time`() = runTest(UnconfinedTestDispatcher()) {
        repository.setReminderTime("08:30")

        val reminderTime = repository.reminderTime.first()

        assertEquals("08:30", reminderTime)
    }
}
