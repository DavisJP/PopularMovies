package com.exercise.davismiyashiro.popularmovies.data

import androidx.room.Entity

import androidx.room.PrimaryKey
import android.os.Parcelable
import com.exercise.davismiyashiro.popularmovies.data.local.TABLE_NAME

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

/**
 * Created by Davis Miyashiro on 05/02/2017.
 */
@Parcelize
@JsonClass(generateAdapter = true)
@Entity(tableName = TABLE_NAME)
data class MovieDetails(
        @field:Json(name = "id") @PrimaryKey var movieid: Int,
        @field:Json(name = "title") var title: String,
        @field:Json(name = "poster_path") var posterPath: String,
        @field:Json(name = "overview") var overview: String,
        @field:Json(name = "release_date") var releaseDate: String,
        @field:Json(name = "vote_average") var voteAverage: Double
) : Parcelable