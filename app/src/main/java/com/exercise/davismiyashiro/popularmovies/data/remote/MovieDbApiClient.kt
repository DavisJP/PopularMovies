package com.exercise.davismiyashiro.popularmovies.data.remote

import com.exercise.davismiyashiro.popularmovies.BuildConfig
import com.exercise.davismiyashiro.popularmovies.data.Response

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber

/**
 * Retrofit client
 *
 * Created by Davis Miyashiro on 20/02/2017.
 */

class MovieDbApiClient {

    private var service: TheMovieDb? = null

    private val okHttp: OkHttpClient
        get() {
            val logging = HttpLoggingInterceptor()
            if (BuildConfig.DEBUG) {
                logging.level = HttpLoggingInterceptor.Level.BODY
            } else {
                logging.level = HttpLoggingInterceptor.Level.NONE
            }

            val httpClient = OkHttpClient.Builder()

            return httpClient.addInterceptor(logging).build()
        }

    fun getService(): TheMovieDb? {
        if (service == null) {
            val retrofitSingle = Retrofit.Builder()
                    .baseUrl(THEMOVIEDB_API)
                    .client(okHttp)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()
            service = retrofitSingle.create(TheMovieDb::class.java)
        }
        return service
    }

    interface RequestListener<T> {
        fun onRequestFailure(throwable: Throwable)

        fun onRequestSuccess(result: T?)
    }

    companion object {

        private val THEMOVIEDB_API = "https://api.themoviedb.org"

        /**
         * Asynchronous Retrofit call
         *
         * @param call
         * @param listener
         */
        fun <T> enqueue(call: Call<T>, listener: RequestListener<T>?) {

            if (call == null) {
                return
            }

            call.enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: retrofit2.Response<T>) {
                    if (response.isSuccessful) {
                        listener?.onRequestSuccess(response.body())
                    } else {
                        val code = response.code()

                        if (code == 401) {
                            Timber.d("onRequestUnauthenticated" + response.message())
                        } else if (code in 400..499) {
                            Timber.d("onRequestClientError" + response.message())
                        } else if (code in 500..599) {
                            Timber.d("onRequestServerError" + response.message())
                        } else {
                            onFailure(call, UnknownError(response.code().toString() + " " + response.message()))
                        }
                    }
                }

                override fun onFailure(call: Call<T>, throwable: Throwable) {
                    listener?.onRequestFailure(throwable)
                }
            })
        }
    }
}
