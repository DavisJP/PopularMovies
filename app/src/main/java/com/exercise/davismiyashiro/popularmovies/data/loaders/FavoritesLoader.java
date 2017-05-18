package com.exercise.davismiyashiro.popularmovies.data.loaders;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.data.Response;

import java.util.ArrayList;
import java.util.List;

import static com.exercise.davismiyashiro.popularmovies.data.local.MoviesDbContract.*;

/**
 * Created by Davis Miyashiro on 18/03/2017.
 */

public class FavoritesLoader extends AsyncTaskLoader<Response> {

    public static final int ID_LOADER_FAVORITES = 91;
    private Context mContext;
    private List<MovieDetails> listFavoriteMovies;

    Response<MovieDetails> response;

    public FavoritesLoader(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onStartLoading() {
        if (response != null) {
            // Delivers any previously loaded data immediately
            deliverResult(response);
        } else {
            // Force a new load
            forceLoad();
        }
    }

    @Override
    public Response<MovieDetails> loadInBackground() {
        Cursor cursor;

        try {
            cursor = mContext.getContentResolver().query(
                    MoviesEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);

        } catch (Exception e) {
            Log.e(FavoritesLoader.class.getSimpleName(), "Failed to asynchronously load data.");
            e.printStackTrace();
            return null;
        }

        if (cursor != null) {
            listFavoriteMovies = new ArrayList<>();
            response = new Response<>();

            while (cursor.moveToNext()) {
                String columnMovieId = MoviesEntry.COLUMN_MOVIE_ID;
                String columnMovieTitle = MoviesEntry.COLUMN_MOVIE_TITLE;
                String columnMoviePoster = MoviesEntry.COLUMN_MOVIE_POSTER;
                String columnSynopsis = MoviesEntry.COLUMN_MOVIE_SYNOPSIS;
                String columnUserRatings = MoviesEntry.COLUMN_USER_RATINGS;
                String columnReleaseDate = MoviesEntry.COLUMN_RELEASE_DATE;

                Integer movieId = cursor.getInt(cursor.getColumnIndex(columnMovieId));
                String title = cursor.getString(cursor.getColumnIndex(columnMovieTitle));
                String posterPath = cursor.getString(cursor.getColumnIndex(columnMoviePoster));
                String synopsis = cursor.getString(cursor.getColumnIndex(columnSynopsis));
                Double userRatings = cursor.getDouble(cursor.getColumnIndex(columnUserRatings));
                String releaseDate = cursor.getString(cursor.getColumnIndex(columnReleaseDate));

                MovieDetails movieDetails = new MovieDetails(movieId, title, posterPath, synopsis, userRatings, releaseDate);
                listFavoriteMovies.add(movieDetails);

                response.setResults(listFavoriteMovies);
            }
        }
        return response;
    }

    @Override
    public void deliverResult(Response favoriteMovies) {
        response = favoriteMovies;
        super.deliverResult(favoriteMovies);
    }
}
