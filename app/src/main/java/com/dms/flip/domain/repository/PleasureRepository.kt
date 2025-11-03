package com.dms.flip.domain.repository

import com.dms.flip.data.model.PleasureCategory
import com.dms.flip.domain.model.Pleasure
import com.dms.flip.domain.model.PleasureHistory
import kotlinx.coroutines.flow.Flow

interface PleasureRepository {
    fun getPleasures(): Flow<List<Pleasure>>
    fun getPleasuresCount(): Flow<Int>
    fun getRandomPleasure(category: PleasureCategory?): Flow<Pleasure>
    suspend fun insert(pleasure: Pleasure)
    suspend fun update(pleasure: Pleasure)
    suspend fun delete(pleasuresId: List<String>)
    suspend fun upsertPleasureHistory(pleasureHistory: PleasureHistory)
    fun getPleasureHistory(id: String): Flow<PleasureHistory?>
}
