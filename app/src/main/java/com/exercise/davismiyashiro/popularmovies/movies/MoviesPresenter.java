package com.exercise.davismiyashiro.popularmovies.movies;

import android.support.annotation.NonNull;
import android.util.Log;

import com.exercise.davismiyashiro.popularmovies.BuildConfig;
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.data.Response;
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient;
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb;

import java.util.List;

import retrofit2.Call;

/**
 * Created by Davis Miyashiro on 06/12/2017.
 */

public class MoviesPresenter implements MoviesInterfaces.Presenter {

    @NonNull
    private MoviesInterfaces.View view;
    private TheMovieDb serviceApi;

    public MoviesPresenter(@NonNull TheMovieDb apiClient) {
        serviceApi = apiClient;
    }

    public void attachView(@NonNull MoviesInterfaces.View mainView) {
        view = mainView;
    }

    public void dettachView() {
        view = null;
    }

    public void loadMovies(String sorting) {
        final Call call  = serviceApi.getPopular(sorting, BuildConfig.API_KEY);

        MovieDbApiClient.enqueue(call, new MovieDbApiClient.RequestListener<Response<MovieDetails>>() {
            @Override
            public void onRequestFailure(Throwable throwable) {
                Log.d("DAVISLOG", "FAIL! = " + throwable.getLocalizedMessage());
                throwable.printStackTrace();
                //TODO: Add exception handling
            }

            @Override
            public void onRequestSuccess(Response<MovieDetails> result) {
                List<MovieDetails> movies = result.getResults();
                if (view!= null) {
                    if (movies != null && !movies.isEmpty()) {
                        view.updateMovieData(movies);
                        view.showMovieList();
                    } else {
                        view.showErrorMsg();
                    }
                } else {
                    call.cancel();
                }
            }
        });
    }
}
