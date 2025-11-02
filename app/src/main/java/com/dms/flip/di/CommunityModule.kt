package com.dms.flip.di

import com.dms.flip.BuildConfig
import com.dms.flip.data.firebase.source.FeedSource
import com.dms.flip.data.firebase.source.FirestoreFeedSource
import com.dms.flip.data.firebase.source.FirestoreFriendsSource
import com.dms.flip.data.firebase.source.FirestoreProfileSource
import com.dms.flip.data.firebase.source.FirestoreRequestsSource
import com.dms.flip.data.firebase.source.FirestoreSearchSource
import com.dms.flip.data.firebase.source.FirestoreSuggestionsSource
import com.dms.flip.data.firebase.source.FriendsSource
import com.dms.flip.data.firebase.source.ProfileSource
import com.dms.flip.data.firebase.source.RequestsSource
import com.dms.flip.data.firebase.source.SearchSource
import com.dms.flip.data.firebase.source.SuggestionsSource
import com.dms.flip.data.mock.community.MockFeedRepository
import com.dms.flip.data.mock.community.MockFriendsRepository
import com.dms.flip.data.mock.community.MockProfileRepository
import com.dms.flip.data.mock.community.MockRequestsRepository
import com.dms.flip.data.mock.community.MockSearchRepository
import com.dms.flip.data.mock.community.MockSuggestionsRepository
import com.dms.flip.data.repository.community.FeedRepositoryImpl
import com.dms.flip.data.repository.community.FriendsRepositoryImpl
import com.dms.flip.data.repository.community.ProfileRepositoryImpl
import com.dms.flip.data.repository.community.RequestsRepositoryImpl
import com.dms.flip.data.repository.community.SearchRepositoryImpl
import com.dms.flip.data.repository.community.SuggestionsRepositoryImpl
import com.dms.flip.domain.repository.community.FeedRepository
import com.dms.flip.domain.repository.community.FriendsRepository
import com.dms.flip.domain.repository.community.ProfileRepository
import com.dms.flip.domain.repository.community.RequestsRepository
import com.dms.flip.domain.repository.community.SearchRepository
import com.dms.flip.domain.repository.community.SuggestionsRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.Lazy
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommunityModule {

    @Provides
    @Singleton
    fun provideFeedSource(firestore: FirebaseFirestore): FeedSource = FirestoreFeedSource(firestore)

    @Provides
    @Singleton
    fun provideFriendsSource(firestore: FirebaseFirestore): FriendsSource = FirestoreFriendsSource(firestore)

    @Provides
    @Singleton
    fun provideRequestsSource(firestore: FirebaseFirestore): RequestsSource = FirestoreRequestsSource(firestore)

    @Provides
    @Singleton
    fun provideSuggestionsSource(firestore: FirebaseFirestore): SuggestionsSource = FirestoreSuggestionsSource(firestore)

    @Provides
    @Singleton
    fun provideSearchSource(firestore: FirebaseFirestore): SearchSource = FirestoreSearchSource(firestore)

    @Provides
    @Singleton
    fun provideProfileSource(firestore: FirebaseFirestore): ProfileSource = FirestoreProfileSource(firestore)

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
    fun provideProfileRepository(
        impl: Lazy<ProfileRepositoryImpl>,
        mock: Lazy<MockProfileRepository>
    ): ProfileRepository =
        if (BuildConfig.USE_MOCK_COMMUNITY_DATA) mock.get() else impl.get()

    @Provides
    @Singleton
    fun provideFriendsRepository(
        impl: Lazy<FriendsRepositoryImpl>,
        mock: Lazy<MockFriendsRepository>
    ): FriendsRepository =
        if (BuildConfig.USE_MOCK_COMMUNITY_DATA) mock.get() else impl.get()

    @Provides
    @Singleton
    fun provideRequestsRepository(
        impl: Lazy<RequestsRepositoryImpl>,
        mock: Lazy<MockRequestsRepository>
    ): RequestsRepository =
        if (BuildConfig.USE_MOCK_COMMUNITY_DATA) mock.get() else impl.get()
}
