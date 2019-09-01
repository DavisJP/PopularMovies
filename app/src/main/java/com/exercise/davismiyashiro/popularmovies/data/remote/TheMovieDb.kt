package com.exercise.davismiyashiro.popularmovies.data.remote

import com.exercise.davismiyashiro.popularmovies.data.MovieDetails
import com.exercise.davismiyashiro.popularmovies.data.Response
import com.exercise.davismiyashiro.popularmovies.data.Review
import com.exercise.davismiyashiro.popularmovies.data.Trailer
import kotlinx.coroutines.Deferred

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit interfaces
 *
 * Created by Davis Miyashiro on 20/02/2017.
 */

private const val API_SORTING_PARAM = "sorting"
private const val API_MOVIE_ID_PARAM = "id"

interface TheMovieDb {

    @GET("/3/movie/{sorting}")
    fun getPopular(
            @Path(API_SORTING_PARAM) sort: String): Deferred<retrofit2.Response<Response<MovieDetails>>>

    @GET("/3/movie/{id}/videos")
    fun getTrailers(
            @Path(API_MOVIE_ID_PARAM) movieId: String): Deferred<retrofit2.Response<Response<Trailer>>>

    @GET("/3/movie/{id}/reviews")
    fun getReviews(
            @Path(API_MOVIE_ID_PARAM) movieId: String): Deferred<retrofit2.Response<Response<Review>>>
}