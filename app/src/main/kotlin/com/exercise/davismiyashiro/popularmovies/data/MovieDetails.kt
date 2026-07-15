/*
 * MIT License
 *
 * Copyright (c) 2019 Davis Miyashiro
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.exercise.davismiyashiro.popularmovies.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.exercise.davismiyashiro.popularmovies.data.local.TABLE_NAME
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Davis Miyashiro on 05/02/2017.
 */
@Serializable
@Entity(tableName = TABLE_NAME)
data class MovieDetails(
    @SerialName("id") @PrimaryKey var movieid: Int,
    @SerialName("title") var title: String,
    @SerialName("backdrop_path") var backdropPath: String?,
    @SerialName("poster_path") var posterPath: String,
    @SerialName("overview") var overview: String,
    @SerialName("release_date") var releaseDate: String,
    @SerialName("vote_average") var voteAverage: Double,
)
