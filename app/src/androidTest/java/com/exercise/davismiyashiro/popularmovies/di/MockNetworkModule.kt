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
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

private const val DATABASE_NAME = "moviesdatabase.db"
private const val API_KEY_PARAM = "api_key"
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NetworkModule::class]
)

class MockNetworkModule {
    @Provides
    @Singleton
    @Named("BaseUrl")
    fun baseUrl(): String {
        return "https://api.themoviedb.org"
    }

    @Provides
    @Singleton
    fun provideTestOkHttpClient(): OkHttpClient {
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
            .connectTimeout(5, TimeUnit.SECONDS) // Use short timeouts for tests
            .readTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideTestRetrofit(
        okHttpClient: OkHttpClient, // From this module
        @Named("BaseUrl") baseUrl: String // From this module
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create()) // Or your preferred converter
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideTestApiService(retrofit: Retrofit): TheMovieDb { // From this module
        return retrofit.create(TheMovieDb::class.java)
    }

    @Provides
    @Singleton
    fun provideLocalRepository(@ApplicationContext appContext: Context): MoviesDb {
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

    @Provides
    @Singleton
    fun provideRemoteRepository(
        theMovieDb: TheMovieDb,
        moviesDao: MoviesDao,
    ): MovieRepository {
        return Repository(theMovieDb, moviesDao)
    }
}