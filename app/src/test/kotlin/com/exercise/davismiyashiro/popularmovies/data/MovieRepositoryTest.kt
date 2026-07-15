package com.exercise.davismiyashiro.popularmovies.data

import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDao
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb
import dagger.Lazy
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class MovieRepositoryTest {

    private val theMovieDb: TheMovieDb = mockk()
    private val moviesDao: MoviesDao = mockk()
    private val theMovieDbLazy: Lazy<TheMovieDb> = mockk {
        every { get() } returns theMovieDb
    }

    private lateinit var repository: MovieRepository

    private val movieId = 123
    private val movieDetails = MovieDetails(
        movieid = movieId,
        title = "Test Movie",
        backdropPath = "/backdrop.jpg",
        posterPath = "/poster.jpg",
        overview = "Test Overview",
        releaseDate = "2023-01-01",
        voteAverage = 8.5,
    )

    @Before
    fun setup() {
        repository = MovieRepository(theMovieDbLazy, moviesDao)
    }

    @Test
    fun `loadMoviesFromNetwork returns success result when api succeeds`() = runTest {
        val moviesResponse = MovieResponse(results = listOf(movieDetails))
        coEvery { theMovieDb.getPopular(any()) } returns moviesResponse

        val result = repository.loadMoviesFromNetwork("popular")

        assertTrue(result is MovieDbApiClient.Result.Success)
        assertEquals(listOf(movieDetails), (result as MovieDbApiClient.Result.Success).data)
    }

    @Test
    fun `loadMoviesFromNetwork returns Error with NetworkException when IOException is thrown`() = runTest {
        coEvery { theMovieDb.getPopular(any()) } throws IOException("No internet")

        val result = repository.loadMoviesFromNetwork("popular")

        assertTrue(result is MovieDbApiClient.Result.Error)
        assertTrue((result as MovieDbApiClient.Result.Error).exception is MovieDbApiClient.NetworkException)
    }

    @Test
    fun `loadMoviesFromNetwork returns Error with ApiException when 401 HttpException is thrown`() = runTest {
        val errorResponse = Response.error<MovieResponse<List<MovieDetails>>>(401, "".toResponseBody())
        coEvery { theMovieDb.getPopular(any()) } throws HttpException(errorResponse)

        val result = repository.loadMoviesFromNetwork("popular")

        assertTrue(result is MovieDbApiClient.Result.Error)
        assertTrue((result as MovieDbApiClient.Result.Error).exception is MovieDbApiClient.ApiException)
        assertTrue(result.exception.message?.contains("401") == true)
    }

    @Test
    fun `loadMoviesFromNetwork returns Error with UnexpectedApiException on unknown exception`() = runTest {
        coEvery { theMovieDb.getPopular(any()) } throws RuntimeException("Unexpected")

        val result = repository.loadMoviesFromNetwork("popular")

        assertTrue(result is MovieDbApiClient.Result.Error)
        assertTrue((result as MovieDbApiClient.Result.Error).exception is MovieDbApiClient.UnexpectedApiException)
    }

    @Test
    fun `loadMoviesFromDb calls dao`() = runTest {
        val movies = listOf(movieDetails)
        every { moviesDao.getAllMovies() } returns flowOf(movies)

        val result = repository.loadMoviesFromDb().first()

        assertEquals(movies, result)
        coVerify { moviesDao.getAllMovies() }
    }

    @Test
    fun `getMovieFromDb calls dao`() = runTest {
        every { moviesDao.getMovieById(movieId) } returns flowOf(movieDetails)

        val result = repository.getMovieFromDb(movieId).first()

        assertEquals(movieDetails, result)
        coVerify { moviesDao.getMovieById(movieId) }
    }

    @Test
    fun `getFavoriteMoviesIds returns set of ids`() = runTest {
        val ids = listOf(1, 2, 3)
        every { moviesDao.getFavoriteMoviesIds() } returns flowOf(ids)

        val result = repository.getFavoriteMoviesIds().first()

        assertEquals(setOf(1, 2, 3), result)
    }

    @Test
    fun `findTrailersByMovieId returns list on success`() = runTest {
        val trailers = listOf(Trailer("1", "en", "US", "Key", "Name", "Site", 1080, "Type"))
        val response = MovieResponse(results = trailers)
        coEvery { theMovieDb.getTrailers(movieId.toString()) } returns response

        val result = repository.findTrailersByMovieId(movieId)

        assertEquals(trailers, result)
    }

    @Test
    fun `findTrailersByMovieId returns empty list on exception`() = runTest {
        coEvery { theMovieDb.getTrailers(any()) } throws IOException()

        val result = repository.findTrailersByMovieId(movieId)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `findReviewsByMovieId returns list on success`() = runTest {
        val reviews = listOf(Review("1", "Author", "Content", "url"))
        val response = MovieResponse(results = reviews)
        coEvery { theMovieDb.getReviews(movieId.toString()) } returns response

        val result = repository.findReviewsByMovieId(movieId)

        assertEquals(reviews, result)
    }

    @Test
    fun `insertMovieDb calls dao`() = runTest {
        coEvery { moviesDao.insert(any()) } returns Unit

        repository.insertMovieDb(movieDetails)

        coVerify { moviesDao.insert(movieDetails) }
    }

    @Test
    fun `deleteMovieDb calls dao`() = runTest {
        coEvery { moviesDao.deleteMovies(any()) } returns Unit

        repository.deleteMovieDb(movieDetails)

        coVerify { moviesDao.deleteMovies(movieDetails) }
    }
}
