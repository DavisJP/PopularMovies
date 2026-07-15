package com.exercise.davismiyashiro.popularmovies.movies

import app.cash.turbine.test
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails
import com.exercise.davismiyashiro.popularmovies.data.MovieRepository
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesViewModelTest {

    @get:Rule
    val coroutinesTestRule = MainDispatcherRule()

    lateinit var moviesViewModel: MoviesViewModel
    private lateinit var testDispatcher: TestDispatcher

    private val repository: MovieRepository = mockk()

    val fakeMovies = listOf(
        MovieDetails(
            movieid = 1,
            title = "Fake",
            backdropPath = "Fake",
            posterPath = "Fake",
            overview = "Fake",
            releaseDate = "Fake",
            voteAverage = 0.1,
        ),
    )

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        every { repository.getFavoriteMoviesIds() } returns flowOf(emptySet())
    }

    @Test
    fun load_popular_movies_calls_remote_success() = runTest {
        every { repository.getFavoriteMoviesIds() } returns flowOf(emptySet())
        coEvery { repository.loadMoviesFromNetwork(POPULARITY_DESC_PARAM) } returns
            MovieDbApiClient.Result.Success(fakeMovies)

        moviesViewModel = MoviesViewModel(repository)

        moviesViewModel.uiState.test {
            TestCase.assertEquals(MovieListState.Loading, awaitItem()) // initial state
            val successState = awaitItem()
            TestCase.assertTrue(successState is MovieListState.Success)
            TestCase.assertEquals(1, (successState as MovieListState.Success).movieList.size)

            coVerify { repository.loadMoviesFromNetwork(POPULARITY_DESC_PARAM) }
            verify(exactly = 0) { repository.loadMoviesFromDb() }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun load_popular_movies_calls_remote_error() = runTest(testDispatcher) {
        coEvery { repository.loadMoviesFromNetwork(POPULARITY_DESC_PARAM) } returns
            MovieDbApiClient.Result.Error(okio.IOException("Error loading popular movies"))

        moviesViewModel = MoviesViewModel(repository)

        moviesViewModel.uiState.test {
            TestCase.assertEquals(MovieListState.Loading, awaitItem()) // initial state
            val errorState = awaitItem()
            TestCase.assertTrue(errorState is MovieListState.Error)
            Assert.assertEquals(
                MovieListState.Error(
                    message = "Error loading popular movies",
                ),
                moviesViewModel.uiState.value,
            )

            coVerify(exactly = 1) {
                repository.loadMoviesFromNetwork(POPULARITY_DESC_PARAM)
            }
            verify(exactly = 0) { repository.loadMoviesFromDb() }
        }
    }

    @Test
    fun load_favorite_movies_calls_db() = runTest(testDispatcher) {
        val response = listOf<MovieDetails>()
        coEvery { repository.loadMoviesFromNetwork(POPULARITY_DESC_PARAM) } returns
            MovieDbApiClient.Result.Success(fakeMovies)
        every { repository.loadMoviesFromDb() } returns flowOf(response)
        moviesViewModel = MoviesViewModel(repository)

        moviesViewModel.uiState.test {
            TestCase.assertEquals(MovieListState.Loading, awaitItem()) // initial state
            val successState = awaitItem()
            TestCase.assertTrue(successState is MovieListState.Success)
            TestCase.assertEquals(1, (successState as MovieListState.Success).movieList.size)
            coVerify(exactly = 1) {
                repository.loadMoviesFromNetwork(POPULARITY_DESC_PARAM)
            }

            moviesViewModel.loadMovieListBySortingOption(FAVORITES_PARAM)

            val dbSuccess = awaitItem()
            TestCase.assertTrue(dbSuccess is MovieListState.Success)
            verify(exactly = 1) { repository.loadMoviesFromDb() }
        }
    }

    @Test
    fun selecting_same_sort_option_does_not_reload_movies() = runTest(testDispatcher) {
        coEvery { repository.loadMoviesFromNetwork(POPULARITY_DESC_PARAM) } returns
            MovieDbApiClient.Result.Success(fakeMovies)

        moviesViewModel = MoviesViewModel(repository)

        moviesViewModel.uiState.test {
            TestCase.assertEquals(MovieListState.Loading, awaitItem())
            awaitItem()

            moviesViewModel.loadMovieListBySortingOption(POPULARITY_DESC_PARAM)

            expectNoEvents()
            coVerify(exactly = 1) {
                repository.loadMoviesFromNetwork(POPULARITY_DESC_PARAM)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @ExperimentalCoroutinesApi
    class MainDispatcherRule(
        private val dispatcher: TestDispatcher = StandardTestDispatcher(),
    ) : TestWatcher() {
        override fun starting(description: Description) {
            Dispatchers.setMain(dispatcher)
        }

        override fun finished(description: Description) {
            Dispatchers.resetMain()
        }
    }
}
