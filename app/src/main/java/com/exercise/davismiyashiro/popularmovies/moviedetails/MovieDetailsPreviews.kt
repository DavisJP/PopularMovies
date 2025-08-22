package com.exercise.davismiyashiro.popularmovies.moviedetails

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import coil3.annotation.ExperimentalCoilApi
import com.exercise.davismiyashiro.popularmovies.R
import com.exercise.davismiyashiro.popularmovies.data.Review
import com.exercise.davismiyashiro.popularmovies.data.Trailer

@OptIn(ExperimentalCoilApi::class)
@Preview(showBackground = true)
@Composable
fun ImagePlaceholderPreview() {
    MaterialTheme {
        ImagePlaceholder(
            model = R.drawable.header,
            contentDescription = "Sample Image Preview"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrailerItemPreview() {
    MaterialTheme {
        val sampleTrailer = Trailer(
            id = "1",
            key = "abcdef",
            name = "Official Trailer",
            site = "YouTube",
            size = 1080,
            type = "Trailer",
            iso6391 = "iso6391",
            iso31661 = "iso6391"
        )
        TrailerItem(trailer = sampleTrailer, onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewItemPreview() {
    MaterialTheme {
        val sampleReview = Review(
            id = "1",
            author = "John Doe",
            content = "This is a great movie! ".repeat(10),
            url = ""
        )
        ReviewItem(review = sampleReview, onClick = {})
    }
}

@Preview(showBackground = true, name = "MovieDetailsContent - Populated")
@Composable
fun MovieDetailsContentPopulatedPreview() {
    val sampleMovieDetails = MovieDetailsObservable(
        id = 1,
        title = "Awesome Movie Title",
        posterPath = "/poster.jpg",
        overview = "This is a really awesome movie that you should definitely watch. ".repeat(5),
        releaseDate = "2024-01-01",
        voteAverage = 8.5,
        backdropPath = "/backdrop.jpg"
    )
    val sampleTrailers = listOf(
        Trailer(
            id = "1",
            key = "key1",
            name = "Trailer 1",
            site = "YouTube",
            type = "Trailer",
            size = 1080,
            iso6391 = "iso6391",
            iso31661 = "iso6391"
        ),
        Trailer(
            id = "2",
            key = "key2",
            name = "Trailer 2 - Extended Cut",
            site = "YouTube",
            type = "Trailer",
            size = 1080,
            iso6391 = "iso6391",
            iso31661 = "iso6391"
        )
    )
    val sampleReviews = listOf(
        Review(
            id = "1",
            author = "Jane Critic",
            content = "A cinematic masterpiece! ".repeat(3),
            url = ""
        ),
        Review(
            id = "2",
            author = "Bob Reviewer",
            content = "Simply stunning visuals and compelling story. ".repeat(3),
            url = ""
        )
    )
    MaterialTheme {
        MovieDetailsContent(
            movieDetails = sampleMovieDetails,
            trailers = sampleTrailers,
            reviews = sampleReviews,
            isFavorite = true,
            onFavoriteToggle = {},
            onTrailerClick = {},
            onReviewClick = {}
        )
    }
}

@Preview(showBackground = true, name = "MovieDetailsContent - Empty")
@Composable
fun MovieDetailsContentEmptyPreview() {
    val sampleMovieDetails = MovieDetailsObservable(
        id = 1,
        title = "Awesome Movie Title",
        posterPath = "/poster.jpg",
        overview = "This is a really awesome movie that you should definitely watch. ".repeat(5),
        releaseDate = "2024-01-01",
        voteAverage = 8.5,
        backdropPath = "/backdrop.jpg"
    )
    MaterialTheme {
        MovieDetailsContent(
            movieDetails = sampleMovieDetails,
            trailers = emptyList(),
            reviews = emptyList(),
            isFavorite = false,
            onFavoriteToggle = {},
            onTrailerClick = {},
            onReviewClick = {}
        )
    }
}
