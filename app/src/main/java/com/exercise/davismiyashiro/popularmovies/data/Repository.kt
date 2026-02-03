package com.exercise.davismiyashiro.popularmovies.data

import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient
import kotlinx.coroutines.flow.Flow

interface Repository {
    suspend fun loadMoviesFromNetwork(sortingOption: String): MovieDbApiClient.Result<Exception, List<MovieDetails>>

    fun loadMoviesFromDb(): Flow<List<MovieDetails>>

    fun getMovieFromDb(movieId: Int): Flow<MovieDetails?>

    fun getFavoriteMoviesIds(): Flow<Set<Int>>

    fun findTrailersByMovieId(movieId: Int): Flow<List<Trailer>>

    fun findReviewsByMovieId(movieId: Int): Flow<List<Review>>

    suspend fun insertMovieDb(movieDetails: MovieDetails)

    suspend fun deleteMovieDb(movieDetails: MovieDetails)
}