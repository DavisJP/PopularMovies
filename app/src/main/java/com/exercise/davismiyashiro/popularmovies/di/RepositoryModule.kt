package com.exercise.davismiyashiro.popularmovies.di

import com.exercise.davismiyashiro.popularmovies.data.MovieRepository
import com.exercise.davismiyashiro.popularmovies.data.Repository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Singleton
    @Binds
    abstract fun bindRepository(repository: MovieRepository): Repository

}