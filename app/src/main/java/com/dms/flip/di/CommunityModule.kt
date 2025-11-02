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
import com.dms.flip.data.mock.community.MockFriendsRepository
import com.dms.flip.data.mock.community.MockRequestsRepository
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
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.Lazy
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CommunityModule {

    @Binds
    @Singleton
    abstract fun bindFeedRepository(impl: FeedRepositoryImpl): FeedRepository

    @Binds
    @Singleton
    abstract fun bindSuggestionsRepository(impl: SuggestionsRepositoryImpl): SuggestionsRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    companion object {
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
}
