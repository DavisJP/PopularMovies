package com.exercise.davismiyashiro.popularmovies.data.local;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDbContract.MoviesEntry;

import java.util.List;

/**
 * Created by Davis Miyashiro.
 */
@Dao
public interface MoviesDao {

    String SELECT_ALL_MOVIES = "SELECT * FROM " + MoviesEntry.TABLE_NAME;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MovieDetails movie);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMovies(List<MovieDetails> movies);

    @Update
    void updateMovies(MovieDetails... movies);

    @Delete
    void deleteMovies(MovieDetails... movies);

    @Query("SELECT * FROM " + MoviesEntry.TABLE_NAME)
    List<MovieDetails> getAllMovies();
}
