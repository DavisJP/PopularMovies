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
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient.BaseNetworkHandler
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb
import com.exercise.davismiyashiro.popularmovies.movies.FAVORITES_PARAM
import timber.log.Timber

/**
 * Created by Davis Miyashiro.
 */

class Repository(private val theMovieDb: TheMovieDb, private val moviesDao: MoviesDao) : BaseNetworkHandler() {

    suspend fun loadMoviesFromNetwork(sortingOption: String): LiveData<List<MovieDetails>> {

        val moviesObservable = MediatorLiveData<List<MovieDetails>>()

        val moviesResponse = apiCall(
                call = { theMovieDb.getPopular(sortingOption) },
                errorMessage = "Error Fetching Movies"
        )

        moviesObservable.postValue(moviesResponse?.results)

        return moviesObservable
    }

    fun loadMoviesFromDb(sortingOption: String): LiveData<List<MovieDetails>> {
        val moviesObservable = MediatorLiveData<List<MovieDetails>>()
        moviesObservable.addSource<List<MovieDetails>>(moviesDao.getAllMovies()) { values ->
            if (sortingOption == FAVORITES_PARAM) {
                moviesObservable.value = values
            }
        }
        return moviesObservable
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

        trailersObservable.postValue(trailersResponse?.results)
        return trailersObservable
    }

    suspend fun findReviewsByMovieId(movieId: Int?): LiveData<List<Review>> {

        val reviewsObservable = MediatorLiveData<List<Review>>()

        val reviewsResponse = apiCall(
                call = { theMovieDb.getReviews(movieId.toString()) },
                errorMessage = "Error Fetching Reviews"
        )

        if (reviewsResponse?.results != null) {
            val reviews = reviewsResponse.results
            if (reviews.isNotEmpty()) {
                reviewsObservable.postValue(reviews)
            }
        }

        return reviewsObservable
    }

    suspend fun insertMovieDb(movieDetails: MovieDetails) {
        moviesDao.insert(movieDetails)
        loadMoviesFromDb(FAVORITES_PARAM)
    }

    suspend fun deleteMovieDb(movieDetails: MovieDetails) {
        moviesDao.deleteMovies(movieDetails)
        loadMoviesFromDb(FAVORITES_PARAM)
    }
}
