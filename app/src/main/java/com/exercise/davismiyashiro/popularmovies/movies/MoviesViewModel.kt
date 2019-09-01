package com.exercise.davismiyashiro.popularmovies.movies

import android.app.Application

import androidx.databinding.library.baseAdapters.BR
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.*

import com.exercise.davismiyashiro.popularmovies.data.MovieDetails
import com.exercise.davismiyashiro.popularmovies.data.Repository
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsObservable

import java.util.ArrayList

import com.exercise.davismiyashiro.popularmovies.movies.MoviesActivity.FAVORITES_PARAM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Created by Davis Miyashiro.
 */

class MoviesViewModel(application: Application, private val repository: Repository) : AndroidViewModel(application), Observable {

    @get:Bindable
    var moviesObservable: LiveData<List<MovieDetailsObservable>> = MutableLiveData()
        private set
    private val callbacks = PropertyChangeRegistry()

    private val parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Default
    private val scope = CoroutineScope(coroutineContext)

    private fun setMoviesObservable(moviesObservable: LiveData<List<MovieDetails>>) {
        this.moviesObservable = convertMovieDetailsToUImodel(moviesObservable)
        notifyPropertyChanged(BR.moviesObservable)
    }

    fun setMoviesBySortingOption(sortingOption: String) {

        scope.launch {
            if (sortingOption == FAVORITES_PARAM) {
                setMoviesObservable(repository.loadMoviesFromDb(sortingOption))
            } else {
                setMoviesObservable(repository.loadMoviesFromNetwork(sortingOption))
            }
        }
    }

    fun cancelAsyncTasks() {
        repository.cancelAsyncTasks()
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.remove(callback)
    }

    /**
     * Notifies listeners that all properties of this instance have changed.
     */
    fun notifyChange() {
        callbacks.notifyCallbacks(this, 0, null)
    }

    /**
     * Call this to notify of property changes generate on get Methods annotated with
     * @Bindable and accessible from BR object
     *
     * @param fieldId
     */
    private fun notifyPropertyChanged(fieldId: Int) {
        callbacks.notifyCallbacks(this, fieldId, null)
    }

    class Factory(private val application: Application, private val repository: Repository) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MoviesViewModel(application, repository) as T
        }
    }

    private fun convertMovieDetailsToUImodel(movies: LiveData<List<MovieDetails>>): LiveData<List<MovieDetailsObservable>> {
        val movieDetailsObservableList = MediatorLiveData<List<MovieDetailsObservable>>()

        movieDetailsObservableList.addSource(movies) { result ->
            if (result != null) {
                val movieDetailsObservableList1 = ArrayList<MovieDetailsObservable>()
                for ((movieid, title, posterPath, overview, releaseDate, voteAverage) in result) {
                    movieDetailsObservableList1.add(MovieDetailsObservable(
                            movieid,
                            title,
                            posterPath,
                            overview,
                            releaseDate,
                            voteAverage))
                }
                movieDetailsObservableList.value = movieDetailsObservableList1
            }

        }
        return movieDetailsObservableList
    }
}
