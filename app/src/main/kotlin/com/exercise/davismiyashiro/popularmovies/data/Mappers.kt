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

import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDTO
import com.exercise.davismiyashiro.popularmovies.data.remote.ReviewDTO
import com.exercise.davismiyashiro.popularmovies.data.remote.TrailerDTO
import com.exercise.davismiyashiro.popularmovies.domain.Movie
import com.exercise.davismiyashiro.popularmovies.domain.Review
import com.exercise.davismiyashiro.popularmovies.domain.Trailer
import com.exercise.davismiyashiro.popularmovies.data.local.MovieEntity
import com.exercise.davismiyashiro.popularmovies.moviedetails.IMG_BASE_URL
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsUI

fun MovieDTO.toDomain(): Movie {
    return Movie(
        id = id,
        title = title,
        backdropPath = backdropPath,
        posterPath = posterPath,
        overview = overview,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
    )
}

fun MovieEntity.toDomain(): Movie {
    return Movie(
        id = id,
        title = title,
        backdropPath = backdropPath,
        posterPath = posterPath,
        overview = overview,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
    )
}

fun Movie.toEntity(): MovieEntity {
    return MovieEntity(
        id = id,
        title = title,
        backdropPath = backdropPath,
        posterPath = posterPath,
        overview = overview,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
    )
}

fun TrailerDTO.toDomain(): Trailer {
    return Trailer(
        id = id,
        key = key,
        name = name,
        site = site,
        size = size,
        type = type,
    )
}

fun ReviewDTO.toDomain(): Review {
    return Review(
        id = id,
        author = author,
        content = content,
        url = url,
    )
}

fun Movie.toUI(): MovieDetailsUI {
    return MovieDetailsUI(
        id = id,
        title = title,
        backdropPath = IMG_BASE_URL + backdropPath,
        posterPath = IMG_BASE_URL + posterPath,
        overview = overview,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
    )
}

fun MovieDetailsUI.toDomain(): Movie {
    return Movie(
        id = id,
        title = title,
        backdropPath = backdropPath.replace(IMG_BASE_URL, ""),
        posterPath = posterPath.replace(IMG_BASE_URL, ""),
        overview = overview,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
    )
}
