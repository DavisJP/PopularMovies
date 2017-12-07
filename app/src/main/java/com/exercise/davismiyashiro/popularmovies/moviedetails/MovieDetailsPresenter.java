package com.exercise.davismiyashiro.popularmovies.moviedetails;

import android.support.annotation.NonNull;
import android.util.Log;

import com.exercise.davismiyashiro.popularmovies.BuildConfig;
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.data.Response;
import com.exercise.davismiyashiro.popularmovies.data.Review;
import com.exercise.davismiyashiro.popularmovies.data.Trailer;
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient;
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb;

import java.util.List;

import retrofit2.Call;

/**
 * Created by Davis Miyashiro on 07/12/2017.
 */

public class MovieDetailsPresenter implements MovieDetailsInterfaces.Presenter {

    MovieDetailsInterfaces.View view;
    private TheMovieDb serviceApi;

    private List<MovieDetails> mFavoriteMovies;

    public List<MovieDetails> getFavoriteMovies() {
        return mFavoriteMovies;
    }

    public void setFavoriteMovies(List<MovieDetails> mFavoriteMovies) {
        this.mFavoriteMovies = mFavoriteMovies;
    }

    public boolean hasFavoriteMovie(MovieDetails movie) {
        return mFavoriteMovies.contains(movie);
    }

    public MovieDetailsPresenter(@NonNull TheMovieDb apiClient) {
        serviceApi = apiClient;
    }

    public void attachView(@NonNull MovieDetailsInterfaces.View detailsView) {
        view = detailsView;
    }

    public void dettachView() {
        view = null;
    }

    @Override
    public void loadTrailers(Integer movieId) {

        final Call call  = serviceApi.getTrailers(String.valueOf(movieId), BuildConfig.API_KEY);

        MovieDbApiClient.enqueue(call, new MovieDbApiClient.RequestListener<Response<Trailer>>() {
            @Override
            public void onRequestFailure(Throwable throwable) {
                Log.d("DAVISLOG", "FAIL! = " + throwable.getLocalizedMessage());
                throwable.printStackTrace();
                //TODO: Add exception handling
            }

            @Override
            public void onRequestSuccess(Response<Trailer> result) {
                List<Trailer> trailers = result.getResults();
                if (trailers != null && !trailers.isEmpty()) {
                    if (view != null) {
                        view.replaceTrailersData(trailers);
                    } else {
                        call.cancel();
                    }

                }
            }
        });
    }

    @Override
    public void loadReviews(Integer movieId) {

        final Call call  = serviceApi.getReviews(String.valueOf(movieId), BuildConfig.API_KEY);

        MovieDbApiClient.enqueue(call, new MovieDbApiClient.RequestListener<Response<Review>>() {
            @Override
            public void onRequestFailure(Throwable throwable) {
                if (call.isCanceled()) {
                    Log.e("DAVISLOG", "request was cancelled");
                } else {
                    Log.d("DAVISLOG", "FAIL! = " + throwable.getLocalizedMessage());
                    throwable.printStackTrace();
                    //TODO: Add exception handling
                }
            }

            @Override
            public void onRequestSuccess(Response<Review> result) {
                List<Review> reviews = result.getResults();
                if (reviews != null && !reviews.isEmpty()) {

                    if (view != null) {
                        view.replaceReviewsData(reviews);
                    } else {
                        call.cancel();
                    }
                }
            }
        });
    }
}
