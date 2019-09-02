package com.exercise.davismiyashiro.popularmovies.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.exercise.davismiyashiro.popularmovies.data.local.MovieDataService
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDao
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient.BaseNetworkHandler
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb
import com.exercise.davismiyashiro.popularmovies.movies.FAVORITES_PARAM
import timber.log.Timber
import java.util.*

/**
 * Created by Davis Miyashiro.
 */

class Repository(private val theMovieDb: TheMovieDb, private val moviesDao: MoviesDao) : BaseNetworkHandler(){

    private var asyncTaskInsert: MovieDataService.AsyncTaskInsert? = null
    private var asyncTaskDelete: MovieDataService.AsyncTaskDelete? = null
    private val asyncTaskQueryMovie: MovieDataService.AsyncTaskQueryMovie? = null
    private var asyncTaskQueryAll: MovieDataService.AsyncTaskQueryAll? = null

    suspend fun loadMoviesFromNetwork(sortingOption: String): LiveData<List<MovieDetails>> {

        val moviesObservable = MediatorLiveData<List<MovieDetails>>()

        val moviesResponse = apiCall(
                call = {theMovieDb.getPopular(sortingOption)},
                errorMessage = "Error Fetching Movies"
        )

        moviesObservable.postValue(moviesResponse?.results)

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

    suspend fun findTrailersByMovieId(movieId: Int?): LiveData<List<Trailer>> {

        val trailersObservable = MediatorLiveData<List<Trailer>>()

        val trailersResponse = apiCall(
                call = {theMovieDb.getTrailers(movieId.toString())},
                errorMessage = "Error Fetching Trailers"
        )

        trailersObservable.postValue(trailersResponse?.results)
        return trailersObservable
    }

    suspend fun findReviewsByMovieId(movieId: Int?): LiveData<List<Review>> {

        val reviewsObservable = MediatorLiveData<List<Review>>()

        val reviewsResponse = apiCall(
                call = {theMovieDb.getReviews(movieId.toString())},
                errorMessage = "Error Fetching Reviews"
        )

        if (reviewsResponse?.results != null)  {
            val reviews = reviewsResponse.results
            if (reviews.isNotEmpty()){
                reviewsObservable.postValue(reviews)
            }
        }

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
