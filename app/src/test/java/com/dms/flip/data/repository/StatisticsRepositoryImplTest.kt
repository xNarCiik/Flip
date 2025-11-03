package com.dms.flip.data.repository

import com.dms.flip.ui.settings.statistics.CategoryStat
import com.dms.flip.ui.settings.statistics.DetailedStats
import com.dms.flip.ui.settings.statistics.MonthlyProgress
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StatisticsRepositoryImplTest {

    private val repository = StatisticsRepositoryImpl()

    @Test
    fun `getStatistics emits predefined ui state`() = runTest {
        val statsDeferred = async { repository.getStatistics().first() }

        advanceTimeBy(1_500)

        val stats = statsDeferred.await()

        assertFalse(stats.isLoading)
        assertEquals(142, stats.totalPleasures)
        assertEquals(12, stats.currentStreak)
        assertEquals(4.2f, stats.averagePerDay)
        assertEquals(89, stats.activeDays)
        assertEquals(MonthlyProgress(completed = 21, total = 31), stats.monthlyProgress)
        assertEquals(
            listOf(
                CategoryStat(name = "🍽️ Gastronomie", count = 45),
                CategoryStat(name = "🎵 Musique", count = 38),
                CategoryStat(name = "🏃 Sport", count = 32)
            ),
            stats.favoriteCategories
        )
        assertEquals(
            DetailedStats(
                bestStreak = 18,
                weekProgress = "5 / 7 jours",
                weeklyAverage = "28 plaisirs",
                lastPleasure = "Il y a 2h"
            ),
            stats.detailedStats
        )
    }
}
