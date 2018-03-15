package com.exercise.davismiyashiro.popularmovies;

import android.app.Application;

import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient;
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb;

import timber.log.Timber;

/**
 * Created by Davis Miyashiro on 07/12/2017.
 */

public class App extends Application {

    private final TheMovieDb movieDbApi;

    public App() {

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        MovieDbApiClient apiClient = new MovieDbApiClient();
        movieDbApi = apiClient.getService();
    }

    public TheMovieDb getMovieDbApi () {
        return movieDbApi;
    }
}
