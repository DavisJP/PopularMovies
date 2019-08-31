package com.exercise.davismiyashiro.popularmovies.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.exercise.davismiyashiro.popularmovies.BuildConfig
import com.exercise.davismiyashiro.popularmovies.data.local.MovieDataService
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDao
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb
import com.exercise.davismiyashiro.popularmovies.movies.MoviesActivity.FAVORITES_PARAM
import timber.log.Timber
import java.util.*

/**
 * Created by Davis Miyashiro.
 */

class Repository(private val theMovieDb: TheMovieDb, private val moviesDao: MoviesDao) {

    private var asyncTaskInsert: MovieDataService.AsyncTaskInsert? = null
    private var asyncTaskDelete: MovieDataService.AsyncTaskDelete? = null
    private val asyncTaskQueryMovie: MovieDataService.AsyncTaskQueryMovie? = null
    private var asyncTaskQueryAll: MovieDataService.AsyncTaskQueryAll? = null

    fun loadMoviesFromNetwork(sortingOption: String): LiveData<List<MovieDetails>> {
        val call = theMovieDb.getPopular(sortingOption, BuildConfig.API_KEY)

        val moviesObservable = MediatorLiveData<List<MovieDetails>>()

        MovieDbApiClient.enqueue(call, object : MovieDbApiClient.RequestListener<Response<MovieDetails>> {
            override fun onRequestFailure(throwable: Throwable) {
                Timber.e("FAIL! = " + throwable.localizedMessage)

                if (throwable.cause is UnknownError) {
                    throwable.printStackTrace()
                    throw UnknownError(throwable.message)
                }
                moviesObservable.setValue(ArrayList())
            }

            override fun onRequestSuccess(result: Response<MovieDetails>?) {
                val movies = result!!.results
                if (movies != null) {
                    moviesObservable.setValue(movies)
                } else {
                    moviesObservable.setValue(ArrayList())
                    //TODO: call.cancel() if finished too soon?
                }
            }
        })
        return moviesObservable
    }

    fun loadMoviesFromDb(sortingOption: String): LiveData<List<MovieDetails>> {

        val moviesObservable = MediatorLiveData<List<MovieDetails>>()

        asyncTaskQueryAll = MovieDataService.AsyncTaskQueryAll(moviesDao,
                object: MovieDataService.AsyncTaskQueryAll.AsyncTaskQueryAllResponse {
            override fun processFinish(movieList: LiveData<List<MovieDetails>>) {
                if (movieList != null) {
                    moviesObservable.addSource<List<MovieDetails>>(movieList) { values ->
                        if (sortingOption == FAVORITES_PARAM) {
                            moviesObservable.value = values
                        }
                    }
                } else {
                    moviesObservable.setValue(ArrayList())
                }
            }
        })
        asyncTaskQueryAll?.execute()
        return moviesObservable
    }

    fun getMovieFromDb(movieId: Int): LiveData<MovieDetails> {
        val moviesObservable = MediatorLiveData<MovieDetails>()

        moviesObservable.addSource(moviesDao.getMovieById(movieId)) { result ->
            if (result != null && result.movieid != 0) {
                Timber.e("moviesDao result: $result")
                moviesObservable.setValue(result)
            } else {
                Timber.e("moviesDao returned null")
                moviesObservable.setValue(null)
            }
        }
        return moviesObservable
    }

    fun cancelAsyncTasks() {
        if (asyncTaskQueryAll != null) asyncTaskQueryAll?.cancel(true)
        if (asyncTaskInsert != null) asyncTaskInsert?.cancel(true)
        if (asyncTaskDelete != null) asyncTaskDelete?.cancel(true)
    }

    fun findTrailersByMovieId(movieId: Int?): LiveData<List<Trailer>> {

        val call = theMovieDb.getTrailers(movieId.toString(), BuildConfig.API_KEY)

        val trailersObservable = MediatorLiveData<List<Trailer>>()

        MovieDbApiClient.enqueue(call, object : MovieDbApiClient.RequestListener<Response<Trailer>> {
            override fun onRequestFailure(throwable: Throwable) {
                Timber.d("DAVISLOG: FAIL! = $throwable.localizedMessage")
                throwable.printStackTrace()
                //TODO: Add exception handling
                trailersObservable.value = ArrayList()
            }

            override fun onRequestSuccess(result: Response<Trailer>?) {
                val trailers = result!!.results
                if (trailers != null && !trailers.isEmpty()) {
                    trailersObservable.value = trailers
                }
            }
        })
        return trailersObservable
    }

    fun findReviewsByMovieId(movieId: Int?): LiveData<List<Review>> {

        val call = theMovieDb.getReviews(movieId.toString(), BuildConfig.API_KEY)

        val reviewsObservable = MediatorLiveData<List<Review>>()

        MovieDbApiClient.enqueue(call, object : MovieDbApiClient.RequestListener<Response<Review>> {
            override fun onRequestFailure(throwable: Throwable) {
                if (call.isCanceled) {
                    Timber.e("DAVISLOG: request was cancelled")
                } else {
                    Timber.d(throwable, "DAVISLOG: FAIL! : $throwable.localizedMessage")
                    throwable.printStackTrace()
                    //TODO: Add exception handling
                    reviewsObservable.setValue(ArrayList())
                }
            }

            override fun onRequestSuccess(result: Response<Review>?) {
                val reviews = result!!.results
                if (reviews != null && !reviews.isEmpty()) {
                    reviewsObservable.value = reviews
                }
            }
        })
        return reviewsObservable
    }

    fun insertMovieDb(movieDetails: MovieDetails) {
        asyncTaskInsert = MovieDataService.AsyncTaskInsert(moviesDao, object : MovieDataService.AsyncTaskInsert.AsyncResponse {
            override fun processFinish() {
                loadMoviesFromDb(FAVORITES_PARAM)
            }
        })
        asyncTaskInsert?.execute(movieDetails)
    }

    fun deleteMovieDb(movieDetails: MovieDetails) {
        asyncTaskDelete = MovieDataService.AsyncTaskDelete(moviesDao, object : MovieDataService.AsyncTaskDelete.AsyncResponse {
            override fun processFinish() {
                loadMoviesFromDb(FAVORITES_PARAM)
            }
        })
        asyncTaskDelete?.execute(movieDetails)
    }
}
