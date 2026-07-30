package com.exercise.davismiyashiro.popularmovies.moviedetails

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import coil3.annotation.ExperimentalCoilApi
import com.exercise.davismiyashiro.popularmovies.R
import com.exercise.davismiyashiro.popularmovies.domain.Review
import com.exercise.davismiyashiro.popularmovies.domain.Trailer
import com.exercise.davismiyashiro.popularmovies.ui.theme.PopularMoviesTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalCoilApi::class)
@Preview(showBackground = true)
@Composable
private fun ImagePlaceholderPreview() {
    PopularMoviesTheme {
        ImagePlaceholder(
            model = R.drawable.header,
            contentDescription = "Sample Image Preview",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TrailerItemPreview() {
    PopularMoviesTheme {
        val sampleTrailer = Trailer(
            id = "1",
            key = "abcdef",
            name = "Official Trailer",
            site = "YouTube",
            size = 1080,
            type = "Trailer",
        )
        TrailerItem(trailer = sampleTrailer, onClick = { _ -> })
    }
}

@Preview(showBackground = true)
@Composable
private fun ReviewItemPreview() {
    PopularMoviesTheme {
        val sampleReview = Review(
            id = "1",
            author = "John Doe",
            content = "This is a great movie! ".repeat(10),
            url = "",
        )
        ReviewItem(review = sampleReview, onClick = { _ -> })
    }
}

@Preview(showBackground = true, name = "MovieDetailsContent - Populated")
@Composable
private fun MovieDetailsContentPopulatedPreview() {
    val sampleMovieDetails = MovieDetailsUI(
        id = 1,
        title = "Awesome Movie Title",
        posterPath = "/poster.jpg",
        overview = "This is a really awesome movie that you should definitely watch. ".repeat(5),
        releaseDate = "2024-01-01",
        voteAverage = 8.5,
        backdropPath = "/backdrop.jpg",
    )
    val sampleTrailers = listOf(
        Trailer(
            id = "1",
            key = "key1",
            name = "Trailer 1",
            site = "YouTube",
            type = "Trailer",
            size = 1080,
        ),
        Trailer(
            id = "2",
            key = "key2",
            name = "Trailer 2 - Extended Cut",
            site = "YouTube",
            type = "Trailer",
            size = 1080,
        ),
    ).toImmutableList()

    val sampleReviews = listOf(
        Review(
            id = "1",
            author = "Jane Critic",
            content = "A cinematic masterpiece! ".repeat(3),
            url = "",
        ),
        Review(
            id = "2",
            author = "Bob Reviewer",
            content = "Simply stunning visuals and compelling story. ".repeat(3),
            url = "",
        ),
    ).toImmutableList()

    PopularMoviesTheme {
        MovieDetailsScreenContent(
            movieDetails = sampleMovieDetails,
            trailers = sampleTrailers,
            reviews = sampleReviews,
            isFavorite = true,
            onFavoriteToggle = {},
            onTrailerClick = {},
            onReviewClick = {},
        )
    }
}

@Preview(showBackground = true, name = "MovieDetailsContent - Empty")
@Composable
private fun MovieDetailsContentEmptyPreview() {
    val sampleMovieDetails = MovieDetailsUI(
        id = 1,
        title = "Awesome Movie Title",
        posterPath = "/poster.jpg",
        overview = "This is a really awesome movie that you should definitely watch. ".repeat(5),
        releaseDate = "2024-01-01",
        voteAverage = 8.5,
        backdropPath = "/backdrop.jpg",
    )
    PopularMoviesTheme {
        MovieDetailsScreenContent(
            movieDetails = sampleMovieDetails,
            trailers = persistentListOf(),
            reviews = persistentListOf(),
            isFavorite = false,
            onFavoriteToggle = {},
            onTrailerClick = {},
            onReviewClick = {},
        )
    }
}
