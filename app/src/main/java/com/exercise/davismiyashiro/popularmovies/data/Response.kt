package com.exercise.davismiyashiro.popularmovies.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Created by Davis Miyashiro on 05/02/2017.
 */

@JsonClass(generateAdapter = true)
data class Response<T>(
        @field:Json(name = "page") var page: Int = 0,
        @field:Json(name = "results") var results: List<T>,
        @field:Json(name = "total_results") var totalResults: Int = 0,
        @field:Json(name = "total_pages") var totalPages: Int = 0
)