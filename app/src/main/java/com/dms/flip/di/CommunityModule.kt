package com.dms.flip.di

import com.dms.flip.BuildConfig
import com.dms.flip.data.firebase.source.FeedSource
import com.dms.flip.data.firebase.source.FirestoreFeedSource
import com.dms.flip.data.firebase.source.FirestoreProfileSource
import com.dms.flip.data.firebase.source.FirestoreFriendsRequestsSource
import com.dms.flip.data.firebase.source.FirestoreSearchSource
import com.dms.flip.data.firebase.source.FirestoreSuggestionsSource
import com.dms.flip.data.firebase.source.FriendsRequestsSource
import com.dms.flip.data.firebase.source.ProfileSource
import com.dms.flip.data.firebase.source.SearchSource
import com.dms.flip.data.firebase.source.SuggestionsSource
import com.dms.flip.data.mock.community.MockFeedRepository
import com.dms.flip.data.mock.community.MockFriendsRequestsRepository
import com.dms.flip.data.mock.community.MockSearchRepository
import com.dms.flip.data.mock.community.MockSuggestionsRepository
import com.dms.flip.data.repository.community.FeedRepositoryImpl
import com.dms.flip.data.repository.community.FriendsRequestsRepositoryImpl
import com.dms.flip.data.repository.community.SearchRepositoryImpl
import com.dms.flip.data.repository.community.SuggestionsRepositoryImpl
import com.dms.flip.domain.repository.community.FeedRepository
import com.dms.flip.domain.repository.community.FriendsRequestsRepository
import com.dms.flip.domain.repository.community.SearchRepository
import com.dms.flip.domain.repository.community.SuggestionsRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommunityModule {

    @Provides
    @Singleton
    fun provideFeedSource(firestore: FirebaseFirestore, functions: FirebaseFunctions): FeedSource =
        FirestoreFeedSource(firestore, functions)

    @Provides
    @Singleton
    fun provideFriendsRequestsSource(
        firestore: FirebaseFirestore,
        firebaseFunctions: FirebaseFunctions
    ): FriendsRequestsSource = FirestoreFriendsRequestsSource(firestore, firebaseFunctions)

    @Provides
    @Singleton
    fun provideSuggestionsSource(firestore: FirebaseFirestore): SuggestionsSource =
        FirestoreSuggestionsSource(firestore)

    @Provides
    @Singleton
    fun provideSearchSource(firestore: FirebaseFirestore): SearchSource =
        FirestoreSearchSource(firestore)

    @Provides
    @Singleton
    fun provideProfileSource(firestore: FirebaseFirestore): ProfileSource =
        FirestoreProfileSource(firestore)

    @Provides
    @Singleton
    fun provideFeedRepository(
        impl: Lazy<FeedRepositoryImpl>,
        mock: Lazy<MockFeedRepository>
    ): FeedRepository =
        if (BuildConfig.USE_MOCK_COMMUNITY_DATA) mock.get() else impl.get()

    @Provides
    @Singleton
    fun provideSuggestionsRepository(
        impl: Lazy<SuggestionsRepositoryImpl>,
        mock: Lazy<MockSuggestionsRepository>
    ): SuggestionsRepository =
        if (BuildConfig.USE_MOCK_COMMUNITY_DATA) mock.get() else impl.get()

    @Provides
    @Singleton
    fun provideSearchRepository(
        impl: Lazy<SearchRepositoryImpl>,
        mock: Lazy<MockSearchRepository>
    ): SearchRepository =
        if (BuildConfig.USE_MOCK_COMMUNITY_DATA) mock.get() else impl.get()
    
    @Provides
    @Singleton
    fun provideRequestsRepository(
        impl: Lazy<FriendsRequestsRepositoryImpl>,
        mock: Lazy<MockFriendsRequestsRepository>
    ): FriendsRequestsRepository =
        if (BuildConfig.USE_MOCK_COMMUNITY_DATA) mock.get() else impl.get()
}
