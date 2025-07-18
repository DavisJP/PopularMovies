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
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient.BaseNetworkHandler
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb
import timber.log.Timber

/**
 * Created by Davis Miyashiro.
 */

class Repository(private val theMovieDb: TheMovieDb, private val moviesDao: MoviesDao) :
    BaseNetworkHandler() {

    suspend fun loadMoviesFromNetwork(sortingOption: String): MovieDbApiClient.Result<Exception, List<MovieDetails>> {
        val moviesResponse = apiCall(
            call = { theMovieDb.getPopular(sortingOption) },
            errorMessage = "Error Fetching Movies"
        )

        return moviesResponse.map { moviesResponse ->
            moviesResponse.results
        }
    }

    suspend fun loadMoviesFromDb(): MovieDbApiClient.Result<Exception, List<MovieDetails>> {
        return try {
            val movies = moviesDao.getAllMovies()
            MovieDbApiClient.Result.Success(movies)
        } catch (e: Exception) {
            MovieDbApiClient.Result.Error(e)
        }
    }

    fun getMovieFromDb(movieId: Int): LiveData<MovieDetails> {
        val moviesObservable = MediatorLiveData<MovieDetails>()

        moviesObservable.addSource(moviesDao.getMovieById(movieId)) { result ->
            if (result != null && result.movieid != 0) {
                Timber.e("moviesDao result: $result")
                moviesObservable.setValue(result)
            } else {
                Timber.e("moviesDao returned null")
                moviesObservable.setValue(null)
            }
        }
        return moviesObservable
    }

    suspend fun findTrailersByMovieId(movieId: Int?): LiveData<List<Trailer>> {

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

    suspend fun findReviewsByMovieId(movieId: Int?): LiveData<List<Review>> {

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

    suspend fun insertMovieDb(movieDetails: MovieDetails) {
        moviesDao.insert(movieDetails)
        loadMoviesFromDb()
    }

    suspend fun deleteMovieDb(movieDetails: MovieDetails) {
        moviesDao.deleteMovies(movieDetails)
        loadMoviesFromDb()
    }
}
