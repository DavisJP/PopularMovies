package com.exercise.davismiyashiro.popularmovies.moviedetails;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MediatorLiveData;
import android.support.annotation.NonNull;

import com.exercise.davismiyashiro.popularmovies.App;
import com.exercise.davismiyashiro.popularmovies.BuildConfig;
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.data.Response;
import com.exercise.davismiyashiro.popularmovies.data.Review;
import com.exercise.davismiyashiro.popularmovies.data.Trailer;
import com.exercise.davismiyashiro.popularmovies.data.local.MovieDataService;
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDao;
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDb;
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient;
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import timber.log.Timber;

/**
 * Created by Davis Miyashiro.
 */

public class MovieDetailsViewModel extends AndroidViewModel{

    TheMovieDb serviceApi;

    private MoviesDb db;
    private MoviesDao moviesDao;

    private MovieDataService.AsyncTaskInsert asyncTaskInsert;
    private MovieDataService.AsyncTaskDelete asyncTaskDelete;
    private MovieDataService.AsyncTaskQueryAll asyncTaskQueryAll;

    private MediatorLiveData<List<MovieDetails>> moviesObservable;

    private MediatorLiveData<List<Trailer>> trailersObservable;
    private MediatorLiveData<List<Review>> reviewsObservable;
    public MovieDetailsViewModel(@NonNull Application application) {
        super(application);

        serviceApi = ((App)application).getMovieDbApi();

        db = MoviesDb.getDatabase(getApplication());
        moviesDao = db.moviesDao();

        moviesObservable = new MediatorLiveData<>();
        moviesObservable.setValue(new ArrayList<>());

        trailersObservable = new MediatorLiveData<>();
        trailersObservable.setValue(new ArrayList<>());

        reviewsObservable = new MediatorLiveData<>();
        trailersObservable.setValue(new ArrayList<>());
    }

    public MediatorLiveData<List<MovieDetails>> getMoviesObservable() {
        return moviesObservable;
    }

    public MediatorLiveData<List<Trailer>> getTrailersObservable() {
        return trailersObservable;
    }

    public MediatorLiveData<List<Review>> getReviewsObservable() {
        return reviewsObservable;
    }

    public void refreshFavoriteMoviesList() {
        asyncTaskQueryAll = new MovieDataService.AsyncTaskQueryAll(moviesDao, movieList -> {
            if (movieList != null) {
                moviesObservable.setValue(movieList);
            }
        });
        asyncTaskQueryAll.execute();
    }

    public void loadTrailers(Integer movieId) {

        final Call call  = serviceApi.getTrailers(String.valueOf(movieId), BuildConfig.API_KEY);

        MovieDbApiClient.enqueue(call, new MovieDbApiClient.RequestListener<Response<Trailer>>() {
            @Override
            public void onRequestFailure(Throwable throwable) {
                Timber.d("DAVISLOG", "FAIL! = " + throwable.getLocalizedMessage());
                throwable.printStackTrace();
                //TODO: Add exception handling
            }

            @Override
            public void onRequestSuccess(Response<Trailer> result) {
                List<Trailer> trailers = result.getResults();
                if (trailers != null && !trailers.isEmpty()) {
                    trailersObservable.setValue(trailers);
                }
            }
        });
    }

    public void loadReviews(Integer movieId) {

        final Call call  = serviceApi.getReviews(String.valueOf(movieId), BuildConfig.API_KEY);

        MovieDbApiClient.enqueue(call, new MovieDbApiClient.RequestListener<Response<Review>>() {
            @Override
            public void onRequestFailure(Throwable throwable) {
                if (call.isCanceled()) {
                    Timber.e("DAVISLOG", "request was cancelled");
                } else {
                    Timber.d("DAVISLOG", "FAIL! = " + throwable.getLocalizedMessage());
                    throwable.printStackTrace();
                    //TODO: Add exception handling
                }
            }

            @Override
            public void onRequestSuccess(Response<Review> result) {
                List<Review> reviews = result.getResults();
                if (reviews != null && !reviews.isEmpty()) {
                    reviewsObservable.setValue(reviews);
                }
            }
        });
    }

    public void insertMovie (MovieDetails movieDetails) {
        asyncTaskInsert = new MovieDataService.AsyncTaskInsert(moviesDao, () -> refreshFavoriteMoviesList());
        asyncTaskInsert.execute(movieDetails);
    }

    public void deleteMovie (MovieDetails movieDetails) {
        asyncTaskDelete = new MovieDataService.AsyncTaskDelete(moviesDao, () -> refreshFavoriteMoviesList());
        asyncTaskDelete.execute(movieDetails);
    }

    public void cancelAsyncTasks() {
        if (asyncTaskInsert != null) asyncTaskInsert.cancel(true);
        if (asyncTaskDelete != null) asyncTaskDelete.cancel(true);
        if (asyncTaskQueryAll != null) asyncTaskQueryAll.cancel(true);
    }
}
