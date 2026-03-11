package com.exercise.davismiyashiro.popularmovies

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import kotlinx.serialization.Serializable
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsObservable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object MovieList : Route

    @Serializable
    data class MovieDetails(val movie: MovieDetailsObservable) : Route
}

@Composable
fun rememberNavigationState(
    startRoute: Route,
    topLevelRoutes: Set<Route>
): NavigationState {

    val topLevelRoute = rememberSerializable(
        startRoute, topLevelRoutes,
        serializer = MutableStateSerializer(NavKeySerializer())
    ) {
        mutableStateOf(startRoute)
    }

    val backStacks = topLevelRoutes.associateWith { key -> rememberNavBackStack(key) }

    return remember(startRoute, topLevelRoutes) {
        NavigationState(
            startRoute = startRoute,
            topLevelRoute = topLevelRoute,
            backStacks = backStacks
        )
    }
}

class NavigationState(
    val startRoute: Route,
    topLevelRoute: MutableState<Route>,
    val backStacks: Map<Route, NavBackStack<out NavKey>>
) {
    var topLevelRoute: Route by topLevelRoute
    val stacksInUse: List<Route>
        get() = if (topLevelRoute == startRoute) {
            listOf(startRoute)
        } else {
            listOf(startRoute, topLevelRoute)
        }
}

@Composable
fun NavigationState.toEntries(
    entryProvider: (Route) -> NavEntry<out Route>
): SnapshotStateList<NavEntry<out Route>> {

    val decoratedEntries = backStacks.mapValues { (_, stack) ->
        val decorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
        )
        @Suppress("UNCHECKED_CAST")
        rememberDecoratedNavEntries(
            backStack = stack as NavBackStack<NavKey>,
            entryDecorators = decorators,
            entryProvider = entryProvider as (NavKey) -> NavEntry<NavKey>
        )
    }

    @Suppress("UNCHECKED_CAST")
    return stacksInUse
        .flatMap { decoratedEntries[it] ?: emptyList() }
        .toMutableStateList() as SnapshotStateList<NavEntry<out Route>>
}

class Navigator(val state: NavigationState) {
    fun navigate(route: Route) {
        if (route in state.backStacks.keys) {
            state.topLevelRoute = route
        } else {
            @Suppress("UNCHECKED_CAST")
            (state.backStacks[state.topLevelRoute] as? NavBackStack<Route>)?.add(route)
        }
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute]
            ?: error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.last()

        if (currentRoute == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }
}
