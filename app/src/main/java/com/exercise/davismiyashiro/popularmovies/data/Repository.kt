package com.exercise.davismiyashiro.popularmovies.data

import androidx.lifecycle.LiveData
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient

interface Repository {
    suspend fun loadMoviesFromNetwork(sortingOption: String): MovieDbApiClient.Result<Exception, List<MovieDetails>>

    suspend fun loadMoviesFromDb(): MovieDbApiClient.Result<Exception, List<MovieDetails>>

    fun getMovieFromDb(movieId: Int): LiveData<MovieDetails>

    suspend fun findTrailersByMovieId(movieId: Int?): LiveData<List<Trailer>>

    suspend fun findReviewsByMovieId(movieId: Int?): LiveData<List<Review>>

    suspend fun insertMovieDb(movieDetails: MovieDetails)

    suspend fun deleteMovieDb(movieDetails: MovieDetails)
}