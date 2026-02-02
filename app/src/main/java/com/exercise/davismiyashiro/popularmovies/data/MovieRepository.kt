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

package com.exercise.davismiyashiro.popularmovies.data

import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDao
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient.ApiException
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient.NetworkException
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient.UnexpectedApiException
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

/**
 * Created by Davis Miyashiro.
 */

class MovieRepository @Inject constructor(
    private val theMovieDbLazy: Lazy<TheMovieDb>,
    private val moviesDao: MoviesDao
) :
    Repository {

    private val theMovieDb: TheMovieDb by lazy {
        theMovieDbLazy.get()
    }

    override suspend fun loadMoviesFromNetwork(sortingOption: String): MovieDbApiClient.Result<Exception, List<MovieDetails>> {
        val moviesResponse = apiCall(
            call = { theMovieDb.getPopular(sortingOption) },
            errorMessage = "Error Fetching Movies"
        )

        return moviesResponse.map { moviesResponse ->
            moviesResponse.results
        }
    }

    override fun loadMoviesFromDb(): Flow<List<MovieDetails>> {
        return moviesDao.getAllMovies()
    }

    override fun getMovieFromDb(movieId: Int): Flow<MovieDetails?> {
        return moviesDao.getMovieById(movieId)
    }

    override fun getFavoriteMoviesIds(): Flow<Set<Int>> =
        moviesDao.getFavoriteMoviesIds().map { it.toSet() }

    override fun findTrailersByMovieId(movieId: Int): Flow<List<Trailer>> = flow {
        val trailersResponse = apiCall(
            call = { theMovieDb.getTrailers(movieId.toString()) },
            errorMessage = "Error Fetching Trailers"
        )

        emit(
            trailersResponse.fold(
                success = {
                    it.results
                },
                ex = {
                    Timber.e(it)
                    emptyList()
                }
            ))
    }.flowOn(Dispatchers.IO)

    override fun findReviewsByMovieId(movieId: Int): Flow<List<Review>> = flow {

        val reviewsResponse = apiCall(
            call = { theMovieDb.getReviews(movieId.toString()) },
            errorMessage = "Error Fetching Reviews"
        )

        emit(
            reviewsResponse.fold(
                success = {
                    it.results
                },
                ex = {
                    Timber.e(it)
                    emptyList()
                }
            )
        )
    }.flowOn(Dispatchers.IO)

    override suspend fun insertMovieDb(movieDetails: MovieDetails) {
        moviesDao.insert(movieDetails)
    }

    override suspend fun deleteMovieDb(movieDetails: MovieDetails) {
        moviesDao.deleteMovies(movieDetails)
    }

    private suspend fun <T : Any> apiCall(
        call: suspend () -> Response<T>,
        errorMessage: String
    ): MovieDbApiClient.Result<Exception, T> {
        try {
            val response = call()
            return if (response.isSuccessful)
                response.body()?.let { body ->
                    MovieDbApiClient.Result.Success(body)
                } ?: MovieDbApiClient.Result.Error(ApiException(errorMessage))
            else {
                when (response.code()) {
                    401 -> MovieDbApiClient.Result.Error(ApiException(errorMessage.plus("onRequestUnauthenticated: ${response.message()}")))
                    in 400..499 -> MovieDbApiClient.Result.Error(ApiException(errorMessage.plus("onRequestClientError: ${response.message()}")))
                    in 500..599 -> MovieDbApiClient.Result.Error(ApiException(errorMessage.plus("onRequestServerError: ${response.message()}")))
                    else -> MovieDbApiClient.Result.Error(ApiException(errorMessage.plus("UnknownError: ${response.code()} ${response.message()}")))
                }
            }
        } catch (e: IOException) {
            return MovieDbApiClient.Result.Error(NetworkException(errorMessage, e))
        } catch (e: Exception) {
            return MovieDbApiClient.Result.Error(UnexpectedApiException(errorMessage, e))
        }
    }
}
