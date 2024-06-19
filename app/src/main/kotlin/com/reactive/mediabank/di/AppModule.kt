package com.reactive.mediabank.di

import android.content.ContentResolver
import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest
import com.reactive.mediabank.screens.data.repository.MediaRepositoryImpl
import com.reactive.mediabank.screens.domain.repository.MediaRepository
import com.reactive.mediabank.screens.domain.use_case.MediaUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideContentResolver(@ApplicationContext context : Context) : ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    fun provideMediaUseCases(
        repository : MediaRepository,
        @ApplicationContext context : Context,
    ) : MediaUseCases {
        return MediaUseCases(context, repository)
    }

    @Provides
    @Singleton
    fun provideMediaRepository(
        @ApplicationContext context : Context,
    ) : MediaRepository {
        return MediaRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun getImageLoader(@ApplicationContext context : Context) : ImageLoader = ImageLoader(context)

    @Provides
    fun getImageRequest(@ApplicationContext context : Context) : ImageRequest.Builder =
        ImageRequest.Builder(context)

}
