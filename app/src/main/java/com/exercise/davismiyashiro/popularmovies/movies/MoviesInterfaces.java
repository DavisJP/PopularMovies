package com.exercise.davismiyashiro.popularmovies.movies;

import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;

import java.util.List;

/**
 * Created by Davis Miyashiro on 06/12/2017.
 */

public interface MoviesInterfaces {

    interface View {
        void showErrorMsg();

        void showMovieList();

        void updateMovieData(List<MovieDetails> listMovies);
    }

    interface Presenter {
        void attachView(MoviesInterfaces.View mainView);

        void loadMovies(String sorting);

        void dettachView();
    }
}
