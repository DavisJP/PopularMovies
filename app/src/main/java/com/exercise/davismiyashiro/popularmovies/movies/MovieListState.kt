package com.exercise.davismiyashiro.popularmovies.movies

import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsObservable
import kotlinx.collections.immutable.ImmutableList

sealed class MovieListState {
    object Loading : MovieListState()
    data class Success(val movieList: ImmutableList<MovieDetailsObservable>) : MovieListState()
    data class Error(val message: String) : MovieListState()
}
