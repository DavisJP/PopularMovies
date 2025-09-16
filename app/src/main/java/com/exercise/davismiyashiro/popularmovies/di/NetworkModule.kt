package com.exercise.davismiyashiro.popularmovies.di

import android.content.Context
import androidx.room.Room
import com.exercise.davismiyashiro.popularmovies.BuildConfig
import com.exercise.davismiyashiro.popularmovies.data.Repository
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDao
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDb
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient.MovieRepository
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

private const val API_KEY_PARAM = "api_key"
private const val THEMOVIEDB_API = "https://api.themoviedb.org"
private const val DATABASE_NAME = "moviesdatabase.db"

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

    @Provides
    @Singleton
    @Named("BaseUrl")
    fun baseUrl() = THEMOVIEDB_API


    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
            val newUrl = chain.request().url
                .newBuilder()
                .addQueryParameter(API_KEY_PARAM, BuildConfig.API_KEY)
                .build()

            val newRequest = chain.request()
                .newBuilder()
                .url(newUrl)
                .build()
            chain.proceed(newRequest)
        }

        val logging = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            logging.level = HttpLoggingInterceptor.Level.BODY
        } else {
            logging.level = HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        @Named("BaseUrl") baseUrl: String
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): TheMovieDb {
        return retrofit.create(TheMovieDb::class.java)
    }

    @Provides
    @Singleton
    fun provideRemoteRepository(
        theMovieDb: TheMovieDb,
        moviesDao: MoviesDao,
    ): MovieRepository {
        return Repository(theMovieDb, moviesDao)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): MoviesDb {
        return Room.databaseBuilder(
            appContext,
            MoviesDb::class.java,
            DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideMoviesDao(appDatabase: MoviesDb): MoviesDao {
        return appDatabase.moviesDao()
    }
}