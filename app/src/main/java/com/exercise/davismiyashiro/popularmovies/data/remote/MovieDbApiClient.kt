/*
 * MIT License
 *
 * Copyright (c) 2019 Davis Miyashiro
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.exercise.davismiyashiro.popularmovies.data.remote

import androidx.lifecycle.LiveData
import com.exercise.davismiyashiro.popularmovies.BuildConfig
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails
import com.exercise.davismiyashiro.popularmovies.data.Review
import com.exercise.davismiyashiro.popularmovies.data.Trailer
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException

private const val API_KEY_PARAM = "api_key"
private const val THEMOVIEDB_API = "https://api.themoviedb.org"

/**
 * Retrofit client
 *
 * Created by Davis Miyashiro on 20/02/2017.
 */
class MovieDbApiClient {

    private var service: TheMovieDb? = null

    private val authInterceptor = Interceptor { chain ->
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

    private val okHttp: OkHttpClient
        get() {
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

    fun getService(): TheMovieDb? {
        if (service == null) {
            val retrofitSingle = Retrofit.Builder()
                .baseUrl(THEMOVIEDB_API)
                .client(okHttp)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
            service = retrofitSingle.create(TheMovieDb::class.java)
        }
        return service
    }

    sealed class Result<out E, out S> {
        data class Error(val exception: Exception) : Result<Exception, Nothing>()
        data class Success<out S>(val data: S) : Result<Nothing, S>()

        fun <T> fold(ex: (Exception) -> T, success: (S) -> T): T {
            return when (this) {
                is Error -> ex(exception)
                is Success -> success(data)
            }
        }

        inline fun <T> map(transform: (S) -> T): Result<E, T> {
            return when (this) {
                is Error -> this
                is Success -> Success(transform(data))
            }
        }
    }

    class ApiException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
    class NetworkException(message: String, cause: Throwable? = null) : IOException(message, cause)
    class UnexpectedApiException(message: String, cause: Throwable? = null) :
        RuntimeException(message, cause)

    interface MovieRepository {
        suspend fun loadMoviesFromNetwork(sortingOption: String): Result<Exception, List<MovieDetails>>

        suspend fun loadMoviesFromDb(): Result<Exception, List<MovieDetails>>

        fun getMovieFromDb(movieId: Int): LiveData<MovieDetails>

        suspend fun findTrailersByMovieId(movieId: Int?): LiveData<List<Trailer>>

        suspend fun findReviewsByMovieId(movieId: Int?): LiveData<List<Review>>

        suspend fun insertMovieDb(movieDetails: MovieDetails)

        suspend fun deleteMovieDb(movieDetails: MovieDetails)
    }
}
