package com.exercise.davismiyashiro.popularmovies.moviedetails

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutBaseScope
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.Dimension
import com.exercise.davismiyashiro.popularmovies.R

private const val BACKDROP_ASPECT_RATIO = 16f / 9f
private val posterWidth = 100.dp
private val posterHeight = 150.dp

@Composable
internal fun MovieDetailsHeader(
    movieDetails: MovieDetailsObservable,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
    ) {
        val backdropRef = createRef()
        val posterRef = createRef()
        val titleRef = createRef()
        val favoriteRef = createRef()
        val releaseDateRef = createRef()
        val voteAverageRef = createRef()
        val leftGuideline = createGuidelineFromStart(16.dp)
        val rightGuideline = createGuidelineFromEnd(16.dp)

        HeaderImages(
            movieDetails = movieDetails,
            backdropRef = backdropRef,
            posterRef = posterRef,
            leftGuideline = leftGuideline,
        )
        MovieTitle(
            movieDetails = movieDetails,
            backdropRef = backdropRef,
            posterRef = posterRef,
            favoriteRef = favoriteRef,
            titleRef = titleRef,
        )
        FavoriteButton(
            isFavorite = isFavorite,
            onFavoriteToggle = onFavoriteToggle,
            titleRef = titleRef,
            favoriteRef = favoriteRef,
            rightGuideline = rightGuideline,
        )
        ReleaseDateAndRating(
            movieDetails = movieDetails,
            titleRef = titleRef,
            releaseDateRef = releaseDateRef,
            voteAverageRef = voteAverageRef,
        )
    }
}

@Composable
private fun ConstraintLayoutScope.HeaderImages(
    movieDetails: MovieDetailsObservable,
    backdropRef: ConstrainedLayoutReference,
    posterRef: ConstrainedLayoutReference,
    leftGuideline: ConstraintLayoutBaseScope.VerticalAnchor,
) {
    val isPreview = LocalInspectionMode.current
    val backdropModel = if (isPreview) R.drawable.header else movieDetails.backdropPath
    val posterModel = if (isPreview) R.drawable.poster else movieDetails.posterPath

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
            .aspectRatio(BACKDROP_ASPECT_RATIO),
    )

    ImagePlaceholder(
        model = posterModel,
        contentDescription = stringResource(R.string.movie_poster_description),
        modifier = Modifier
            .constrainAs(posterRef) {
                top.linkTo(backdropRef.bottom)
                bottom.linkTo(backdropRef.bottom)
                start.linkTo(leftGuideline)
                width = Dimension.value(posterWidth)
                height = Dimension.value(posterHeight)
            },
    )
}

@Composable
private fun ConstraintLayoutScope.MovieTitle(
    movieDetails: MovieDetailsObservable,
    backdropRef: ConstrainedLayoutReference,
    posterRef: ConstrainedLayoutReference,
    favoriteRef: ConstrainedLayoutReference,
    titleRef: ConstrainedLayoutReference,
) {
    Text(
        text = movieDetails.title,
        style = MaterialTheme.typography.headlineSmall,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.constrainAs(titleRef) {
            top.linkTo(backdropRef.bottom, margin = 8.dp)
            start.linkTo(posterRef.end, margin = 16.dp)
            end.linkTo(favoriteRef.start, margin = 8.dp)
            width = Dimension.fillToConstraints
        },
    )
}

@Composable
private fun ConstraintLayoutScope.FavoriteButton(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    titleRef: ConstrainedLayoutReference,
    favoriteRef: ConstrainedLayoutReference,
    rightGuideline: ConstraintLayoutBaseScope.VerticalAnchor,
) {
    IconToggleButton(
        checked = isFavorite,
        onCheckedChange = { onFavoriteToggle() },
        modifier = Modifier.constrainAs(favoriteRef) {
            top.linkTo(titleRef.top)
            end.linkTo(rightGuideline)
        },
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = stringResource(R.string.mark_as_favorite),
            tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.Gray,
        )
    }
}

@Composable
private fun ConstraintLayoutScope.ReleaseDateAndRating(
    movieDetails: MovieDetailsObservable,
    titleRef: ConstrainedLayoutReference,
    releaseDateRef: ConstrainedLayoutReference,
    voteAverageRef: ConstrainedLayoutReference,
) {
    if (movieDetails.releaseDate.isEmpty()) return

    Text(
        text = stringResource(
            R.string.release_date,
            movieDetails.releaseDate,
        ),
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.constrainAs(releaseDateRef) {
            top.linkTo(titleRef.bottom, margin = 8.dp)
            start.linkTo(titleRef.start)
        },
    )
    Text(
        text = stringResource(R.string.average_rating, movieDetails.voteAverage),
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.constrainAs(voteAverageRef) {
            top.linkTo(releaseDateRef.bottom, margin = 8.dp)
            start.linkTo(titleRef.start)
        },
    )
}
