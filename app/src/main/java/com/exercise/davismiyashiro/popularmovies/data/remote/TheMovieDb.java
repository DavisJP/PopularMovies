package com.exercise.davismiyashiro.popularmovies.data.remote;

import com.exercise.davismiyashiro.popularmovies.data.Trailer;
import com.exercise.davismiyashiro.popularmovies.data.Response;
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.data.Review;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Davis Miyashiro on 20/02/2017.
 */

public interface TheMovieDb {
    String API_KEY_PARAM = "api_key";

    @GET("/3/movie/{sorting}")
    Call<Response<MovieDetails>> getPopular(
            @Path("sorting") String sort,
            @Query(API_KEY_PARAM) String apiKey);

    @GET("/3/movie/{id}/videos")
    Call<Response<Trailer>> getTrailers(
            @Path("id") String movieId,
            @Query(API_KEY_PARAM) String apiKey);

    @GET("/3/movie/{id}/reviews")
    Call<Response<Review>> getReviews(
            @Path("id") String movieId,
            @Query(API_KEY_PARAM) String apiKey);
}