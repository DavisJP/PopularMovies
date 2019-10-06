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

package com.exercise.davismiyashiro.popularmovies.data.remote

import com.exercise.davismiyashiro.popularmovies.data.MovieDetails
import com.exercise.davismiyashiro.popularmovies.data.Response
import com.exercise.davismiyashiro.popularmovies.data.Review
import com.exercise.davismiyashiro.popularmovies.data.Trailer

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
    suspend fun getPopular(
            @Path(API_SORTING_PARAM) sort: String): retrofit2.Response<Response<MovieDetails>>

    @GET("/3/movie/{id}/videos")
    suspend fun getTrailers(
            @Path(API_MOVIE_ID_PARAM) movieId: String): retrofit2.Response<Response<Trailer>>

    @GET("/3/movie/{id}/reviews")
    suspend fun getReviews(
            @Path(API_MOVIE_ID_PARAM) movieId: String): retrofit2.Response<Response<Review>>
}