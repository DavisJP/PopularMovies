package com.exercise.davismiyashiro.popularmovies.data

import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient
import kotlinx.coroutines.flow.Flow

interface Repository {
    suspend fun loadMoviesFromNetwork(sortingOption: String): MovieDbApiClient.Result<Exception, List<MovieDetails>>

    fun loadMoviesFromDb(): Flow<List<MovieDetails>>

    fun getMovieFromDb(movieId: Int): Flow<MovieDetails?>

    fun getFavoriteMoviesIds(): Flow<Set<Int>>

    suspend fun findTrailersByMovieId(movieId: Int): List<Trailer>

    suspend fun findReviewsByMovieId(movieId: Int): List<Review>

    suspend fun insertMovieDb(movieDetails: MovieDetails)

    suspend fun deleteMovieDb(movieDetails: MovieDetails)
}