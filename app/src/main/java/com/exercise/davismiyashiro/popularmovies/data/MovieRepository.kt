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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDao
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient.ApiException
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient.NetworkException
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient.UnexpectedApiException
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

/**
 * Created by Davis Miyashiro.
 */

class MovieRepository @Inject constructor(
    private val theMovieDb: TheMovieDb,
    private val moviesDao: MoviesDao
) :
    Repository {

    override suspend fun loadMoviesFromNetwork(sortingOption: String): MovieDbApiClient.Result<Exception, List<MovieDetails>> {
        val moviesResponse = apiCall(
            call = { theMovieDb.getPopular(sortingOption) },
            errorMessage = "Error Fetching Movies"
        )

        return moviesResponse.map { moviesResponse ->
            moviesResponse.results
        }
    }

    override suspend fun loadMoviesFromDb(): MovieDbApiClient.Result<Exception, List<MovieDetails>> {
        return try {
            val movies = moviesDao.getAllMovies()
            MovieDbApiClient.Result.Success(movies)
        } catch (e: Exception) {
            MovieDbApiClient.Result.Error(e)
        }
    }

    override fun getMovieFromDb(movieId: Int): LiveData<MovieDetails> {
        return moviesDao.getMovieById(movieId)
    }

    override suspend fun findTrailersByMovieId(movieId: Int?): LiveData<List<Trailer>> {

        val trailersObservable = MediatorLiveData<List<Trailer>>()

        val trailersResponse = apiCall(
            call = { theMovieDb.getTrailers(movieId.toString()) },
            errorMessage = "Error Fetching Trailers"
        )

        val result = trailersResponse.map { response -> response.results }

        result.fold(
            success = {
                if (it.isNotEmpty()) {
                    trailersObservable.postValue(it)
                }
            },
            ex = {
                Timber.e(it)
            }
        )
        return trailersObservable
    }

    override suspend fun findReviewsByMovieId(movieId: Int?): LiveData<List<Review>> {

        val reviewsObservable = MediatorLiveData<List<Review>>()

        val reviewsResponse = apiCall(
            call = { theMovieDb.getReviews(movieId.toString()) },
            errorMessage = "Error Fetching Reviews"
        )

        val result = reviewsResponse.map { response -> response.results }

        result.fold(
            success = {
                if (it.isNotEmpty()) {
                    reviewsObservable.postValue(it)
                }
            },
            ex = {
                Timber.e(it)
            }
        )
        return reviewsObservable
    }

    override suspend fun insertMovieDb(movieDetails: MovieDetails) {
        moviesDao.insert(movieDetails)
        loadMoviesFromDb()
    }

    override suspend fun deleteMovieDb(movieDetails: MovieDetails) {
        moviesDao.deleteMovies(movieDetails)
        loadMoviesFromDb()
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
