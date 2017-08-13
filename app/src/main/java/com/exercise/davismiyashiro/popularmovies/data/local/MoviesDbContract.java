package com.exercise.davismiyashiro.popularmovies.data.local;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Davis Miyashiro on 16/03/2017.
 */

public final class MoviesDbContract {

    public static final String CONTENT_AUTHORITY = "com.exercise.davismiyashiro.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIES = "movies";

    private MoviesDbContract() {
        //Prevent instantiation
    }

    public static final class MoviesEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIES)
                .build();

        public static final String TABLE_NAME = "movies";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_MOVIE_TITLE = "movie_title";
        public static final String COLUMN_MOVIE_POSTER = "movie_poster";
        public static final String COLUMN_MOVIE_SYNOPSIS = "movie_synopsis";
        public static final String COLUMN_USER_RATINGS = "user_ratings";
        public static final String COLUMN_RELEASE_DATE = "release_date";
    }
}
