package com.exercise.davismiyashiro.popularmovies.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

import com.exercise.davismiyashiro.popularmovies.data.MovieDetails

/**
 * Created by Davis Miyashiro.
 */
@Dao
interface MoviesDao {

    @get:Query("SELECT * FROM $TABLE_NAME")
    val allMovies: LiveData<List<MovieDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(movie: MovieDetails)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMovies(movies: List<MovieDetails>)

    @Update
    fun updateMovies(vararg movies: MovieDetails)

    @Delete
    fun deleteMovies(vararg movies: MovieDetails)

    @Query("SELECT * FROM $TABLE_NAME WHERE movieid = :id")
    fun getMovieById(id: Int?): LiveData<MovieDetails>

}
