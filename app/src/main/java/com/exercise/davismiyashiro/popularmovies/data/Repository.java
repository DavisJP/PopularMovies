package com.exercise.davismiyashiro.popularmovies.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.exercise.davismiyashiro.popularmovies.BuildConfig;
import com.exercise.davismiyashiro.popularmovies.data.local.MovieDataService;
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDao;
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient;
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb;
import com.exercise.davismiyashiro.popularmovies.movies.MoviesActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import timber.log.Timber;

import static com.exercise.davismiyashiro.popularmovies.movies.MoviesActivity.FAVORITES_PARAM;

/**
 * Created by Davis Miyashiro.
 */

public class Repository {

    private TheMovieDb theMovieDb;
    private MoviesDao moviesDao;

    private MovieDataService.AsyncTaskInsert asyncTaskInsert;
    private MovieDataService.AsyncTaskDelete asyncTaskDelete;
    private MovieDataService.AsyncTaskQueryMovie asyncTaskQueryMovie;
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
                    throw new UnknownError(throwable.getMessage());
                }
                moviesObservable.setValue(new ArrayList<>());
            }

            @Override
            public void onRequestSuccess(Response<MovieDetails> result) {
                List<MovieDetails> movies = result.getResults();
                if (movies != null) {
                    moviesObservable.setValue(movies);
                } else {
                    moviesObservable.setValue(new ArrayList<>());
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
                moviesObservable.setValue(new ArrayList<>());
            }
        });
        asyncTaskQueryAll.execute();
        return moviesObservable;
    }

    public LiveData<MovieDetails> getMovieFromDb(int movieId) {
        final MediatorLiveData<MovieDetails> moviesObservable = new MediatorLiveData<>();

        moviesObservable.addSource(moviesDao.getMovieById(movieId), result -> {
            if (result != null && result.getMovieid() != 0) {
                Timber.e("moviesDao result: " + result);
                moviesObservable.setValue(result);
            } else {
                Timber.e("moviesDao returned null");
                moviesObservable.setValue(null);
            }
        });
        return moviesObservable;
    }

    public void cancelAsyncTasks() {
        if (asyncTaskQueryAll != null) asyncTaskQueryAll.cancel(true);
        if (asyncTaskInsert != null) asyncTaskInsert.cancel(true);
        if (asyncTaskDelete != null) asyncTaskDelete.cancel(true);
    }

    public LiveData<List<Trailer>> findTrailersByMovieId(Integer movieId) {

        final Call call = theMovieDb.getTrailers(String.valueOf(movieId), BuildConfig.API_KEY);

        final MediatorLiveData<List<Trailer>> trailersObservable = new MediatorLiveData<>();

        MovieDbApiClient.enqueue(call, new MovieDbApiClient.RequestListener<Response<Trailer>>() {
            @Override
            public void onRequestFailure(Throwable throwable) {
                Timber.d("DAVISLOG", "FAIL! = " + throwable.getLocalizedMessage());
                throwable.printStackTrace();
                //TODO: Add exception handling
                trailersObservable.setValue(new ArrayList<>());
            }

            @Override
            public void onRequestSuccess(Response<Trailer> result) {
                List<Trailer> trailers = result.getResults();
                if (trailers != null && !trailers.isEmpty()) {
                    trailersObservable.setValue(trailers);
                }
            }
        });
        return trailersObservable;
    }

    public LiveData<List<Review>> findReviewsByMovieId(Integer movieId) {

        final Call call = theMovieDb.getReviews(String.valueOf(movieId), BuildConfig.API_KEY);

        final MediatorLiveData<List<Review>> reviewsObservable = new MediatorLiveData<>();

        MovieDbApiClient.enqueue(call, new MovieDbApiClient.RequestListener<Response<Review>>() {
            @Override
            public void onRequestFailure(Throwable throwable) {
                if (call.isCanceled()) {
                    Timber.e("DAVISLOG", "request was cancelled");
                } else {
                    Timber.d("DAVISLOG", "FAIL! = " + throwable.getLocalizedMessage());
                    throwable.printStackTrace();
                    //TODO: Add exception handling
                    reviewsObservable.setValue(new ArrayList<>());
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
        return reviewsObservable;
    }

    public void insertMovieDb(MovieDetails movieDetails) {
        asyncTaskInsert = new MovieDataService.AsyncTaskInsert(moviesDao, () -> loadMoviesFromDb(FAVORITES_PARAM));
        asyncTaskInsert.execute(movieDetails);
    }

    public void deleteMovieDb(MovieDetails movieDetails) {
        asyncTaskDelete = new MovieDataService.AsyncTaskDelete(moviesDao, () -> loadMoviesFromDb(FAVORITES_PARAM));
        asyncTaskDelete.execute(movieDetails);
    }
}
