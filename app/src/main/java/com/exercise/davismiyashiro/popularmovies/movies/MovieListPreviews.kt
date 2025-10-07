package com.exercise.davismiyashiro.popularmovies.movies

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.exercise.davismiyashiro.popularmovies.R
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsObservable

// Sample Data for Previews
private val sampleMovie1 = MovieDetailsObservable(
    id = 1,
    title = "Popular Movie Title 1",
    posterPath = "/sRLC052ieEzkQs9dEtPMfAMPzt.jpg", // Example valid path
    overview = "This is a great movie about popular things happening.",
    backdropPath = "/5A4sRQA8sE6C5fhRen9rmaL3ypr.jpg",
    releaseDate = "2023-01-01",
    voteAverage = 8.5
)

private val sampleMovie2 = MovieDetailsObservable(
    id = 2,
    title = "Top Rated Action Packed Adventure II",
    posterPath = "/v7UF7Y22nKwpsM4XW7gW7W7SLN.jpg", // Example valid path
    overview = "This is a critically acclaimed movie with lots of action and adventure for everyone to enjoy.",
    backdropPath = "/5A4sRQA8sE6C5fhRen9rmaL3ypr.jpg",
    releaseDate = "2023-02-15",
    voteAverage = 9.1
)

private val sampleMovieList = listOf(sampleMovie1, sampleMovie2, sampleMovie1.copy(id = 3, title = "Another Movie 3"), sampleMovie2.copy(id = 4, title = "Sequel to the Adventure 4"))

// Previews for MovieGridItem
@Preview(showBackground = true, widthDp = 200)
@Composable
fun MovieGridItemPreview() {
    MaterialTheme {
        MovieGridItem(movie = sampleMovie1, onMovieClick = {})
    }
}

// Previews for MovieListGrid
@Preview(showBackground = true, widthDp = 380, heightDp = 600)
@Composable
fun MovieListGridPopulatedPreview() {
    MaterialTheme {
        MovieListGrid(movies = sampleMovieList, onMovieClick = {})
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 200)
@Composable
fun MovieListGridEmptyPreview() {
    MaterialTheme {
        MovieListGrid(movies = emptyList(), onMovieClick = {})
    }
}

// Previews for MoviesTopAppBar
@Preview(showBackground = true, widthDp = 360)
@Composable
fun MoviesTopAppBarPopularPreview() {
    MaterialTheme {
        MoviesTopAppBar(currentSortOption = POPULARITY_DESC_PARAM, onSortChanged = {})
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun MoviesTopAppBarHighestRatedPreview() {
    MaterialTheme {
        MoviesTopAppBar(currentSortOption = HIGHEST_RATED_PARAM, onSortChanged = {})
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun MoviesTopAppBarFavoritesPreview() {
    MaterialTheme {
        MoviesTopAppBar(currentSortOption = FAVORITES_PARAM, onSortChanged = {})
    }
}

// Previews for MoviesScreen States (without full ViewModel)

@Preview(showBackground = true, name = "Movies Screen - Loading State")
@Composable
fun MoviesScreenLoadingPreview() {
    MaterialTheme {
        Scaffold(
            topBar = { MoviesTopAppBar(currentSortOption = POPULARITY_DESC_PARAM, onSortChanged = {}) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Preview(showBackground = true, name = "Movies Screen - Error State")
@Composable
fun MoviesScreenErrorPreview() {
    MaterialTheme {
        Scaffold(
            topBar = { MoviesTopAppBar(currentSortOption = POPULARITY_DESC_PARAM, onSortChanged = {}) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.please_check_your_network_status_or_try_again_later),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Movies Screen - Content Loaded")
@Composable
fun MoviesScreenContentLoadedPreview() {
    MaterialTheme {
        Scaffold(
            topBar = { MoviesTopAppBar(currentSortOption = POPULARITY_DESC_PARAM, onSortChanged = {}) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                MovieListGrid(movies = sampleMovieList, onMovieClick = {})
            }
        }
    }
}
