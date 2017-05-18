package com.exercise.davismiyashiro.popularmovies.data.loaders;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.data.Response;
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient;
import com.exercise.davismiyashiro.popularmovies.movies.MoviesActivity;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;

/**
 * Created by Davis Miyashiro on 04/02/2017.
 */

public class MoviesLoader extends AsyncTaskLoader<Response> {

    public static final String POPULARITY_DESC_PARAM = "popular";
    public static final String HIGHEST_RATED_PARAM = "top_rated";
    String sortingOption = POPULARITY_DESC_PARAM;

    public MoviesLoader(Context context, Bundle bundle) {
        super(context);

        String sortingParam;
        if (bundle != null) {
            sortingParam = bundle.getString(MoviesActivity.QUERY_BUNDLE_ID);
            if (sortingParam != null && !sortingParam.isEmpty())
                sortingOption = sortingParam;
        }
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        if (isConnected())
            forceLoad();
        else {
            //deliverResult(new ArrayList<MovieDetails>());
            deliverResult(new Response());
        }

    }

    @Override
    public Response loadInBackground() {

        Response<MovieDetails> response = new Response();

        //Call<Response<MovieDetails>> call = MovieDbApiClient.createTheMovieDbRequest().getPopular(sortingOption, API_KEY_PROP);
        Call<Response<MovieDetails>> call = MovieDbApiClient.getInstance().getPopularMovies(sortingOption);
        try {
            List<MovieDetails> movieDetails = call.execute().body().getResults();
            response.setResults(movieDetails);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return response;
    }

    @Override
    public void deliverResult(Response data) {
        super.deliverResult(data);
    }


    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
