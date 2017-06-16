package com.exercise.davismiyashiro.popularmovies.data.loaders;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import com.exercise.davismiyashiro.popularmovies.BuildConfig;
import com.exercise.davismiyashiro.popularmovies.data.Response;
import com.exercise.davismiyashiro.popularmovies.data.Trailer;
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;

/**
 * AsyncTaskLoader for Trailers
 *
 * Created by Davis Miyashiro on 26/02/2017.
 */

public class TrailersLoader extends AsyncTaskLoader<Response> {

    public static final int ID_LOADER_TRAILERS = 71;
    private int movieId;

    public TrailersLoader(Context context, Bundle args) {
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

        Response<Trailer> response = new Response();

        Call<Response<Trailer>> call = MovieDbApiClient.getService().getTrailers(String.valueOf(movieId), BuildConfig.API_KEY);
        try {
            //TODO: Save it to DB so that it can be retrieved as a Cursor
            List<Trailer> trailers = call.execute().body().getResults();
            response.setResults(trailers);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return response;
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
