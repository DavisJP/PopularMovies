package com.exercise.davismiyashiro.popularmovies;

import android.app.Application;

import com.exercise.davismiyashiro.popularmovies.data.Repository;
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDao;
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDb;
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient;
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb;
import com.facebook.stetho.Stetho;

import timber.log.Timber;

/**
 * Created by Davis Miyashiro on 07/12/2017.
 */

public class App extends Application {

    private MoviesDb db;
    private MoviesDao moviesDao;

    private TheMovieDb serviceApi;
    private Repository repository;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            Stetho.initializeWithDefaults(this);
        }

        MovieDbApiClient apiClient = new MovieDbApiClient();
        serviceApi = apiClient.getService();

        db = MoviesDb.getDatabase(this);
        moviesDao = db.moviesDao();

        repository = new Repository(serviceApi, moviesDao);
    }

    public Repository getRepository() {
        return repository;
    }

}
