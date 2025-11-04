package com.dms.flip.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.dms.flip.data.local.LocalDailyMessagesDataSource
import com.dms.flip.data.repository.AuthRepository
import com.dms.flip.data.repository.DailyMessageRepositoryImpl
import com.dms.flip.data.repository.HistoryRepositoryImpl
import com.dms.flip.data.repository.OnboardingRepositoryImpl
import com.dms.flip.data.repository.PleasureRepositoryImpl
import com.dms.flip.data.repository.SettingsRepositoryImpl
import com.dms.flip.data.repository.StatisticsRepositoryImpl
import com.dms.flip.data.repository.StorageRepository
import com.dms.flip.data.repository.UserRepositoryImpl
import com.dms.flip.data.repository.community.PostRepositoryImpl
import com.dms.flip.domain.repository.DailyMessageRepository
import com.dms.flip.domain.repository.HistoryRepository
import com.dms.flip.domain.repository.PleasureRepository
import com.dms.flip.domain.repository.SettingsRepository
import com.dms.flip.domain.repository.StatisticsRepository
import com.dms.flip.domain.repository.UserRepository
import com.dms.flip.domain.repository.community.PostRepository
import com.dms.flip.domain.repository.onboarding.OnboardingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth
    ): AuthRepository =
        AuthRepository(auth = firebaseAuth)

    @Provides
    @Singleton
    fun providerStorageRepository(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth,
        storage: FirebaseStorage,
        firestore: FirebaseFirestore
    ): StorageRepository =
        StorageRepository(
            context = context,
            firebaseAuth = firebaseAuth,
            storage = storage,
            firestore = firestore
        )

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): UserRepository = UserRepositoryImpl(firestore = firestore, firebaseAuth = firebaseAuth)

    @Provides
    @Singleton
    fun provideOnboardingRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): OnboardingRepository =
        OnboardingRepositoryImpl(firestore = firestore, firebaseAuth = firebaseAuth)

    @Provides
    @Singleton
    fun bindPleasureRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): PleasureRepository =
        PleasureRepositoryImpl(firestore, firebaseAuth)

    @Provides
    @Singleton
    fun provideHistoryRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): HistoryRepository =
        HistoryRepositoryImpl(firestore = firestore, auth = firebaseAuth)

    @Provides
    @Singleton
    fun provideDailyMessageRepository(localDataSource: LocalDailyMessagesDataSource): DailyMessageRepository =
        DailyMessageRepositoryImpl(localDataSource = localDataSource)

    @Provides
    @Singleton
    fun provideSettingsRepository(dataStore: DataStore<Preferences>): SettingsRepository =
        SettingsRepositoryImpl(dataStore = dataStore)

    @Provides
    @Singleton
    fun provideStatisticsRepository(): StatisticsRepository = StatisticsRepositoryImpl()

    @Provides
    @Singleton
    fun providePostRepository(firestore: FirebaseFirestore): PostRepository = PostRepositoryImpl(firestore = firestore)

}
