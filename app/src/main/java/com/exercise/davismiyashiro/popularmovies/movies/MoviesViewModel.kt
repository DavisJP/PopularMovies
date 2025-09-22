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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails
import com.exercise.davismiyashiro.popularmovies.data.Repository
import com.exercise.davismiyashiro.popularmovies.di.IoDispatcher
import com.exercise.davismiyashiro.popularmovies.di.MainDispatcher
import com.exercise.davismiyashiro.popularmovies.moviedetails.IMG_BASE_URL
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsObservable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Created by Davis Miyashiro.
 */

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val repository: Repository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    @param:MainDispatcher private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) :
    ViewModel() {

    private val _uiState = MutableStateFlow<MovieListState>(MovieListState.Loading)
    private val _currentSortingOption = MutableStateFlow(POPULARITY_DESC_PARAM)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MovieListState> = _currentSortingOption.combine(
        repository.getFavoriteMoviesIds()
            .distinctUntilChanged()
    ) { sortingOption, favoriteMoviesIds ->
        Pair(sortingOption, favoriteMoviesIds)
    }.flatMapLatest { (sortingOption, favoriteMoviesIds) ->
        flow {
            try {
                val movieList = if (sortingOption == FAVORITES_PARAM) {
                    repository.loadMoviesFromDb()
                } else {
                    repository.loadMoviesFromNetwork(sortingOption)
                }

                emit(
                    movieList.fold(
                        ex = { exception ->
                            MovieListState.Error(
                                message = exception.message.toString()
                            )
                        },
                        success = { movieList ->
                            MovieListState.Success(
                                movieList = convertMovieDetailsToUImodel(movieList)
                            )
                        }
                    ))
            } catch (ex: Exception) {
                emit(MovieListState.Error(ex.message.toString()))
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MovieListState.Loading
    )

    fun loadMovieListBySortingOption(sortingOption: String = POPULARITY_DESC_PARAM) {
        _currentSortingOption.value = sortingOption
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