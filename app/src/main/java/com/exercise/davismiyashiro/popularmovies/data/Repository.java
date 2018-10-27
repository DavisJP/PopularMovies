package com.exercise.davismiyashiro.popularmovies.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;

import com.exercise.davismiyashiro.popularmovies.BuildConfig;
import com.exercise.davismiyashiro.popularmovies.data.local.MovieDataService;
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDao;
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient;
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb;
import com.exercise.davismiyashiro.popularmovies.movies.MoviesActivity;

import java.util.List;

import retrofit2.Call;
import timber.log.Timber;

/**
 * Created by Davis Miyashiro.
 */

public class Repository {

    private TheMovieDb theMovieDb;
    private MoviesDao moviesDao;

    //    private MutableLiveData<List<MovieDetails>> moviesObservable;
    private MovieDataService.AsyncTaskQueryAll asyncTaskQueryAll;

    public Repository(TheMovieDb networkService, MoviesDao dbService) {
        theMovieDb = networkService;
        moviesDao = dbService;
    }

    public LiveData<List<MovieDetails>> loadMoviesFromNetwork(String sortingOption) {
        final Call call = theMovieDb.getPopular(sortingOption, BuildConfig.API_KEY);

        final MutableLiveData<List<MovieDetails>> moviesObservable = new MediatorLiveData<>();

        MovieDbApiClient.enqueue(call, new MovieDbApiClient.RequestListener<Response<MovieDetails>>() {
            @Override
            public void onRequestFailure(Throwable throwable) {
                Timber.e("FAIL! = " + throwable.getLocalizedMessage());

                if (throwable.getCause() instanceof UnknownError) {
                    throwable.printStackTrace();
//                    throw new UnknownError(throwable.getMessage());
                }
                moviesObservable.setValue(null);
            }

            @Override
            public void onRequestSuccess(Response<MovieDetails> result) {
                List<MovieDetails> movies = result.getResults();
                if (movies != null) {
                    moviesObservable.setValue(movies);
                } else {
                    moviesObservable.setValue(null);
                    //TODO: call.cancel() if finished too soon?
                }
            }
        });
        return moviesObservable;
    }

    public LiveData<List<MovieDetails>> loadMoviesFromDb(String sortingOption) {

        final MediatorLiveData<List<MovieDetails>> moviesObservable = new MediatorLiveData<>();

        asyncTaskQueryAll = new MovieDataService.AsyncTaskQueryAll(moviesDao, movieList -> {
            if (movieList != null) {
                moviesObservable.addSource(movieList, values -> {
                    if (sortingOption.equals(MoviesActivity.FAVORITES_PARAM)) {
                        moviesObservable.setValue(values);
                    }
                });
            } else {
                moviesObservable.setValue(null); //TODO: Show no favorites
            }
        });
        asyncTaskQueryAll.execute();
        return moviesObservable;
    }

    public void cancelAsyncTasks() {
        if (asyncTaskQueryAll != null) asyncTaskQueryAll.cancel(true);
    }
}
