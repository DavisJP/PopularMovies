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

package com.exercise.davismiyashiro.popularmovies.data.local

import android.app.IntentService
import androidx.lifecycle.LiveData
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import androidx.lifecycle.MutableLiveData

import com.exercise.davismiyashiro.popularmovies.data.MovieDetails

import timber.log.Timber

/**
 * Created by Davis Miyashiro on 07/12/2017.
 */

class MovieDataService : IntentService(TAG) {


    @Deprecated("using AsyncTasks for more control over background tasks")
    override fun onHandleIntent(intent: Intent?) {
        if (ACTION_INSERT == intent?.action) {
            val values = intent.getParcelableExtra<ContentValues>(EXTRA_VALUES)
            insertMovieDb(values)
        } else if (ACTION_DELETE == intent?.action) {
            deleteMovieDb(intent.data)
        }
    }

    private fun deleteMovieDb(uri: Uri?) {
        val rows = contentResolver.delete(uri!!, null, null)

        if (rows > 0) {
            Timber.d(TAG, "Delete Movie ok")
            val deleteOk = Intent(ACTION_DELETE)
            sendBroadcast(deleteOk)
        } else {
            Timber.e(TAG, "Error deleting Movie")
        }
    }

    private fun insertMovieDb(values: ContentValues) {
        if (contentResolver.insert(CONTENT_URI, values) != null) {
            Timber.d(TAG, "Insert Movie ok")
            val insertOk = Intent(ACTION_INSERT)
            sendBroadcast(insertOk)
        } else {
            Timber.e(TAG, "Error inserting Movie")
        }
    }

    class AsyncTaskQueryMovie(private val localDao: MoviesDao,
                              private val asyncResponse: AsyncTaskQueryResponse) :
            AsyncTask<Int, Void, LiveData<MovieDetails>>() {

        private var movie: LiveData<MovieDetails>? = null

        interface AsyncTaskQueryResponse {
            fun processFinish(movie: LiveData<MovieDetails>?)
        }

        override fun doInBackground(vararg id: Int?): LiveData<MovieDetails>? {
            if (!isCancelled) {
                //TODO: DB works but this is not updating
                movie = localDao.getMovieById(id[0])
                return localDao.getMovieById(id[0])
            }
            return null
        }

        override fun onPostExecute(movieDetails: LiveData<MovieDetails>) {
            super.onPostExecute(movieDetails)
            asyncResponse.processFinish(movie)
        }
    }

    class AsyncTaskQueryAll(private val localDao: MoviesDao,
                            private val asyncResponse: AsyncTaskQueryAllResponse?) :
            AsyncTask<Void, Void, LiveData<List<MovieDetails>>>() {
        private var movieList: LiveData<List<MovieDetails>> = MutableLiveData()

        interface AsyncTaskQueryAllResponse {
            fun processFinish(movieList: LiveData<List<MovieDetails>>)
        }

        override fun doInBackground(vararg voids: Void): LiveData<List<MovieDetails>> {
            if (!isCancelled) {
                movieList = localDao.allMovies
            }
            return movieList
        }

        override fun onPostExecute(movieDetails: LiveData<List<MovieDetails>>) {
            super.onPostExecute(movieDetails)
            asyncResponse?.processFinish(movieList)
        }
    }

    class AsyncTaskInsert(private val localDao: MoviesDao,
                          private val asyncResponse: AsyncResponse?) : AsyncTask<MovieDetails, Void, Void>() {

        interface AsyncResponse {
            fun processFinish()
        }

        override fun doInBackground(vararg movieDetails: MovieDetails): Void? {
            if (!isCancelled) {
                if (movieDetails != null) {
                    localDao.insert(movieDetails[0])
                }
            }
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            asyncResponse?.processFinish()
        }
    }

    class AsyncTaskDelete(private val localDao: MoviesDao,
                          private val asyncResponse: AsyncResponse?) : AsyncTask<MovieDetails, Void, Void>() {

        interface AsyncResponse {
            fun processFinish()
        }

        override fun doInBackground(vararg movieDetails: MovieDetails): Void? {
            if (!isCancelled) {
                if (movieDetails != null) {
                    localDao.deleteMovies(movieDetails[0])
                }
            }
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            asyncResponse?.processFinish()
        }
    }

    companion object {

        private val TAG = MovieDataService::class.java.simpleName

        private const val CONTENT_AUTHORITY = "com.exercise.davismiyashiro.popularmovies"
        private val BASE_CONTENT_URI = Uri.parse("content://$CONTENT_AUTHORITY")
        private const val PATH_MOVIES = "movies"

        val CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIES)
                .build()

        val ACTION_INSERT = "$TAG.INSERT"
        val ACTION_DELETE = "$TAG.DELETE"

        val EXTRA_VALUES = "$TAG.ContentValues"
    }
}
