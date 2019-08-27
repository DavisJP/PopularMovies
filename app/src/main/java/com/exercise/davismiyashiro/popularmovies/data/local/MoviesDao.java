package com.exercise.davismiyashiro.popularmovies.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;

import java.util.List;

import static com.exercise.davismiyashiro.popularmovies.data.local.MoviesDb.TABLE_NAME;

/**
 * Created by Davis Miyashiro.
 */
@Dao
public interface MoviesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MovieDetails movie);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMovies(List<MovieDetails> movies);

    @Update
    void updateMovies(MovieDetails... movies);

    @Delete
    void deleteMovies(MovieDetails... movies);

    @Query("SELECT * FROM " + TABLE_NAME)
    LiveData<List<MovieDetails>> getAllMovies();

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE movieid = :id")
    LiveData<MovieDetails> getMovieById(Integer id);

}
