import app.cash.turbine.test
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails
import com.exercise.davismiyashiro.popularmovies.data.MovieRepository
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient
import com.exercise.davismiyashiro.popularmovies.movies.FAVORITES_PARAM
import com.exercise.davismiyashiro.popularmovies.movies.MovieListState
import com.exercise.davismiyashiro.popularmovies.movies.MoviesViewModel
import com.exercise.davismiyashiro.popularmovies.movies.POPULARITY_DESC_PARAM
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okio.IOException
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class MoviesViewModelTest {

    @get:Rule
    val coroutinesTestRule = MainDispatcherRule()

    lateinit var moviesViewModel: MoviesViewModel
    private lateinit var testDispatcher: TestDispatcher

    @Mock
    private lateinit var repository: MovieRepository

    val fakeMovies = listOf(
        MovieDetails(
            movieid = 1, title = "Fake",
            backdropPath = "Fake",
            posterPath = "Fake",
            overview = "Fake",
            releaseDate = "Fake",
            voteAverage = 0.1
        )
    )

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        `when`(repository.getFavoriteMoviesIds()).thenReturn(flowOf(emptySet()))
    }

    @Test
    fun load_popular_movies_calls_remote_success() = runTest {
        `when`(repository.getFavoriteMoviesIds()).thenReturn(flowOf(emptySet()))
        `when`(repository.loadMoviesFromNetwork(POPULARITY_DESC_PARAM))
            .thenReturn(MovieDbApiClient.Result.Success(fakeMovies))

        moviesViewModel = MoviesViewModel(repository)

        moviesViewModel.uiState.test {
            assertEquals(MovieListState.Loading, awaitItem()) // initial state
            val successState = awaitItem()
            assertTrue(successState is MovieListState.Success)
            assertEquals(1, (successState as MovieListState.Success).movieList.size)

            verify(repository).loadMoviesFromNetwork(POPULARITY_DESC_PARAM)
            verify(repository, never()).loadMoviesFromDb()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun load_popular_movies_calls_remote_error() = runTest(testDispatcher) {
        `when`(repository.loadMoviesFromNetwork(POPULARITY_DESC_PARAM)).thenReturn(
            MovieDbApiClient.Result.Error(IOException("Error loading popular movies"))
        )

        moviesViewModel = MoviesViewModel(repository)

        moviesViewModel.uiState.test {
            assertEquals(MovieListState.Loading, awaitItem()) // initial state
            val errorState = awaitItem()
            assertTrue(errorState is MovieListState.Error)
            Assert.assertEquals(
                MovieListState.Error(
                    message = "Error loading popular movies"
                ), moviesViewModel.uiState.value
            )

            verify(repository, times(1)).loadMoviesFromNetwork(POPULARITY_DESC_PARAM)
            verify(repository, never()).loadMoviesFromDb()
        }
    }

    @Test
    fun load_favorite_movies_calls_db() = runTest(testDispatcher) {
        val response = listOf<MovieDetails>()
        `when`(repository.loadMoviesFromNetwork(POPULARITY_DESC_PARAM))
            .thenReturn(MovieDbApiClient.Result.Success(fakeMovies))
        `when`(repository.loadMoviesFromDb()).thenReturn(flowOf(response))
        moviesViewModel = MoviesViewModel(repository)

        moviesViewModel.uiState.test {
            assertEquals(MovieListState.Loading, awaitItem()) // initial state
            val successState = awaitItem()
            assertTrue(successState is MovieListState.Success)
            assertEquals(1, (successState as MovieListState.Success).movieList.size)
            verify(repository, times(1)).loadMoviesFromNetwork(POPULARITY_DESC_PARAM)

            moviesViewModel.loadMovieListBySortingOption(FAVORITES_PARAM)

            val dbSuccess = awaitItem()
            assertTrue(dbSuccess is MovieListState.Success)
            verify(repository, times(1)).loadMoviesFromDb()
        }
    }

    @ExperimentalCoroutinesApi
    class MainDispatcherRule(
        private val dispatcher: TestDispatcher = StandardTestDispatcher()
    ) : TestWatcher() {
        override fun starting(description: Description) {
            Dispatchers.setMain(dispatcher)
        }

        override fun finished(description: Description) {
            Dispatchers.resetMain()
        }
    }
}