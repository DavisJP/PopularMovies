/*
 * MIT License
 *
 * Copyright (c) 2019 Davis Miyashiro
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.exercise.davismiyashiro.popularmovies.movies

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import coil3.compose.SubcomposeAsyncImage
import com.exercise.davismiyashiro.popularmovies.Navigator
import com.exercise.davismiyashiro.popularmovies.R
import com.exercise.davismiyashiro.popularmovies.Route
import com.exercise.davismiyashiro.popularmovies.domain.MovieSortOption
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsUI
import kotlinx.collections.immutable.ImmutableList

private const val POSTER_ASPECT_RATIO = 2f / 3f
private const val LOADING_INDICATOR_WIDTH_FRACTION = 0.8f

fun movieListEntry(navigator: Navigator) = NavEntry(Route.MovieList) {
    MoviesScreen(
        onMovieClick = { movie ->
            navigator.navigate(Route.MovieDetails(movie))
        },
    )
}

@Composable
fun MoviesScreen(
    onMovieClick: (MovieDetailsUI) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MoviesViewModel = hiltViewModel(),
) {
    val currentSortOption by viewModel.currentSortingOption.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MoviesContent(
        uiState = uiState,
        currentSortOption = currentSortOption,
        onSortChange = viewModel::loadMovieListBySortingOption,
        onMovieClick = onMovieClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesContent(
    uiState: MovieListState,
    currentSortOption: MovieSortOption,
    onSortChange: (MovieSortOption) -> Unit,
    onMovieClick: (MovieDetailsUI) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            MoviesTopAppBar(
                currentSortOption = currentSortOption,
                onSortChange = onSortChange,
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (val state = uiState) {
                is MovieListState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is MovieListState.Success -> {
                    if (!state.movieList.isEmpty()) {
                        MovieListGrid(
                            movies = state.movieList,
                            onMovieClick = onMovieClick,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.no_movies_found),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                is MovieListState.Error -> {
                    Text(
                        text = stringResource(R.string.please_check_your_network_status_or_try_again_later),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesTopAppBar(
    currentSortOption: MovieSortOption,
    onSortChange: (MovieSortOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val titleResId = when (currentSortOption) {
        MovieSortOption.POPULAR -> R.string.popular_movies
        MovieSortOption.TOP_RATED -> R.string.highest_rated_movies
        MovieSortOption.FAVORITES -> R.string.favorites
    }

    TopAppBar(
        modifier = modifier,
        title = { Text(stringResource(id = titleResId)) },
        actions = {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.action_settings),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.popular)) },
                        onClick = {
                            onSortChange(MovieSortOption.POPULAR)
                            menuExpanded = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.rating)) },
                        onClick = {
                            onSortChange(MovieSortOption.TOP_RATED)
                            menuExpanded = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.favorites)) },
                        onClick = {
                            onSortChange(MovieSortOption.FAVORITES)
                            menuExpanded = false
                        },
                    )
                }
            }
        },
    )
}

@Composable
fun MovieListGrid(
    movies: ImmutableList<MovieDetailsUI>,
    onMovieClick: (MovieDetailsUI) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 180.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        itemsIndexed(
            items = movies,
            key = { index, movie -> "${movie.id}_$index" },
            contentType = { _, _ -> "movie" },
        ) { _, movie ->
            MovieGridItem(movie = movie, onMovieClick = onMovieClick)
        }
    }
}

@Composable
fun MovieGridItem(
    movie: MovieDetailsUI,
    onMovieClick: (MovieDetailsUI) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onMovieClick(movie) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        SubcomposeAsyncImage(
            model = movie.posterPath,
            contentDescription = movie.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(POSTER_ASPECT_RATIO),
            contentScale = ContentScale.Crop,
            loading = {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(LOADING_INDICATOR_WIDTH_FRACTION))
            },
            error = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Error loading image",
                        modifier = Modifier.size(48.dp),
                    )
                }
            },
        )
    }
}
