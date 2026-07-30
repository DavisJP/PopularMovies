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
import com.exercise.davismiyashiro.popularmovies.domain.Repository
import com.exercise.davismiyashiro.popularmovies.data.toDomain
import com.exercise.davismiyashiro.popularmovies.domain.Review
import com.exercise.davismiyashiro.popularmovies.domain.Trailer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Davis Miyashiro.
 */

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    val repository: Repository,
) :
    ViewModel() {

    private val _toastMessageEvents = MutableSharedFlow<Int>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val toastMessageEvents: SharedFlow<Int> = _toastMessageEvents.asSharedFlow()

    suspend fun reviews(movieId: Int): ImmutableList<Review> =
        repository.findReviewsByMovieId(movieId).toImmutableList()

    suspend fun trailers(movieId: Int): ImmutableList<Trailer> =
        repository.findTrailersByMovieId(movieId).toImmutableList()

    fun isFavorite(movieId: Int): Flow<Boolean> =
        repository.getMovieFromDb(movieId).map { movie -> movie != null }

    fun setFavorite(movieDetailsUI: MovieDetailsUI, isFavorite: Boolean) {
        viewModelScope.launch {
            if (isFavorite) {
                deleteMovie(movieDetailsUI)
                _toastMessageEvents.emit(R.string.movie_deleted_msg)
            } else {
                insertMovie(movieDetailsUI)
                _toastMessageEvents.emit(R.string.movie_added_msg)
            }
        }
    }

    private suspend fun insertMovie(movieDetailsUI: MovieDetailsUI) {
        repository.insertMovieDb(movieDetailsUI.toDomain())
    }

    private suspend fun deleteMovie(movieDetailsUI: MovieDetailsUI) {
        repository.deleteMovieDb(movieDetailsUI.toDomain())
    }
}
