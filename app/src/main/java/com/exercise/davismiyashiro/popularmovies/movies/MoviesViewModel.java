package com.exercise.davismiyashiro.popularmovies.movies;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MediatorLiveData;
import android.support.annotation.NonNull;

import com.exercise.davismiyashiro.popularmovies.App;
import com.exercise.davismiyashiro.popularmovies.BuildConfig;
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.data.Response;
import com.exercise.davismiyashiro.popularmovies.data.local.MovieDataService;
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDao;
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDb;
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient;
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb;

import java.util.List;

import retrofit2.Call;
import timber.log.Timber;

/**
 * Created by Davis Miyashiro.
 */

public class MoviesViewModel extends AndroidViewModel {

    private String sortingOption;

    TheMovieDb serviceApi;

    private MoviesDb db;

    private MoviesDao moviesDao;
    private MovieDataService.AsyncTaskQueryAll asyncTaskQueryAll;
    private MediatorLiveData<List<MovieDetails>> moviesObservable;

    public MoviesViewModel(@NonNull Application application) {
        super(application);

        serviceApi = ((App)application).getMovieDbApi();

        db = MoviesDb.getDatabase(getApplication());
        moviesDao = db.moviesDao();

        moviesObservable = new MediatorLiveData<>();
        moviesObservable.setValue(null);
    }

    public MediatorLiveData<List<MovieDetails>> getMoviesObservable() {
        return moviesObservable;
    }

    public void setSortingOption(String sortingOption) {
        this.sortingOption = sortingOption;
    }

    public void loadMovies() {
        final Call call  = serviceApi.getPopular(sortingOption, BuildConfig.API_KEY);

        MovieDbApiClient.enqueue(call, new MovieDbApiClient.RequestListener<Response<MovieDetails>>() {
            @Override
            public void onRequestFailure(Throwable throwable) {
                Timber.e("FAIL! = " + throwable.getLocalizedMessage());
                moviesObservable.setValue(null);

                if (throwable.getCause() instanceof UnknownError) {
                    throwable.printStackTrace();
//                    throw new UnknownError(throwable.getMessage());
                }
            }

            @Override
            public void onRequestSuccess(Response<MovieDetails> result) {
                List<MovieDetails> movies = result.getResults();
                    if (movies != null) {
                        moviesObservable.setValue(movies);
                    }
                    //TODO: call.cancel() if finished too soon?
            }
        });
    }

    public void refreshFavoriteMovies() {
        asyncTaskQueryAll = new MovieDataService.AsyncTaskQueryAll(moviesDao, movieList -> {
            if (movieList != null) {
                moviesObservable.addSource(movieList, values -> {
                    if (sortingOption.equals(MoviesActivity.FAVORITES_PARAM)){
                        moviesObservable.setValue(values);
                    }
                });
            } else {
                moviesObservable.setValue(null); //TODO: Show no favorites
            }
        });
        asyncTaskQueryAll.execute();
    }

    public void cancelAsyncTasks() {
        if (asyncTaskQueryAll != null) asyncTaskQueryAll.cancel(true);
    }
}
