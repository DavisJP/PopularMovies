package com.exercise.davismiyashiro.popularmovies.data.remote

import com.exercise.davismiyashiro.popularmovies.data.MovieDetails
import com.exercise.davismiyashiro.popularmovies.data.Response
import com.exercise.davismiyashiro.popularmovies.data.Review
import com.exercise.davismiyashiro.popularmovies.data.Trailer

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interfaces
 *
 * Created by Davis Miyashiro on 20/02/2017.
 */

private const val API_KEY_PARAM = "api_key"
private const val API_SORTING_PARAM = "sorting"
private const val API_MOVIE_ID_PARAM = "id"

interface TheMovieDb {

    @GET("/3/movie/{sorting}")
    fun getPopular(
            @Path(API_SORTING_PARAM) sort: String,
            @Query(API_KEY_PARAM) apiKey: String): Call<Response<MovieDetails>>

    @GET("/3/movie/{id}/videos")
    fun getTrailers(
            @Path(API_MOVIE_ID_PARAM) movieId: String,
            @Query(API_KEY_PARAM) apiKey: String): Call<Response<Trailer>>

    @GET("/3/movie/{id}/reviews")
    fun getReviews(
            @Path(API_MOVIE_ID_PARAM) movieId: String,
            @Query(API_KEY_PARAM) apiKey: String): Call<Response<Review>>
}