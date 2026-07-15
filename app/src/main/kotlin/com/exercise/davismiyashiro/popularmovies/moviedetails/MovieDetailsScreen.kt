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
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import coil3.compose.SubcomposeAsyncImage
import com.exercise.davismiyashiro.popularmovies.R
import com.exercise.davismiyashiro.popularmovies.Route
import com.exercise.davismiyashiro.popularmovies.data.Review
import com.exercise.davismiyashiro.popularmovies.data.Trailer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

const val IMG_BASE_URL = "https://image.tmdb.org/t/p/w500"

fun movieDetailsEntry(key: Route.MovieDetails) = NavEntry(key) {
    val viewModel: MovieDetailsViewModel = hiltViewModel()
    val context = LocalContext.current
    MovieDetailsScreen(
        movieDetails = key.movie,
        viewModel = viewModel,
        onOpenTrailer = { trailerKey ->
            val videoLink = "https://m.youtube.com/watch?v=$trailerKey".toUri()
            val intent = Intent(Intent.ACTION_VIEW, videoLink)
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, R.string.no_app_to_open_youtube, Toast.LENGTH_SHORT).show()
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailsScreen(
    movieDetails: MovieDetailsUI,
    viewModel: MovieDetailsViewModel,
    onOpenTrailer: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val reviews by produceState(initialValue = persistentListOf(), movieDetails.id, viewModel) {
        value = viewModel.reviews(movieDetails.id)
    }
    val trailers by produceState(initialValue = persistentListOf(), movieDetails.id, viewModel) {
        value = viewModel.trailers(movieDetails.id)
    }
    val isFavoriteFlow =
        remember(movieDetails.id, viewModel) { viewModel.isFavorite(movieDetails.id) }
    val isFavorite by isFavoriteFlow.collectAsStateWithLifecycle(initialValue = false)
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.toastMessageEvents.collect { messageId ->
            Toast.makeText(context, messageId, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        movieDetails.title,
                    )
                },
            )
        },
    ) { paddingValues ->
        MovieDetailsContent(
            movieDetails = movieDetails,
            trailers = trailers,
            reviews = reviews,
            isFavorite = isFavorite,
            onFavoriteToggle = { viewModel.setFavorite(movieDetails, isFavorite) },
            onTrailerClick = { trailer -> onOpenTrailer(trailer.key) },
            onReviewClick = { /* Handle review click if needed in the future */ },
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@Composable
fun MovieDetailsContent(
    movieDetails: MovieDetailsUI,
    trailers: ImmutableList<Trailer>,
    reviews: ImmutableList<Review>,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onTrailerClick: (Trailer) -> Unit,
    onReviewClick: (Review) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
    ) {
        item {
            MovieDetailsHeader(
                movieDetails = movieDetails,
                isFavorite = isFavorite,
                onFavoriteToggle = onFavoriteToggle,
            )
        }

        trailerItems(trailers = trailers, onTrailerClick = onTrailerClick)
        reviewItems(reviews = reviews, onReviewClick = onReviewClick)
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

private fun LazyListScope.trailerItems(
    trailers: ImmutableList<Trailer>,
    onTrailerClick: (Trailer) -> Unit,
) {
    if (trailers.isEmpty()) return

    item {
        SectionTitle(text = stringResource(R.string.trailers))
    }
    items(
        items = trailers,
        key = { trailer -> trailer.id },
        contentType = { "trailer" },
    ) { trailer ->
        TrailerItem(trailer = trailer, onClick = onTrailerClick)
        SectionDivider()
    }
}

private fun LazyListScope.reviewItems(
    reviews: ImmutableList<Review>,
    onReviewClick: (Review) -> Unit,
) {
    if (reviews.isEmpty()) return

    item {
        SectionTitle(text = stringResource(R.string.reviews))
    }
    items(
        items = reviews,
        key = { review -> review.id },
        contentType = { "review" },
    ) { review ->
        ReviewItem(review = review, onClick = onReviewClick)
        SectionDivider()
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 8.dp,
        ),
    )
}

@Composable
private fun SectionDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = 16.dp),
        thickness = DividerDefaults.Thickness,
        color = DividerDefaults.color,
    )
}

@Composable
fun ImagePlaceholder(model: Any, contentDescription: String?, modifier: Modifier = Modifier) {
    SubcomposeAsyncImage(
        modifier = modifier,
        model = model,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        loading = {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
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

@Composable
fun TrailerItem(trailer: Trailer, onClick: (Trailer) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(trailer) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.PlayArrow,
            contentDescription = stringResource(R.string.play_trailer_desc),
            modifier = Modifier.size(36.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(trailer.name, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ReviewItem(review: Review, onClick: (Review) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(review) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            review.author,
            style = MaterialTheme.typography.titleSmall,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            review.content,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
