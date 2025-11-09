package com.dms.flip.di

import com.dms.flip.data.firebase.source.FirestoreProfileSource
import com.dms.flip.data.firebase.source.FirestoreFriendsSource
import com.dms.flip.data.firebase.source.FirestoreSearchSource
import com.dms.flip.data.firebase.source.FirestoreSuggestionsSource
import com.dms.flip.data.firebase.source.FriendsSource
import com.dms.flip.data.firebase.source.ProfileSource
import com.dms.flip.data.firebase.source.SearchSource
import com.dms.flip.data.firebase.source.SuggestionsSource
import com.dms.flip.data.mock.community.MockableFeedRepository
import com.dms.flip.data.mock.community.MockableFriendsRepository
import com.dms.flip.data.mock.community.MockableSearchRepository
import com.dms.flip.data.mock.community.MockableSuggestionsRepository
import com.dms.flip.domain.repository.community.FeedRepository
import com.dms.flip.domain.repository.community.FriendsRepository
import com.dms.flip.domain.repository.community.SearchRepository
import com.dms.flip.domain.repository.community.SuggestionsRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
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
    fun provideFriendsRequestsSource(
        firestore: FirebaseFirestore,
        firebaseFunctions: FirebaseFunctions
    ): FriendsSource = FirestoreFriendsSource(firestore, firebaseFunctions)

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
    fun provideFeedRepository(repository: MockableFeedRepository): FeedRepository = repository

    @Provides
    @Singleton
    fun provideSuggestionsRepository(
        repository: MockableSuggestionsRepository
    ): SuggestionsRepository = repository

    @Provides
    @Singleton
    fun provideSearchRepository(
        repository: MockableSearchRepository
    ): SearchRepository = repository

    @Provides
    @Singleton
    fun provideRequestsRepository(
        repository: MockableFriendsRepository
    ): FriendsRepository = repository
}
