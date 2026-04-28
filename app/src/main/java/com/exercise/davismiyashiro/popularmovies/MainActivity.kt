package com.exercise.davismiyashiro.popularmovies

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.exercise.davismiyashiro.popularmovies.ui.theme.PopularMoviesTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.exercise.davismiyashiro.popularmovies.moviedetails.movieDetailsEntry
import com.exercise.davismiyashiro.popularmovies.movies.movieListEntry
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PopularMoviesTheme {
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

    val entryProvider = { key: Route ->
        when (key) {
            is Route.MovieList -> movieListEntry(navigator)
            is Route.MovieDetails -> movieDetailsEntry(key)
        }
    }

    @Suppress("UNCHECKED_CAST")
    NavDisplay(
        entries = navigationState.toEntries(entryProvider) as List<NavEntry<Route>>,
        onBack = { navigator.goBack() }
    )
}
