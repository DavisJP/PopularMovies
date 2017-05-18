package com.exercise.davismiyashiro.popularmovies.data.remote;

import com.exercise.davismiyashiro.popularmovies.BuildConfig;
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.data.Response;
import com.exercise.davismiyashiro.popularmovies.data.Review;
import com.exercise.davismiyashiro.popularmovies.data.Trailer;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Davis Miyashiro on 20/02/2017.
 */

public class MovieDbApiClient {

    public static final String THEMOVIEDB_API = "https://api.themoviedb.org";

    //Singleton
    private static MovieDbApiClient instance;
    private TheMovieDb service;

    public static MovieDbApiClient getInstance() {
        if (instance == null) {
            instance = new MovieDbApiClient();
        }
        return instance;
    }

    private MovieDbApiClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        //logging.setLevel(HttpLoggingInterceptor.Level.BODY); //debug mode
        logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        Retrofit retrofitSingle = new Retrofit.Builder()
                .baseUrl(THEMOVIEDB_API)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        service = retrofitSingle.create(TheMovieDb.class);
    }

    public Call<Response<MovieDetails>> getPopularMovies(String sort) {
        return service.getPopular(sort, BuildConfig.API_KEY);
    }

    public Call<Response<Review>> getMovieReviews(String movieId) {
        return service.getReviews(movieId, BuildConfig.API_KEY);
    }

    public Call<Response<Trailer>> getMovieTrailers(String movieId) {
        return service.getTrailers(movieId, BuildConfig.API_KEY);
    }
}
