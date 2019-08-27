package com.exercise.davismiyashiro.popularmovies.moviedetails;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.data.Repository;
import com.exercise.davismiyashiro.popularmovies.data.Review;
import com.exercise.davismiyashiro.popularmovies.data.Trailer;

import java.util.List;

import timber.log.Timber;

/**
 * Created by Davis Miyashiro.
 */

public class MovieDetailsViewModel extends AndroidViewModel {

    private Repository repository;

    private MutableLiveData<MovieDetailsObservable> movieObservable = new MutableLiveData<>();
    private MutableLiveData<Boolean> favoriteCheckBoxLivedata = new MutableLiveData<>();
    private MutableLiveData<Integer> id = new MutableLiveData<>();
    private MutableLiveData<String> title = new MutableLiveData<>();
    private MutableLiveData<String> posterPath = new MutableLiveData<>();
    private MutableLiveData<String> releaseDate = new MutableLiveData<>();
    private MutableLiveData<String> overview = new MutableLiveData<>();
    private MutableLiveData<Double> voteAverage = new MutableLiveData<>();

    public MovieDetailsViewModel(@NonNull Application application, Repository repositoryParam) {
        super(application);

        repository = repositoryParam;
    }

    public LiveData<MovieDetailsObservable> getMovieObservable(int movieId) {
        return convertMovieDetailsToUImodel(repository.getMovieFromDb(movieId));
    }

    public MutableLiveData<Boolean> getFavoriteCheckBoxLivedata() {
        return favoriteCheckBoxLivedata;
    }

    public void setFavorite() {
        if (favoriteCheckBoxLivedata != null && favoriteCheckBoxLivedata.getValue()) {
            Timber.e("Is favorite, should delete it!");
            deleteMovie(movieObservable.getValue());
            favoriteCheckBoxLivedata.postValue(false);
        } else {
            Timber.e("Is not favorite, save it!");
            insertMovie(movieObservable.getValue());
            favoriteCheckBoxLivedata.setValue(true);
        }
    }

    public MutableLiveData<Integer> getId() {
        return id;
    }

    public void setId(MutableLiveData<Integer> id) {
        this.id = id;
    }

    public MutableLiveData<String> getTitle() {
        return title;
    }

    public void setTitle(MutableLiveData<String> title) {
        this.title = title;
    }

    public MutableLiveData<String> getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(MutableLiveData<String> posterPath) {
        this.posterPath = posterPath;
    }

    public MutableLiveData<String> getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(MutableLiveData<String> releaseDate) {
        this.releaseDate = releaseDate;
    }

    public MutableLiveData<String> getOverview() {
        return overview;
    }

    public void setOverview(MutableLiveData<String> overview) {
        this.overview = overview;
    }

    public MutableLiveData<Double> getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(MutableLiveData<Double> voteAverage) {
        this.voteAverage = voteAverage;
    }

    public void setMovieDetailsLivedatas(MovieDetailsObservable movieDetailsObservable) {
        id.postValue(movieDetailsObservable.getId());
        title.postValue(movieDetailsObservable.getTitle());
        releaseDate.postValue(movieDetailsObservable.getReleaseDate());
        overview.postValue(movieDetailsObservable.getOverview());
        voteAverage.postValue(movieDetailsObservable.getVoteAverage());
        posterPath.postValue(movieDetailsObservable.getPosterPath());

        movieObservable.postValue(movieDetailsObservable);
    }

    public LiveData<List<Trailer>> getTrailersObservable(Integer movieId) {
        return repository.findTrailersByMovieId(movieId);
    }

    public LiveData<List<Review>> getReviewsObservable(Integer movieId) {
        return repository.findReviewsByMovieId(movieId);
    }

    private LiveData<MovieDetailsObservable> convertMovieDetailsToUImodel(LiveData<MovieDetails> movie) {
        MediatorLiveData<MovieDetailsObservable> movieDetailsObservable = new MediatorLiveData<>();

        favoriteCheckBoxLivedata.postValue(false);
        movieDetailsObservable.addSource(movie, result -> {

            //If it's not null then it's a favorite
            if (result != null) {
                favoriteCheckBoxLivedata.postValue(true);
                Timber.e("Is true!");
                final MovieDetailsObservable value = new MovieDetailsObservable(
                        result.getMovieid(),
                        result.getTitle(),
                        result.getPosterPath(),
                        result.getOverview(),
                        result.getReleaseDate(),
                        result.getVoteAverage());
                movieDetailsObservable.setValue(value);
            } else {
                Timber.e("Is False!");
                favoriteCheckBoxLivedata.postValue(false);
            }
        });
        return movieDetailsObservable;
    }


    public void insertMovie(MovieDetailsObservable movieDetailsObservable) {

        repository.insertMovieDb(new MovieDetails(
                movieDetailsObservable.getId(),
                movieDetailsObservable.getTitle(),
                movieDetailsObservable.getPosterPath(),
                movieDetailsObservable.getOverview(),
                movieDetailsObservable.getReleaseDate(),
                movieDetailsObservable.getVoteAverage()));
    }

    public void deleteMovie(MovieDetailsObservable movieDetailsObservable) {

        repository.deleteMovieDb(new MovieDetails(
                movieDetailsObservable.getId(),
                movieDetailsObservable.getTitle(),
                movieDetailsObservable.getPosterPath(),
                movieDetailsObservable.getOverview(),
                movieDetailsObservable.getReleaseDate(),
                movieDetailsObservable.getVoteAverage()));
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
