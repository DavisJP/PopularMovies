package com.exercise.davismiyashiro.popularmovies;

import android.app.Application;

import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient;
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb;
import com.facebook.stetho.Stetho;

import timber.log.Timber;

/**
 * Created by Davis Miyashiro on 07/12/2017.
 */

public class App extends Application {

    private TheMovieDb movieDbApi;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            Stetho.initializeWithDefaults(this);
        }

        MovieDbApiClient apiClient = new MovieDbApiClient();
        movieDbApi = apiClient.getService();
    }

    public TheMovieDb getMovieDbApi () {
        return movieDbApi;
    }
}
