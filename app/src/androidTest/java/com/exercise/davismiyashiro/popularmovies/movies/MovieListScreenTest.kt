package com.exercise.davismiyashiro.popularmovies.movies

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.exercise.davismiyashiro.popularmovies.R
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsObservable
import com.exercise.davismiyashiro.popularmovies.ui.theme.PopularMoviesTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalTestApi::class)
class MovieListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun moviesTopAppBar_callsOnSortChanged_whenHighestRatingsSelected() {
        var selectedSort: String? = null

        composeTestRule.setContent {
            PopularMoviesTheme(dynamicColor = false) {
                MoviesTopAppBar(
                    currentSortOption = POPULARITY_DESC_PARAM,
                    onSortChanged = { selectedSort = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Popular Movies").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.onNodeWithText("Highest Ratings").performClick()

        assertEquals(HIGHEST_RATED_PARAM, selectedSort)
    }

    @Test
    fun movieGridItem_callsOnMovieClick_whenItemTapped() {
        var clickedMovie: MovieDetailsObservable? = null
        val movie = sampleMovieDetailsObservable()

        composeTestRule.setContent {
            PopularMoviesTheme(dynamicColor = false) {
                MovieGridItem(
                    movie = movie,
                    onMovieClick = { clickedMovie = it },
                    modifier = Modifier.testTag("movie_grid_item")
                )
            }
        }

        composeTestRule.onNodeWithTag("movie_grid_item").performClick()

        assertEquals(movie, clickedMovie)
    }

    @Test
    fun movieGridItem_displaysPosterImage_whenPosterLoads() {
        val movie = sampleMovieDetailsObservable(
            title = "The First Movie",
            posterPath = resourcePosterUri()
        )

        composeTestRule.setContent {
            PopularMoviesTheme(dynamicColor = false) {
                MovieGridItem(
                    movie = movie,
                    onMovieClick = {},
                    modifier = Modifier.testTag("movie_grid_item")
                )
            }
        }

        composeTestRule.waitUntilAtLeastOneExists(hasContentDescription(movie.title), 5_000)
        composeTestRule.onNodeWithContentDescription(movie.title).assertIsDisplayed()
    }

    @Test
    fun movieGridItem_showsErrorIcon_whenPosterLoadFails() {
        val movie = sampleMovieDetailsObservable(
            posterPath = "https://example.invalid/poster.jpg"
        )

        composeTestRule.setContent {
            PopularMoviesTheme(dynamicColor = false) {
                MovieGridItem(
                    movie = movie,
                    onMovieClick = {}
                )
            }
        }

        composeTestRule.waitUntilAtLeastOneExists(hasContentDescription("Error loading image"), 5_000)
        composeTestRule.onNodeWithContentDescription("Error loading image").assertIsDisplayed()
    }

    @Test
    fun moviesTopAppBar_showsFavoritesTitle_whenFavoritesSelected() {
        composeTestRule.setContent {
            PopularMoviesTheme(dynamicColor = false) {
                MoviesTopAppBar(
                    currentSortOption = FAVORITES_PARAM,
                    onSortChanged = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Favorites").assertIsDisplayed()
    }

    private fun sampleMovieDetailsObservable(
        id: Int = 123,
        title: String = "Interstellar",
        posterPath: String = "https://example.com/poster.jpg"
    ) = MovieDetailsObservable(
        id = id,
        title = title,
        backdropPath = "https://example.com/backdrop.jpg",
        posterPath = posterPath,
        overview = "A test overview",
        releaseDate = "2014-11-07",
        voteAverage = 8.6
    )

    private fun resourcePosterUri(): String {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return "android.resource://${context.packageName}/${R.drawable.poster}"
    }
}
