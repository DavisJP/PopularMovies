package com.exercise.davismiyashiro.popularmovies.moviedetails

import app.cash.turbine.test
import com.exercise.davismiyashiro.popularmovies.R
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails
import com.exercise.davismiyashiro.popularmovies.data.Repository
import com.exercise.davismiyashiro.popularmovies.data.Review
import com.exercise.davismiyashiro.popularmovies.data.Trailer
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailsViewModelTest {

    @get:Rule
    val coroutinesTestRule = MainDispatcherRule()

    private lateinit var viewModel: MovieDetailsViewModel

    @Mock
    private lateinit var repository: Repository

    private val movieId = 123
    private val movieDetailsObservable = MovieDetailsObservable(
        id = movieId,
        title = "Test Movie",
        backdropPath = "/backdrop.jpg",
        posterPath = "/poster.jpg",
        overview = "Test Overview",
        releaseDate = "2023-01-01",
        voteAverage = 8.5,
    )

    @Before
    fun setup() {
        viewModel = MovieDetailsViewModel(repository)
    }

    @Test
    fun `reviews returns list from repository`() = runTest(coroutinesTestRule.testDispatcher) {
        val reviews = listOf(Review("1", "Author", "Content", "url"))
        `when`(repository.findReviewsByMovieId(movieId)).thenReturn(reviews)

        val result = viewModel.reviews(movieId)

        assertEquals(reviews, result)
        verify(repository).findReviewsByMovieId(movieId)
    }

    @Test
    fun `trailers returns list from repository`() = runTest(coroutinesTestRule.testDispatcher) {
        val trailers = listOf(Trailer("1", "en", "US", "Key", "Name", "Site", 1080, "Type"))
        `when`(repository.findTrailersByMovieId(movieId)).thenReturn(trailers)

        val result = viewModel.trailers(movieId)

        assertEquals(trailers, result)
        verify(repository).findTrailersByMovieId(movieId)
    }

    @Test
    fun `isFavorite returns true when movie exists in db`() = runTest(coroutinesTestRule.testDispatcher) {
        val movieDetails = MovieDetails(movieId, "Title", "backdrop", "poster", "overview", "date", 8.5)
        `when`(repository.getMovieFromDb(movieId)).thenReturn(flowOf(movieDetails))

        viewModel.isFavorite(movieId).test {
            assertEquals(true, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isFavorite returns false when movie does not exist in db`() = runTest(coroutinesTestRule.testDispatcher) {
        `when`(repository.getMovieFromDb(movieId)).thenReturn(flowOf(null))

        viewModel.isFavorite(movieId).test {
            assertEquals(false, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setFavorite deletes movie and emits toast when already favorite`() = runTest(coroutinesTestRule.testDispatcher) {
        viewModel.toastMessageEvents.test {
            viewModel.setFavorite(movieDetailsObservable, isFavorite = true)

            verify(repository).deleteMovieDb(
                MovieDetails(
                    movieid = movieDetailsObservable.id,
                    title = movieDetailsObservable.title,
                    backdropPath = movieDetailsObservable.backdropPath,
                    posterPath = movieDetailsObservable.posterPath,
                    overview = movieDetailsObservable.overview,
                    releaseDate = movieDetailsObservable.releaseDate,
                    voteAverage = movieDetailsObservable.voteAverage,
                )
            )
            assertEquals(R.string.movie_deleted_msg, awaitItem())
        }
    }

    @Test
    fun `setFavorite inserts movie and emits toast when not favorite`() = runTest(coroutinesTestRule.testDispatcher) {
        viewModel.toastMessageEvents.test {
            viewModel.setFavorite(movieDetailsObservable, isFavorite = false)

            verify(repository).insertMovieDb(
                MovieDetails(
                    movieid = movieDetailsObservable.id,
                    title = movieDetailsObservable.title,
                    backdropPath = movieDetailsObservable.backdropPath,
                    posterPath = movieDetailsObservable.posterPath,
                    overview = movieDetailsObservable.overview,
                    releaseDate = movieDetailsObservable.releaseDate,
                    voteAverage = movieDetailsObservable.voteAverage,
                )
            )
            assertEquals(R.string.movie_added_msg, awaitItem())
        }
    }

    class MainDispatcherRule(
        val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
    ) : TestWatcher() {
        override fun starting(description: Description) {
            Dispatchers.setMain(testDispatcher)
        }

        override fun finished(description: Description) {
            Dispatchers.resetMain()
        }
    }
}
