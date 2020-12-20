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
import androidx.lifecycle.*
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails
import com.exercise.davismiyashiro.popularmovies.data.Repository
import com.exercise.davismiyashiro.popularmovies.data.Review
import com.exercise.davismiyashiro.popularmovies.data.Trailer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Created by Davis Miyashiro.
 */

class MovieDetailsViewModel(application: Application,
                            private val repository: Repository) :
        AndroidViewModel(application) {

    private val movieObservable = MutableLiveData<MovieDetailsObservable>()
    val favoriteCheckBoxLivedata = MutableLiveData<Boolean>()
    private var id = MutableLiveData<Int>()
    var title = MutableLiveData<String>()
    var backDropPath = MutableLiveData<String>()
    var posterPath = MutableLiveData<String>()
    var releaseDate = MutableLiveData<String>()
    var overview = MutableLiveData<String>()
    var voteAverage = MutableLiveData<Double>()

    val reviews: LiveData<List<Review>> = id.switchMap { value ->
        liveData (context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emitSource(repository.findReviewsByMovieId(value))
        }
    }

    val trailers: LiveData<List<Trailer>> = id.switchMap { value ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emitSource(repository.findTrailersByMovieId(value))
        }
    }

    fun getMovieObservable(movieId: Int): LiveData<MovieDetailsObservable> {
        return convertMovieDetailsToUImodel(repository.getMovieFromDb(movieId))
    }

    fun setFavorite() {
        if (favoriteCheckBoxLivedata.value!!) {
            Timber.e("Is favorite, should delete it!")
            executeDbOperation { deleteMovie(movieObservable.value!!) }
            favoriteCheckBoxLivedata.postValue(false)
        } else {
            Timber.e("Is not favorite, save it!")
            executeDbOperation { insertMovie(movieObservable.value!!) }
            favoriteCheckBoxLivedata.setValue(true)
        }
    }

    private fun executeDbOperation(operation: suspend () -> Unit): Unit {
        viewModelScope.launch {
            try {
                operation()
            } catch (error: Exception) {
                Timber.e(error)
            } finally {
                // TODO: Clear loading widget
            }
        }
    }

    fun setMovieDetailsLivedatas(movieDetailsObservable: MovieDetailsObservable) {
        id.postValue(movieDetailsObservable.id)
        title.postValue(movieDetailsObservable.title)
        releaseDate.postValue(movieDetailsObservable.releaseDate)
        overview.postValue(movieDetailsObservable.overview)
        voteAverage.postValue(movieDetailsObservable.voteAverage)
        posterPath.postValue(movieDetailsObservable.posterPath)
        backDropPath.postValue(movieDetailsObservable.backdropPath)

        movieObservable.postValue(movieDetailsObservable)
    }

    private fun convertMovieDetailsToUImodel(movie: LiveData<MovieDetails>): LiveData<MovieDetailsObservable> {
        val movieDetailsObservable = MediatorLiveData<MovieDetailsObservable>()

        favoriteCheckBoxLivedata.postValue(false)
        movieDetailsObservable.addSource(movie) { result ->

            //If it's not null then it's a favorite
            if (result != null) {
                favoriteCheckBoxLivedata.postValue(true)
                Timber.e("Is true!")
                val value = MovieDetailsObservable(
                        result.movieid,
                        result.title,
                        result.backdropPath,
                        result.posterPath,
                        result.overview,
                        result.releaseDate,
                        result.voteAverage)
                movieDetailsObservable.setValue(value)
            } else {
                Timber.e("Is False!")
                favoriteCheckBoxLivedata.postValue(false)
            }
        }
        return movieDetailsObservable
    }

    suspend fun insertMovie(movieDetailsObservable: MovieDetailsObservable) {

        repository.insertMovieDb(MovieDetails(
                movieDetailsObservable.id,
                movieDetailsObservable.title,
                movieDetailsObservable.backdropPath,
                movieDetailsObservable.posterPath,
                movieDetailsObservable.overview,
                movieDetailsObservable.releaseDate,
                movieDetailsObservable.voteAverage))
    }

    suspend fun deleteMovie(movieDetailsObservable: MovieDetailsObservable) {

        repository.deleteMovieDb(MovieDetails(
                movieDetailsObservable.id,
                movieDetailsObservable.title,
                movieDetailsObservable.backdropPath,
                movieDetailsObservable.posterPath,
                movieDetailsObservable.overview,
                movieDetailsObservable.releaseDate,
                movieDetailsObservable.voteAverage))
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application, private val repository: Repository) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MovieDetailsViewModel(application, repository) as T
        }
    }
}
