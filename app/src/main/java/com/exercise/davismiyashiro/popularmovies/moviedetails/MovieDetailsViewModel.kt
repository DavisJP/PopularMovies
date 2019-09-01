package com.exercise.davismiyashiro.popularmovies.moviedetails

import android.app.Application
import androidx.lifecycle.*
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails
import com.exercise.davismiyashiro.popularmovies.data.Repository
import com.exercise.davismiyashiro.popularmovies.data.Review
import com.exercise.davismiyashiro.popularmovies.data.Trailer
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

/**
 * Created by Davis Miyashiro.
 */

class MovieDetailsViewModel(application: Application,
                            private val repository: Repository) :
        AndroidViewModel(application) {

    private val movieObservable = MutableLiveData<MovieDetailsObservable>()
    val favoriteCheckBoxLivedata = MutableLiveData<Boolean>()
    var id = MutableLiveData<Int>()
    var title = MutableLiveData<String>()
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
        if (favoriteCheckBoxLivedata != null && favoriteCheckBoxLivedata.value!!) {
            Timber.e("Is favorite, should delete it!")
            deleteMovie(movieObservable.value!!)
            favoriteCheckBoxLivedata.postValue(false)
        } else {
            Timber.e("Is not favorite, save it!")
            insertMovie(movieObservable.value!!)
            favoriteCheckBoxLivedata.setValue(true)
        }
    }

    fun setMovieDetailsLivedatas(movieDetailsObservable: MovieDetailsObservable) {
        id.postValue(movieDetailsObservable.id)
        title.postValue(movieDetailsObservable.title)
        releaseDate.postValue(movieDetailsObservable.releaseDate)
        overview.postValue(movieDetailsObservable.overview)
        voteAverage.postValue(movieDetailsObservable.voteAverage)
        posterPath.postValue(movieDetailsObservable.posterPath)

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

    fun insertMovie(movieDetailsObservable: MovieDetailsObservable) {

        repository.insertMovieDb(MovieDetails(
                movieDetailsObservable.id,
                movieDetailsObservable.title,
                movieDetailsObservable.posterPath,
                movieDetailsObservable.overview,
                movieDetailsObservable.releaseDate,
                movieDetailsObservable.voteAverage))
    }

    fun deleteMovie(movieDetailsObservable: MovieDetailsObservable) {

        repository.deleteMovieDb(MovieDetails(
                movieDetailsObservable.id,
                movieDetailsObservable.title,
                movieDetailsObservable.posterPath,
                movieDetailsObservable.overview,
                movieDetailsObservable.releaseDate,
                movieDetailsObservable.voteAverage))
    }

    fun cancelAsyncTasks() {
        repository.cancelAsyncTasks()
    }

    class Factory(private val application: Application, private val repository: Repository) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MovieDetailsViewModel(application, repository) as T
        }
    }
}
