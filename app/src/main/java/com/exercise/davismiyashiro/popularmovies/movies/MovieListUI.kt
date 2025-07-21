package com.exercise.davismiyashiro.popularmovies.movies

import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsObservable

data class MovieListUI(
    val movieList: List<MovieDetailsObservable> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)
