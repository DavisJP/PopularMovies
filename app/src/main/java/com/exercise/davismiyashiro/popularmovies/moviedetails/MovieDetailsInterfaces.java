package com.exercise.davismiyashiro.popularmovies.moviedetails;

import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.data.Review;
import com.exercise.davismiyashiro.popularmovies.data.Trailer;

import java.util.List;

/**
 * Created by Davis Miyashiro on 07/12/2017.
 */

public interface MovieDetailsInterfaces {

    interface View {
        void refreshFavoriteMoviesList();

        void replaceTrailersData(List<Trailer> trailers);

        void replaceReviewsData(List<Review> reviews);

        void toggleFavoriteStar(boolean value);
    }

    interface Presenter {
        void loadTrailers(Integer movieId);

        void loadReviews(Integer movieId);

        List<MovieDetails> getFavoriteMovies();

        void setFavoriteMovies(List<MovieDetails> mFavoriteMovies);

        boolean hasFavoriteMovie(MovieDetails movie);
    }
}
