package com.exercise.davismiyashiro.popularmovies.moviedetails

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.exercise.davismiyashiro.popularmovies.data.Review
import com.exercise.davismiyashiro.popularmovies.data.Trailer
import com.exercise.davismiyashiro.popularmovies.ui.theme.PopularMoviesTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MovieDetailsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun movieDetailsContent_displaysMetadataTrailersAndReviews() {
        val movie = sampleMovieDetailsObservable()

        composeTestRule.setContent {
            PopularMoviesTheme(dynamicColor = false) {
                MovieDetailsContent(
                    modifier = Modifier.testTag("movie_details_content"),
                    movieDetails = movie,
                    trailers = persistentListOf(sampleTrailer()),
                    reviews = persistentListOf(sampleReview()),
                    isFavorite = false,
                    onFavoriteToggle = {},
                    onTrailerClick = {},
                    onReviewClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText(movie.title).assertIsDisplayed()
        composeTestRule.onNodeWithText("Release date: ${movie.releaseDate}").assertIsDisplayed()
        composeTestRule.onNodeWithText("Average rating: ${movie.voteAverage}").assertIsDisplayed()
        composeTestRule.onNodeWithText("Trailers:").assertIsDisplayed()
        composeTestRule.onNodeWithText("Official Trailer").assertIsDisplayed()

        composeTestRule.onNodeWithTag("movie_details_content")
            .performScrollToNode(hasText("A Reviewer"))

        composeTestRule.onNodeWithText("Reviews:").assertIsDisplayed()
        composeTestRule.onNodeWithText("A Reviewer").assertIsDisplayed()
        composeTestRule.onNodeWithText("A detailed review for the movie.").assertIsDisplayed()
    }

    @Test
    fun movieDetailsContent_callsOnFavoriteToggle_whenFavoriteIconTapped() {
        var favoriteToggleCount = 0

        composeTestRule.setContent {
            PopularMoviesTheme(dynamicColor = false) {
                MovieDetailsContent(
                    movieDetails = sampleMovieDetailsObservable(),
                    trailers = persistentListOf(),
                    reviews = persistentListOf(),
                    isFavorite = false,
                    onFavoriteToggle = { favoriteToggleCount++ },
                    onTrailerClick = {},
                    onReviewClick = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Mark as favorite:").performClick()

        assertEquals(1, favoriteToggleCount)
    }

    @Test
    fun movieDetailsContent_callsOnTrailerClick_whenTrailerTapped() {
        var openedTrailerKey: String? = null
        val trailer = sampleTrailer()

        composeTestRule.setContent {
            PopularMoviesTheme(dynamicColor = false) {
                MovieDetailsContent(
                    modifier = Modifier.testTag("movie_details_content"),
                    movieDetails = sampleMovieDetailsObservable(),
                    trailers = persistentListOf(trailer),
                    reviews = persistentListOf(),
                    isFavorite = false,
                    onFavoriteToggle = {},
                    onTrailerClick = { openedTrailerKey = it.key },
                    onReviewClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText(trailer.name).performClick()

        assertEquals(trailer.key, openedTrailerKey)
    }

    private fun sampleMovieDetailsObservable() = MovieDetailsObservable(
        id = 42,
        title = "Arrival",
        backdropPath = "https://example.com/backdrop.jpg",
        posterPath = "https://example.com/poster.jpg",
        overview = "A linguist works with the military to communicate with alien lifeforms.",
        releaseDate = "2016-11-11",
        voteAverage = 8.1,
    )

    private fun sampleTrailer() = Trailer(
        id = "trailer-id",
        iso6391 = "en",
        iso31661 = "US",
        key = "trailer-key",
        name = "Official Trailer",
        site = "YouTube",
        size = 1080,
        type = "Trailer",
    )

    private fun sampleReview() = Review(
        id = "review-id",
        author = "A Reviewer",
        content = "A detailed review for the movie.",
        url = "https://example.com/review",
    )
}
