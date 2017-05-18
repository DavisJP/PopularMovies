package com.exercise.davismiyashiro.popularmovies.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.exercise.davismiyashiro.popularmovies.data.local.MoviesDbContract.*;

/**
 * Created by Davis Miyashiro on 16/03/2017.
 */

public class MoviesDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "moviesdatabase.db";

    private static final int DATABASE_VERSION = 2;

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIES_TABLE =

                "CREATE TABLE " + MoviesEntry.TABLE_NAME + " (" +
                        MoviesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MoviesEntry.COLUMN_MOVIE_TITLE + " STRING NOT NULL, " +
                        MoviesEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +

                        MoviesEntry.COLUMN_MOVIE_POSTER + " STRING," +
                        MoviesEntry.COLUMN_MOVIE_SYNOPSIS + " STRING, " +
                        MoviesEntry.COLUMN_USER_RATINGS + " REAL, " +
                        MoviesEntry.COLUMN_RELEASE_DATE + " STRING," +
                /*
                 * To ensure this table can only contain one movie entry per id, we declare
                 * the movie_id column to be unique. We also specify "ON CONFLICT REPLACE". This tells
                 * SQLite that if we have a movie entry for a certain id and we attempt to
                 * insert another movie entry with that id, we replace the old movie entry.
                 */
                        " UNIQUE (" + MoviesEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

        //TODO: Avoid dropping when on production, check suggestion:
        //https://thebhwgroup.com/blog/how-android-sqlite-onupgrade
    }
}
