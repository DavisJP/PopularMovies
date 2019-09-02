package com.exercise.davismiyashiro.popularmovies

import android.app.Application

import com.exercise.davismiyashiro.popularmovies.data.Repository
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDao
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDb
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb
import com.facebook.stetho.Stetho

import timber.log.Timber

/**
 * Created by Davis Miyashiro on 07/12/2017.
 */

class App : Application() {

    private var db: MoviesDb? = null
    private var moviesDao: MoviesDao? = null
    private var serviceApi: TheMovieDb? = null

    lateinit var repository: Repository

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Stetho.initializeWithDefaults(this)
        }

        val apiClient = MovieDbApiClient()
        serviceApi = apiClient.getService()

        db = MoviesDb.getDatabase(this)
        moviesDao = db?.moviesDao()

        repository = Repository(serviceApi!!, moviesDao!!)
    }

}
