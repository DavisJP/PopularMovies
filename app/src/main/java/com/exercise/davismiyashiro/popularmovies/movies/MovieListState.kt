package com.exercise.davismiyashiro.popularmovies.movies

import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsObservable

sealed class MovieListState {
    object Loading : MovieListState()
    data class Success(val movieList: List<MovieDetailsObservable>) : MovieListState()
    data class Error(val message: String) : MovieListState()
}
