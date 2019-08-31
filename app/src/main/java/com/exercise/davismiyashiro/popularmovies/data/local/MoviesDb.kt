package com.exercise.davismiyashiro.popularmovies.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

import com.exercise.davismiyashiro.popularmovies.data.MovieDetails

const val TABLE_NAME = "movies"
/**
 * Created by Davis Miyashiro.
 */
@Database(entities = [MovieDetails::class], version = 1, exportSchema = false)
abstract class MoviesDb : RoomDatabase() {

    abstract fun moviesDao(): MoviesDao

    companion object {

        private const val DATABASE_NAME = "moviesdatabase.db"

        private var INSTANCE: MoviesDb? = null

        @Synchronized
        fun getDatabase(context: Context): MoviesDb? {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.applicationContext,
                        MoviesDb::class.java, DATABASE_NAME)
                        .build()
            }
            return INSTANCE
        }
    }
}
