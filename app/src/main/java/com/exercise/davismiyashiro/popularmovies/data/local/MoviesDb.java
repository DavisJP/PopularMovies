package com.exercise.davismiyashiro.popularmovies.data.local;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;

/**
 * Created by Davis Miyashiro.
 */
@Database(entities = {MovieDetails.class}, version = 1, exportSchema = false)
public abstract class MoviesDb extends RoomDatabase {

    public static final String DATABASE_NAME = "moviesdatabase.db";
    public static final String TABLE_NAME = "movies";

    public abstract MoviesDao moviesDao();

    private static MoviesDb INSTANCE;

    public synchronized static MoviesDb getDatabase (final Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    MoviesDb.class, DATABASE_NAME)
                    .build();
        }
        return INSTANCE;
    }
}
