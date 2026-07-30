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
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb
import com.exercise.davismiyashiro.popularmovies.domain.ApiException
import com.exercise.davismiyashiro.popularmovies.domain.Movie
import com.exercise.davismiyashiro.popularmovies.domain.NetworkException
import com.exercise.davismiyashiro.popularmovies.domain.Repository
import com.exercise.davismiyashiro.popularmovies.domain.Result
import com.exercise.davismiyashiro.popularmovies.domain.Review
import com.exercise.davismiyashiro.popularmovies.domain.Trailer
import com.exercise.davismiyashiro.popularmovies.domain.UnexpectedApiException
import dagger.Lazy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

/**
 * Created by Davis Miyashiro.
 */

class MovieRepository @Inject constructor(
    private val theMovieDbLazy: Lazy<TheMovieDb>,
    private val moviesDao: MoviesDao,
) :
    Repository {

    private val theMovieDb: TheMovieDb by lazy {
        theMovieDbLazy.get()
    }

    override suspend fun loadMoviesFromNetwork(
        sortingOption: String,
    ): Result<Exception, List<Movie>> {
        val moviesResponse = apiCall(
            call = { theMovieDb.getPopular(sortingOption) },
            errorMessage = "Error Fetching Movies",
        )

        return moviesResponse.map { response ->
            response.results.map { it.toDomain() }
        }
    }

    override fun loadMoviesFromDb(): Flow<List<Movie>> {
        return moviesDao.getAllMovies().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getMovieFromDb(movieId: Int): Flow<Movie?> {
        return moviesDao.getMovieById(movieId).map { it?.toDomain() }
    }

    override fun getFavoriteMoviesIds(): Flow<Set<Int>> =
        moviesDao.getFavoriteMoviesIds().map { it.toSet() }

    override suspend fun findTrailersByMovieId(movieId: Int): List<Trailer> {
        val trailersResponse = apiCall(
            call = { theMovieDb.getTrailers(movieId.toString()) },
            errorMessage = "Error Fetching Trailers",
        )

        return trailersResponse.fold(
            success = {
                it.results.map { trailerDto -> trailerDto.toDomain() }
            },
            ex = {
                Timber.e(it)
                emptyList()
            },
        )
    }

    override suspend fun findReviewsByMovieId(movieId: Int): List<Review> {
        val reviewsResponse = apiCall(
            call = { theMovieDb.getReviews(movieId.toString()) },
            errorMessage = "Error Fetching Reviews",
        )

        return reviewsResponse.fold(
            success = {
                it.results.map { reviewDto -> reviewDto.toDomain() }
            },
            ex = {
                Timber.e(it)
                emptyList()
            },
        )
    }

    override suspend fun insertMovieDb(movie: Movie) {
        moviesDao.insert(movie.toEntity())
    }

    override suspend fun deleteMovieDb(movie: Movie) {
        moviesDao.deleteMovies(movie.toEntity())
    }

    private suspend fun <T : Any> apiCall(
        call: suspend () -> T,
        errorMessage: String,
    ): Result<Exception, T> {
        try {
            val response = call()
            return Result.Success(response)
        } catch (e: HttpException) {
            val errorResult = when (e.code()) {
                401 -> Result.Error(
                    ApiException(errorMessage.plus("onRequestUnauthenticated: ${e.message}")),
                )

                in 400..499 -> Result.Error(
                    ApiException(errorMessage.plus("onRequestClientError: ${e.message}")),
                )

                in 500..599 -> Result.Error(
                    ApiException(errorMessage.plus("onRequestServerError: ${e.message}")),
                )

                else -> Result.Error(
                    ApiException(errorMessage.plus("UnknownError: ${e.code()} ${e.message}")),
                )
            }
            return errorResult
        } catch (e: IOException) {
            return Result.Error(NetworkException(errorMessage, e))
        } catch (e: Exception) {
            return Result.Error(UnexpectedApiException(errorMessage, e))
        }
    }
}
