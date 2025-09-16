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

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.exercise.davismiyashiro.popularmovies.R
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails
import com.exercise.davismiyashiro.popularmovies.data.Review
import com.exercise.davismiyashiro.popularmovies.data.Trailer
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Davis Miyashiro.
 */

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    application: Application,
    val repository: MovieDbApiClient.MovieRepository
) :
    AndroidViewModel(application) {

    private val movieObservable = MutableLiveData<MovieDetailsObservable>()
    val movieLiveData: LiveData<MovieDetailsObservable> = movieObservable

    private val id = MutableLiveData<Int>()

    val reviews: LiveData<List<Review>> = id.switchMap { value ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emitSource(repository.findReviewsByMovieId(value))
        }
    }

    val trailers: LiveData<List<Trailer>> = id.switchMap { value ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emitSource(repository.findTrailersByMovieId(value))
        }
    }

    val favoriteCheckBoxLivedata: LiveData<Boolean> = id.switchMap { id ->
        if (id == 0) {
            MutableLiveData(false)
        } else {
            repository.getMovieFromDb(id).map { movie: MovieDetails? ->
                movie != null
            }
        }
    }.distinctUntilChanged()

    private val _toastMessageEvents = MutableSharedFlow<Int>()
    val toastMessageEvents: SharedFlow<Int> = _toastMessageEvents.asSharedFlow()

    fun setFavorite() {
        val currentIsFavorite = favoriteCheckBoxLivedata.value ?: return
        val movieObs = movieObservable.value ?: return
        if (currentIsFavorite) {
            executeDbOperation(
                operation = { deleteMovie(movieObs) },
                successMessage = R.string.movie_deleted_msg,
                failureMessage = R.string.error_movie_deleted_msg
            )
        } else {
            executeDbOperation(
                operation = { insertMovie(movieObs) },
                successMessage = R.string.movie_added_msg,
                failureMessage = R.string.error_movie_added_msg
            )
        }
    }

    private fun executeDbOperation(
        operation: suspend () -> Unit,
        @StringRes successMessage: Int,
        @StringRes failureMessage: Int
    ) {
        viewModelScope.launch {
            try {
                operation()
                _toastMessageEvents.emit(successMessage)
            } catch (error: Exception) {
                Timber.e(error)
                _toastMessageEvents.emit(failureMessage)
            } finally {
                // TODO: Clear loading widget
            }
        }
    }

    fun setMovieDetailsLivedatas(movieDetailsObservable: MovieDetailsObservable) {
        id.postValue(movieDetailsObservable.id)
        movieObservable.postValue(movieDetailsObservable)
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

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application, private val repository: MovieDbApiClient.MovieRepository) :
        ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MovieDetailsViewModel(application, repository) as T
        }
    }
}
