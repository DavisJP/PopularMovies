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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
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
    ) {
        // Movie Poster and Basic Info
        item {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                val (
                    backdropRef, posterRef, titleRef, favoriteRef,
                    releaseDateRef,
                    voteAverageRef,
                ) = createRefs()

                val leftGuideline = createGuidelineFromStart(16.dp)
                val rightGuideline = createGuidelineFromEnd(16.dp)

                // Backdrop Image
                val backdropModel =
                    if (LocalInspectionMode.current) {
                        R.drawable.header
                    } else {
                        movieDetails.backdropPath
                    }
                ImagePlaceholder(
                    model = backdropModel,
                    contentDescription = "Backdrop",
                    modifier = Modifier
                        .constrainAs(backdropRef) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                        }
                        .aspectRatio(16f / 9f)
                )

                // Poster Image
                val posterModel =
                    if (LocalInspectionMode.current) {
                        R.drawable.poster
                    } else {
                        movieDetails.posterPath
                    }
                ImagePlaceholder(
                    model = posterModel,
                    contentDescription = stringResource(R.string.movie_poster_description),
                    modifier = Modifier
                        .constrainAs(posterRef) {
                            top.linkTo(backdropRef.bottom)
                            bottom.linkTo(backdropRef.bottom)
                            start.linkTo(leftGuideline)
                            width = Dimension.value(100.dp)
                            height = Dimension.value(150.dp)
                        }
                )

                // Movie Title
                Text(
                    text = movieDetails.title,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.constrainAs(titleRef) {
                        top.linkTo(backdropRef.bottom, margin = 8.dp) // Below backdrop
                        start.linkTo(posterRef.end, margin = 16.dp) // To the right of the poster
                        end.linkTo(favoriteRef.start, margin = 8.dp)
                        width = Dimension.fillToConstraints
                    }
                )

                // Favorite Toggle Button
                IconToggleButton(
                    checked = isFavorite,
                    onCheckedChange = { onFavoriteToggle() },
                    modifier = Modifier.constrainAs(favoriteRef) {
                        top.linkTo(titleRef.top)
                        end.linkTo(rightGuideline)
                    }
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(R.string.mark_as_favorite),
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }

                // Release Date & Average Rating
                if (movieDetails.releaseDate.isNotEmpty()) {
                    Text(
                        text = stringResource(
                            R.string.release_date,
                            movieDetails.releaseDate
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.constrainAs(releaseDateRef) {
                            top.linkTo(titleRef.bottom, margin = 8.dp)
                            start.linkTo(titleRef.start) // Align with title's start
                        }
                    )
                    Text(
                        text = stringResource(R.string.average_rating, movieDetails.voteAverage),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.constrainAs(voteAverageRef) {
                            top.linkTo(releaseDateRef.bottom, margin = 8.dp)
                            start.linkTo(titleRef.start)
                        }
                    )
                }
            }
        }

        // Trailers
        if (trailers.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.trailers),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 8.dp
                    )
                )
            }
            items(trailers) { trailer ->
                TrailerItem(trailer = trailer, onClick = { onTrailerClick(trailer) })
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }
        }

        // Reviews
        if (reviews.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.reviews),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 8.dp
                    )
                )
            }
            items(reviews) { review ->
                ReviewItem(review = review, onClick = { onReviewClick(review) })
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
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
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        },
        error = {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Error loading image",
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    )
}

@Composable
fun TrailerItem(trailer: Trailer, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            review.author,
            style = MaterialTheme.typography.titleSmall,
            overflow = TextOverflow.Ellipsis
        )
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