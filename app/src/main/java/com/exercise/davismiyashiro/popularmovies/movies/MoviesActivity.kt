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

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import com.exercise.davismiyashiro.popularmovies.App
import com.exercise.davismiyashiro.popularmovies.R
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsActivity
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsObservable

const val POPULARITY_DESC_PARAM = "popular"
const val HIGHEST_RATED_PARAM = "top_rated"
const val FAVORITES_PARAM = "favorites"

class MoviesActivity : ComponentActivity() {

    private val viewModel: MoviesViewModel by viewModels {
        val repository = (application as App).repository
        MoviesViewModel.Factory(application, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                MoviesScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadMovieListBySortingOption()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesScreen(viewModel: MoviesViewModel) {
    var currentSortOption by rememberSaveable { mutableStateOf(POPULARITY_DESC_PARAM) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(currentSortOption) {
        viewModel.loadMovieListBySortingOption(currentSortOption)
    }

    Scaffold(
        topBar = {
            MoviesTopAppBar(
                currentSortOption = currentSortOption,
                onSortChanged = { newSortOption ->
                    currentSortOption = newSortOption
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.movieList.isNotEmpty() -> {
                    MovieListGrid(
                        movies = uiState.movieList,
                        onMovieClick = { movie ->
                            val intent = Intent(context, MovieDetailsActivity::class.java).apply {
                                putExtra(MovieDetailsActivity.MOVIE_DETAILS, movie)
                            }
                            context.startActivity(intent)
                        }
                    )
                }

                else -> {
                    Text(
                        text = stringResource(R.string.please_check_your_network_status_or_try_again_later),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesTopAppBar(
    currentSortOption: String,
    onSortChanged: (String) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val titleResId = when (currentSortOption) {
        POPULARITY_DESC_PARAM -> R.string.popular_movies
        HIGHEST_RATED_PARAM -> R.string.highest_rated_movies
        FAVORITES_PARAM -> R.string.favorites
        else -> R.string.app_name
    }

    TopAppBar(
        title = { Text(stringResource(id = titleResId)) },
        actions = {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.action_settings)
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.popular)) },
                        onClick = {
                            onSortChanged(POPULARITY_DESC_PARAM)
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.rating)) },
                        onClick = {
                            onSortChanged(HIGHEST_RATED_PARAM)
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.favorites)) },
                        onClick = {
                            onSortChanged(FAVORITES_PARAM)
                            menuExpanded = false
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun MovieListGrid(
    movies: List<MovieDetailsObservable>,
    onMovieClick: (MovieDetailsObservable) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 180.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(movies, key = { movie -> movie.id }) { movie ->
            MovieGridItem(movie = movie, onMovieClick = onMovieClick)
        }
    }
}

@Composable
fun MovieGridItem(
    movie: MovieDetailsObservable,
    onMovieClick: (MovieDetailsObservable) -> Unit
) {

    val errorImage = rememberVectorPainter(Icons.Filled.Warning)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMovieClick(movie) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        SubcomposeAsyncImage(
            model = movie.posterPath,
            contentDescription = movie.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f),
            contentScale = ContentScale.Crop,
            loading = {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.8f))
            },
            error = {
                errorImage
            }
        )
    }
}

