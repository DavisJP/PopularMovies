package com.exercise.davismiyashiro.popularmovies.movies;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.databinding.Bindable;
import android.databinding.Observable;
import android.databinding.PropertyChangeRegistry;
import android.support.annotation.NonNull;

import com.android.databinding.library.baseAdapters.BR;
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.data.Repository;

import java.util.List;

import static com.exercise.davismiyashiro.popularmovies.movies.MoviesActivity.FAVORITES_PARAM;

/**
 * Created by Davis Miyashiro.
 */

public class MoviesViewModel extends AndroidViewModel implements Observable {

//    private String sortingOption;

    private LiveData<List<MovieDetails>> moviesObservable;
    private Repository repository;

    private PropertyChangeRegistry callbacks = new PropertyChangeRegistry();

    public MoviesViewModel(@NonNull Application application, Repository repositoryParam) {
        super(application);

        repository = repositoryParam;
    }

    @Bindable
    public LiveData<List<MovieDetails>> getMoviesObservable() {
        return moviesObservable;
    }

    public void setMoviesObservable(LiveData<List<MovieDetails>> moviesObservable) {
        this.moviesObservable = moviesObservable;
        notifyPropertyChanged(BR.moviesObservable);
    }

    public LiveData<List<MovieDetails>> getMoviesBySortingOption(String sortingOption) {

        if (sortingOption.equals(FAVORITES_PARAM)) {
            moviesObservable = repository.loadMoviesFromDb(sortingOption);
        } else {
            moviesObservable = repository.loadMoviesFromNetwork(sortingOption);
        }

        setMoviesObservable(moviesObservable);

        return moviesObservable;
    }

//    public void setSortingOption(String sortingOption) {
//        this.sortingOption = sortingOption;
//    }

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
}
