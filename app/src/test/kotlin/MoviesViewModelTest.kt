import com.exercise.davismiyashiro.popularmovies.data.MovieDetails
import com.exercise.davismiyashiro.popularmovies.data.MovieRepository
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient
import com.exercise.davismiyashiro.popularmovies.movies.FAVORITES_PARAM
import com.exercise.davismiyashiro.popularmovies.movies.MovieListState
import com.exercise.davismiyashiro.popularmovies.movies.MoviesViewModel
import com.exercise.davismiyashiro.popularmovies.movies.POPULARITY_DESC_PARAM
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okio.IOException
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class MoviesViewModelTest {

    lateinit var moviesViewModel: MoviesViewModel
    private lateinit var testDispatcher: TestDispatcher

    @Mock
    private lateinit var repository: MovieRepository

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        repository = mock(MovieRepository::class.java)
        moviesViewModel = MoviesViewModel(
            repository, testDispatcher, testDispatcher
        )
    }

    @Test
    fun load_popular_movies_calls_remote_success() = runTest(testDispatcher) {
        val response = listOf<MovieDetails>()
        `when`(repository.loadMoviesFromNetwork(POPULARITY_DESC_PARAM)).thenReturn(
            MovieDbApiClient.Result.Success(
                response
            )
        )
        moviesViewModel.loadMovieListBySortingOption()

        advanceUntilIdle()
        verify(repository, times(1)).loadMoviesFromNetwork(POPULARITY_DESC_PARAM)
        verify(repository, never()).loadMoviesFromDb()
    }

    @Test
    fun load_popular_movies_calls_remote_error() = runTest(testDispatcher) {
        `when`(repository.loadMoviesFromNetwork(POPULARITY_DESC_PARAM)).thenReturn(
            MovieDbApiClient.Result.Error(IOException("Error loading popular movies"))
        )
        moviesViewModel.loadMovieListBySortingOption()

        advanceUntilIdle()
        verify(repository, times(1)).loadMoviesFromNetwork(POPULARITY_DESC_PARAM)
        verify(repository, never()).loadMoviesFromDb()
        Assert.assertEquals(
            MovieListState.Error(
                message = "Error loading popular movies"
            ), moviesViewModel.uiState.value
        )
    }

    @Test
    fun load_favorite_movies_calls_db() = runTest(testDispatcher) {
        val response = listOf<MovieDetails>()
        `when`(repository.loadMoviesFromDb()).thenReturn(
            MovieDbApiClient.Result.Success(
                response
            )
        )
        moviesViewModel.loadMovieListBySortingOption(FAVORITES_PARAM)

        advanceUntilIdle()
        verify(repository, times(1)).loadMoviesFromDb()
        verify(repository, never()).loadMoviesFromNetwork(FAVORITES_PARAM)
    }

}