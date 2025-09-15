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

package com.exercise.davismiyashiro.popularmovies.movies

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient
import com.exercise.davismiyashiro.popularmovies.moviedetails.IMG_BASE_URL
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsObservable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Davis Miyashiro.
 */

class MoviesViewModel(
    application: Application,
    private val repository: MovieDbApiClient.MovieRepository
) :
    AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MovieListUI())
    val uiState: StateFlow<MovieListUI> = _uiState.asStateFlow()

    fun loadMovieListBySortingOption(sortingOption: String = POPULARITY_DESC_PARAM) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val movieList = if (sortingOption == FAVORITES_PARAM) {
                repository.loadMoviesFromDb()
            } else {
                repository.loadMoviesFromNetwork(sortingOption)
            }

            movieList.fold(
                ex = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                },
                success = { movieList ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            movieList = convertMovieDetailsToUImodel(movieList)
                        )
                    }
                },
            )
        }
    }

    class Factory(
        private val application: Application,
        private val repository: MovieDbApiClient.MovieRepository
    ) :
        ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MoviesViewModel(application, repository) as T
        }
    }

    private fun convertMovieDetailsToUImodel(movies: List<MovieDetails>): List<MovieDetailsObservable> {
        if (movies.isNotEmpty()) {
            val movieDetailsObservableList = ArrayList<MovieDetailsObservable>()
            for ((movieId, title, backdropPath, posterPath, overview, releaseDate, voteAverage) in movies) {
                movieDetailsObservableList.add(
                    MovieDetailsObservable(
                        movieId,
                        title,
                        IMG_BASE_URL + backdropPath,
                        IMG_BASE_URL + posterPath,
                        overview,
                        releaseDate,
                        voteAverage
                    )
                )
            }
            return movieDetailsObservableList
        } else {
            return emptyList()
        }
    }
}
