package com.exercise.davismiyashiro.popularmovies.data.local;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;

import static com.exercise.davismiyashiro.popularmovies.data.local.MoviesDbHelper.DATABASE_NAME;

/**
 * Created by Davis Miyashiro.
 */
@Database(entities = {MovieDetails.class}, version = 1, exportSchema = false)
public abstract class MoviesDb extends RoomDatabase {

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
