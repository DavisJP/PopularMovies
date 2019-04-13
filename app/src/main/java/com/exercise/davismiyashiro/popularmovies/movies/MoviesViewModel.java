package com.exercise.davismiyashiro.popularmovies.movies;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.databinding.Bindable;
import androidx.databinding.Observable;
import androidx.databinding.PropertyChangeRegistry;
import androidx.annotation.NonNull;

import com.exercise.davismiyashiro.popularmovies.BR;
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.data.Repository;
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsObservable;

import java.util.ArrayList;
import java.util.List;

import static com.exercise.davismiyashiro.popularmovies.movies.MoviesActivity.FAVORITES_PARAM;

/**
 * Created by Davis Miyashiro.
 */

public class MoviesViewModel extends AndroidViewModel implements Observable {

    private LiveData<List<MovieDetailsObservable>> moviesObservable;
    private Repository repository;

    private PropertyChangeRegistry callbacks = new PropertyChangeRegistry();

    public MoviesViewModel(@NonNull Application application, Repository repositoryParam) {
        super(application);

        repository = repositoryParam;
    }

    @Bindable
    public LiveData<List<MovieDetailsObservable>> getMoviesObservable() {
        return moviesObservable;
    }

    public void setMoviesObservable(LiveData<List<MovieDetails>> moviesObservable) {
        this.moviesObservable = convertMovieDetailsToUImodel(moviesObservable);
        notifyPropertyChanged(BR.moviesObservable);
    }

    public void setMoviesBySortingOption(String sortingOption) {

        if (sortingOption.equals(FAVORITES_PARAM)) {
            setMoviesObservable(repository.loadMoviesFromDb(sortingOption));
        } else {
            setMoviesObservable(repository.loadMoviesFromNetwork(sortingOption));
        }
    }

    public void cancelAsyncTasks() {
        repository.cancelAsyncTasks();
    }

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        callbacks.remove(callback);
    }

    /**
     * Call this to notify of property changes generate on get Methods annotated with
     * @Bindable and accessible from BR object
     *
     * @param fieldId
     */
    private void notifyPropertyChanged(int fieldId) {
        callbacks.notifyCallbacks(this, fieldId, null);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private final Application application;
        private final Repository repository;

        public Factory (Application application, Repository repository) {
            this.application = application;
            this.repository = repository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new MoviesViewModel(application, repository);
        }
    }

    private LiveData<List<MovieDetailsObservable>> convertMovieDetailsToUImodel(LiveData<List<MovieDetails>> movies) {
        MediatorLiveData<List<MovieDetailsObservable>> movieDetailsObservableList = new MediatorLiveData<>();

        movieDetailsObservableList.addSource(movies, result -> {
        if (result != null) {
            List<MovieDetailsObservable> movieDetailsObservableList1 = new ArrayList<>();
            for (MovieDetails movieDetails : result) {
                movieDetailsObservableList1.add(new MovieDetailsObservable(
                        movieDetails.getMovieid(),
                        movieDetails.getTitle(),
                        movieDetails.getPosterPath(),
                        movieDetails.getOverview(),
                        movieDetails.getReleaseDate(),
                        movieDetails.getVoteAverage()));
            }
            movieDetailsObservableList.setValue(movieDetailsObservableList1);
        }

        });
        return movieDetailsObservableList;
    }
}
