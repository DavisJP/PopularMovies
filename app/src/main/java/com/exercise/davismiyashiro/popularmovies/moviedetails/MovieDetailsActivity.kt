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

package com.exercise.davismiyashiro.popularmovies.moviedetails

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import coil3.compose.SubcomposeAsyncImage
import com.exercise.davismiyashiro.popularmovies.App
import com.exercise.davismiyashiro.popularmovies.R
import com.exercise.davismiyashiro.popularmovies.data.Review
import com.exercise.davismiyashiro.popularmovies.data.Trailer


const val IMG_BASE_URL = "https://image.tmdb.org/t/p/w500"

class MovieDetailsActivity : ComponentActivity() {

    private lateinit var viewModel: MovieDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = (application as App).repository
        val factory = MovieDetailsViewModel.Factory(application, repository)
        viewModel = ViewModelProvider(this, factory)[MovieDetailsViewModel::class.java]

        if (intent.hasExtra(MOVIE_DETAILS)) {
            val movieDetails = intent.getParcelableExtra<MovieDetailsObservable>(MOVIE_DETAILS)
            movieDetails?.let {
                viewModel.setMovieDetailsLivedatas(it)
            }
        }

        setContent {
            MaterialTheme {
                MovieDetailsScreen(
                    viewModel = viewModel,
                    onOpenTrailer = { key -> openTrailer(key) }
                )
            }
        }
    }

    private fun showDbResultMessage(@StringRes msg: Int) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun openTrailer(param: String) {
        val videoLink = "https://m.youtube.com/watch?v=$param".toUri()
        val intent = Intent(Intent.ACTION_VIEW, videoLink)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, R.string.no_app_to_open_youtube, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        var MOVIE_DETAILS = "THEMOVIEDBDETAILS"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailsScreen(
    viewModel: MovieDetailsViewModel,
    onOpenTrailer: (String) -> Unit
) {
    val movieDetails by viewModel.movieLiveData.observeAsState()
    val reviews by viewModel.reviews.observeAsState(emptyList())
    val trailers by viewModel.trailers.observeAsState(emptyList())
    val isFavorite by viewModel.favoriteCheckBoxLivedata.observeAsState(false)
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.toastMessageEvents.collect { messageId ->
            Toast.makeText(context, messageId, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        movieDetails?.title ?: stringResource(R.string.title_activity_movie_details)
                    )
                }
            )
        }
    ) { paddingValues ->
        val currentMovieDetails = movieDetails
        if (currentMovieDetails != null) {
            MovieDetailsContent(
                modifier = Modifier.padding(paddingValues),
                movieDetails = currentMovieDetails,
                trailers = trailers,
                reviews = reviews,
                isFavorite = isFavorite,
                onFavoriteToggle = { viewModel.setFavorite() },
                onTrailerClick = { trailer -> onOpenTrailer(trailer.key) },
                onReviewClick = { /* Handle review click if needed in the future */ }
            )
        } else {
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

@Composable
fun MovieDetailsContent(
    modifier: Modifier = Modifier,
    movieDetails: MovieDetailsObservable,
    trailers: List<Trailer>,
    reviews: List<Review>,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onTrailerClick: (Trailer) -> Unit,
    onReviewClick: (Review) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Movie Backdrop and Favorite Icon
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                val headerImage = if (LocalInspectionMode.current) {
                    R.drawable.header
                } else {
                    movieDetails.backdropPath
                }
                ImagePlaceholder(
                    model = headerImage,
                    contentDescription = "Movie Backdrop",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                IconToggleButton(
                    checked = isFavorite,
                    onCheckedChange = { onFavoriteToggle() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = stringResource(R.string.mark_as_favorite),
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Movie Poster and Basic Info
        item {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                val poster = if (LocalInspectionMode.current) {
                    R.drawable.poster
                } else {
                    movieDetails.posterPath
                }
                ImagePlaceholder(
                    model = poster,
                    contentDescription = "Movie Poster",
                    modifier = Modifier
                        .width(120.dp)
                        .height(180.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = movieDetails.title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.release_date, movieDetails.releaseDate),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.average_rating,
                            movieDetails.voteAverage.toString()
                        ), style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Synopsis
        item {
            Column {
                Text(
                    text = stringResource(R.string.synopsis_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = movieDetails.overview, style = MaterialTheme.typography.bodyMedium)
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Trailers
        if (trailers.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.trailers),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(trailers) { trailer ->
                TrailerItem(trailer = trailer, onClick = { onTrailerClick(trailer) })
                Divider()
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Reviews
        if (reviews.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.reviews),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(reviews) { review ->
                ReviewItem(review = review, onClick = { onReviewClick(review) })
                Divider()
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun ImagePlaceholder(model: Any, contentDescription: String?, modifier: Modifier = Modifier) {
    SubcomposeAsyncImage(
        modifier = modifier,
        model = model,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        loading = {
            CircularProgressIndicator()
        },
        error = {

        }
    )
}

@Composable
fun TrailerItem(trailer: Trailer, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.PlayArrow,
            contentDescription = stringResource(R.string.play_trailer_desc),
            modifier = Modifier.size(36.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(trailer.name, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ReviewItem(review: Review, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Text(review.author, style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            review.content,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun stringResource(@StringRes id: Int, vararg formatArgs: Any): String {
    return LocalContext.current.getString(id, *formatArgs)
}