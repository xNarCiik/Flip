package com.dms.flip.data.repository

import com.dms.flip.ui.settings.statistics.CategoryStat
import com.dms.flip.ui.settings.statistics.DetailedStats
import com.dms.flip.ui.settings.statistics.MonthlyProgress
import com.google.common.truth.Truth.assertThat
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

        assertThat(stats.isLoading).isFalse()
        assertThat(stats.totalPleasures).isEqualTo(142)
        assertThat(stats.currentStreak).isEqualTo(12)
        assertThat(stats.averagePerDay).isEqualTo(4.2f)
        assertThat(stats.activeDays).isEqualTo(89)
        assertThat(stats.monthlyProgress).isEqualTo(MonthlyProgress(completed = 21, total = 31))
        assertThat(stats.favoriteCategories).isEqualTo(
            listOf(
                CategoryStat(name = "🍽️ Gastronomie", count = 45),
                CategoryStat(name = "🎵 Musique", count = 38),
                CategoryStat(name = "🏃 Sport", count = 32)
            )
        )
        assertThat(stats.detailedStats).isEqualTo(
            DetailedStats(
                bestStreak = 18,
                weekProgress = "5 / 7 jours",
                weeklyAverage = "28 plaisirs",
                lastPleasure = "Il y a 2h"
            )
        )
    }
}
