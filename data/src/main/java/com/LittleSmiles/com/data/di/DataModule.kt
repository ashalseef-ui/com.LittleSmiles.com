package com.LittleSmiles.com.data.di

import android.content.Context
import com.LittleSmiles.com.core.domain.repository.*
import com.LittleSmiles.com.data.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindBillingRepository(impl: BillingRepositoryImpl): BillingRepository

    @Binds
    @Singleton
    abstract fun bindContentRepository(impl: ContentRepositoryImpl): ContentRepository

    companion object {
        @Provides
        @Singleton
        fun provideProgressRepository(@ApplicationContext context: Context): ProgressRepository = 
            ProgressRepositoryImpl(context)

        @Provides
        @Singleton
        fun provideDevPreferencesRepository(@ApplicationContext context: Context): DevPreferencesRepository = 
            DevPreferencesRepository(context)
    }
}
