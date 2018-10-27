package com.exercise.davismiyashiro.popularmovies.movies;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.databinding.Bindable;
import android.databinding.Observable;
import android.databinding.PropertyChangeRegistry;
import android.support.annotation.NonNull;

import com.android.databinding.library.baseAdapters.BR;
import com.exercise.davismiyashiro.popularmovies.App;
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.data.Repository;
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDao;
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDb;
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb;

import java.util.List;

import static com.exercise.davismiyashiro.popularmovies.movies.MoviesActivity.FAVORITES_PARAM;

/**
 * Created by Davis Miyashiro.
 */

public class MoviesViewModel extends AndroidViewModel implements Observable {

//    private String sortingOption;

    TheMovieDb serviceApi;

    private MoviesDb db;

    private MoviesDao moviesDao;

    private LiveData<List<MovieDetails>> moviesObservable;
    private Repository repository;

    private PropertyChangeRegistry callbacks = new PropertyChangeRegistry();

    public MoviesViewModel(@NonNull Application application) {
        super(application);

        serviceApi = ((App) application).getMovieDbApi();

        db = MoviesDb.getDatabase(getApplication());
        moviesDao = db.moviesDao();

//        moviesObservable = new MediatorLiveData<>();
//        moviesObservable.setValue(null);

        repository = new Repository(serviceApi, moviesDao);
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
}
