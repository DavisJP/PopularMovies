package com.exercise.davismiyashiro.popularmovies.moviedetails;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.data.Repository;
import com.exercise.davismiyashiro.popularmovies.data.Review;
import com.exercise.davismiyashiro.popularmovies.data.Trailer;

import java.util.List;

import static com.exercise.davismiyashiro.popularmovies.movies.MoviesActivity.FAVORITES_PARAM;

/**
 * Created by Davis Miyashiro.
 */

public class MovieDetailsViewModel extends AndroidViewModel {

    private Repository repository;

    public MovieDetailsViewModel(@NonNull Application application, Repository repositoryParam) {
        super(application);

        repository = repositoryParam;
    }

    public LiveData<List<MovieDetails>> getMovieDetailsObservable() {
        return repository.loadMoviesFromDb(FAVORITES_PARAM);
    }

    public LiveData<List<Trailer>> getTrailersObservable(Integer movieId) {
        return repository.findTrailersByMovieId(movieId);
    }

    public LiveData<List<Review>> getReviewsObservable(Integer movieId) {
        return repository.findReviewsByMovieId(movieId);
    }

    public void insertMovie(MovieDetails movieDetails) {
        repository.insertMovieDb(movieDetails);
    }

    public void deleteMovie(MovieDetails movieDetails) {
        repository.deleteMovieDb(movieDetails);
    }

    public void cancelAsyncTasks() {
        repository.cancelAsyncTasks();
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private final Application application;
        private final Repository repository;

        public Factory(Application application, Repository repository) {
            this.application = application;
            this.repository = repository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new MovieDetailsViewModel(application, repository);
        }
    }
}
