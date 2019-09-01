package com.exercise.davismiyashiro.popularmovies.data.remote

import com.exercise.davismiyashiro.popularmovies.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.io.IOException
import java.lang.Exception

private const val API_KEY_PARAM = "api_key"
private const val THEMOVIEDB_API = "https://api.themoviedb.org"

/**
 * Retrofit client
 *
 * Created by Davis Miyashiro on 20/02/2017.
 */
class MovieDbApiClient {

    private var service: TheMovieDb? = null

    private val authInterceptor = Interceptor { chain ->
        val newUrl = chain.request().url()
                .newBuilder()
                .addQueryParameter(API_KEY_PARAM, BuildConfig.API_KEY)
                .build()

        val newRequest = chain.request()
                .newBuilder()
                .url(newUrl)
                .build()
        chain.proceed(newRequest)
    }

    private val okHttp: OkHttpClient
        get() {
            val logging = HttpLoggingInterceptor()
            if (BuildConfig.DEBUG) {
                logging.level = HttpLoggingInterceptor.Level.BODY
            } else {
                logging.level = HttpLoggingInterceptor.Level.NONE
            }

            return OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(authInterceptor)
                    .build()
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

    sealed class Result<out T : Any> {
        data class Success<out T : Any>(val data: T) : Result<T>()
        data class Error(val exception: Exception) : Result<Nothing>()
    }

    open class BaseNetworkHandler {
        suspend fun <T : Any> apiCall(call: suspend () -> Response<T>, errorMessage: String): T? {
            val result : Result<T> = apiResult(call, errorMessage)
            var data: T? = null

            when (result) {
                is Result.Success -> data = result.data
                is Result.Error -> {
                    if (result.exception is UnknownError) {
                        result.exception.printStackTrace()
                        throw UnknownError(result.exception.message)
                    }
                    Timber.e("$errorMessage : Error occurred during apiResult: ${result.exception}")
                }
            }
            return data
        }

        private suspend fun <T: Any> apiResult(call: suspend () -> Response<T>, errorMessage: String): Result<T> {
            val response = call.invoke()
            return if (response.isSuccessful)
                Result.Success(response.body()!!)
            else {
                when (response.code()) {
                    401 -> Result.Error(IOException(errorMessage.plus("onRequestUnauthenticated: ${response.message()}")))
                    in 400..499 -> Result.Error(IOException(errorMessage.plus("onRequestClientError: ${response.message()}")))
                    in 500..599 -> Result.Error(IOException(errorMessage.plus("onRequestServerError: ${response.message()}")))
                    else -> Result.Error(IOException(errorMessage.plus ("UnknownError: ${response.code()} ${response.message()}")))
                }
            }
        }
    }
}
