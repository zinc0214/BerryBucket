package com.zinc.datastore.di

import android.content.Context
import com.zinc.datastore.login.LoginPreferenceDataStoreModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DataStoreModule {
    @Provides
    @Singleton
    fun loginPreferenceDataStoreModule(@ApplicationContext appContext: Context): LoginPreferenceDataStoreModule =
        LoginPreferenceDataStoreModule(appContext)
}