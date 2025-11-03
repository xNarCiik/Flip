package com.dms.flip.di

import android.app.Application
import android.content.Context
import android.content.res.Resources
import com.dms.flip.domain.repository.DailyMessageRepository
import com.dms.flip.domain.repository.PleasureRepository
import com.dms.flip.domain.usecase.GetRandomDailyMessageUseCase
import com.dms.flip.domain.usecase.dailypleasure.GetRandomPleasureUseCase
import com.dms.flip.domain.usecase.history.SaveHistoryEntryUseCase
import com.dms.flip.domain.usecase.pleasures.GetPleasuresUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @ApplicationScope
    @Singleton
    @Provides
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    fun provideGetRandomDailyMessageUseCase(repository: DailyMessageRepository) =
        GetRandomDailyMessageUseCase(repository = repository)

    @Provides
    @Singleton
    fun provideGetPleasuresUseCase(repository: PleasureRepository) =
        GetPleasuresUseCase(repository = repository)

    @Provides
    @Singleton
    fun provideDrawDailyPleasureUseCase(repository: PleasureRepository) =
        GetRandomPleasureUseCase(repository = repository)

    @Provides
    @Singleton
    fun provideSaveHistoryEntryUseCase(repository: PleasureRepository) =
        SaveHistoryEntryUseCase(pleasureRepository = repository)

    @Provides
    @Singleton
    fun provideResources(application: Application): Resources = application.resources

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }
}
