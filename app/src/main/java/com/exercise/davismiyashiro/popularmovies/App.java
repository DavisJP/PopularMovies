package com.exercise.davismiyashiro.popularmovies;

import android.app.Application;

import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient;
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb;

/**
 * Created by Davis Miyashiro on 07/12/2017.
 */

public class App extends Application {

    private static final String THEMOVIEDB_API = "https://api.themoviedb.org";
    private final TheMovieDb movieDbApi;

    public App() {
        MovieDbApiClient apiClient = new MovieDbApiClient();
        movieDbApi = apiClient.getService();
    }

    public TheMovieDb getMovieDbApi () {
        return movieDbApi;
    }
}
