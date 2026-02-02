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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exercise.davismiyashiro.popularmovies.R
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails
import com.exercise.davismiyashiro.popularmovies.data.Repository
import com.exercise.davismiyashiro.popularmovies.data.Review
import com.exercise.davismiyashiro.popularmovies.data.Trailer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Davis Miyashiro.
 */

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    val repository: Repository
) :
    ViewModel() {

    private val _movieObservable = MutableStateFlow<MovieDetailsObservable?>(null)
    val movieObservable: StateFlow<MovieDetailsObservable?> = _movieObservable.asStateFlow()

    private val id: Flow<Int> = movieObservable.mapNotNull { it?.id }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val reviews: StateFlow<List<Review>> = id.flatMapLatest { id ->
        repository.findReviewsByMovieId(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val trailers: StateFlow<List<Trailer>> = id.flatMapLatest { id ->
        repository.findTrailersByMovieId(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val isFavorite: StateFlow<Boolean> = id.flatMapLatest { id ->
        repository.getMovieFromDb(id)
    }.map { movieDetails ->
        movieDetails != null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    private val _toastMessageEvents = MutableSharedFlow<Int>()
    val toastMessageEvents: SharedFlow<Int> = _toastMessageEvents.asSharedFlow()

    fun setFavorite() {
        val currentIsFavorite = isFavorite.value
        val movieObs = _movieObservable.value ?: return

        viewModelScope.launch {
            if (currentIsFavorite) {
                deleteMovie(movieObs)
                _toastMessageEvents.emit(R.string.movie_deleted_msg)
            } else {
                insertMovie(movieObs)
                _toastMessageEvents.emit(R.string.movie_added_msg)
            }
        }
    }

    fun setMovieDetails(movieDetailsObservable: MovieDetailsObservable) {
        _movieObservable.value = movieDetailsObservable
    }

    private suspend fun insertMovie(movieDetailsObservable: MovieDetailsObservable) {
        repository.insertMovieDb(
            MovieDetails(
                movieDetailsObservable.id,
                movieDetailsObservable.title,
                movieDetailsObservable.backdropPath,
                movieDetailsObservable.posterPath,
                movieDetailsObservable.overview,
                movieDetailsObservable.releaseDate,
                movieDetailsObservable.voteAverage
            )
        )
    }

    private suspend fun deleteMovie(movieDetailsObservable: MovieDetailsObservable) {
        repository.deleteMovieDb(
            MovieDetails(
                movieDetailsObservable.id,
                movieDetailsObservable.title,
                movieDetailsObservable.backdropPath,
                movieDetailsObservable.posterPath,
                movieDetailsObservable.overview,
                movieDetailsObservable.releaseDate,
                movieDetailsObservable.voteAverage
            )
        )
    }
}
