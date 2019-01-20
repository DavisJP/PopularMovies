package com.exercise.davismiyashiro.popularmovies.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

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
    LiveData<List<MovieDetails>> getAllMovies();

    @Query("SELECT * FROM movies WHERE id = :id")
    LiveData<MovieDetails> getMovie(int id);

}
