package com.exercise.davismiyashiro.popularmovies

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsScreen
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsViewModel
import com.exercise.davismiyashiro.popularmovies.movies.MoviesScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navigationState = rememberNavigationState(
        startRoute = Route.MovieList,
        topLevelRoutes = setOf(Route.MovieList)
    )
    val navigator = remember { Navigator(navigationState) }
    val context = LocalContext.current

    val entryProvider = { key: Route ->
        when (key) {
            is Route.MovieList -> NavEntry(key) {
                MoviesScreen(
                    viewModel = hiltViewModel(),
                    onMovieClick = { movie ->
                        navigator.navigate(Route.MovieDetails(movie))
                    }
                )
            }

            is Route.MovieDetails -> NavEntry(key) {
                val viewModel: MovieDetailsViewModel = hiltViewModel()
                viewModel.setMovieDetails(key.movie)
                MovieDetailsScreen(
                    viewModel = viewModel,
                    onOpenTrailer = { trailerKey ->
                        val videoLink = "https://m.youtube.com/watch?v=$trailerKey".toUri()
                        val intent = Intent(Intent.ACTION_VIEW, videoLink)
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(
                                context,
                                R.string.no_app_to_open_youtube,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    NavDisplay(
        entries = navigationState.toEntries(entryProvider) as List<NavEntry<Route>>,
        onBack = { navigator.goBack() }
    )
}
