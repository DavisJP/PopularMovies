package com.exercise.davismiyashiro.popularmovies.data.loaders;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import com.exercise.davismiyashiro.popularmovies.data.Response;
import com.exercise.davismiyashiro.popularmovies.data.Review;
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;

/**
 * Created by Davis Miyashiro on 26/02/2017.
 */

public class ReviewsLoader extends AsyncTaskLoader<Response> {

    public static final int ID_LOADER_REVIEWS = 81;
    int movieId;

    public ReviewsLoader(Context context, Bundle args) {
        super(context);

        if (args != null) {
            movieId = args.getInt("MOVIEID");
        }
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        if (isConnected())
            forceLoad();
        else {
            deliverResult(new Response());
        }
    }

    @Override
    public Response loadInBackground() {
        Response<Review> response = new Response();

        //Call<Response<Review>> call = MovieDbApiClient.createTheMovieDbRequest().getReviews(String.valueOf(movieId), API_KEY_PROP);
        Call<Response<Review>> call = MovieDbApiClient.getInstance().getMovieReviews(String.valueOf(movieId));
        try {
            List<Review> reviews = call.execute().body().getResults();
            response.setResults(reviews);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
