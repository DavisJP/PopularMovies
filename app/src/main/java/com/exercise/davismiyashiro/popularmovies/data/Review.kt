package com.exercise.davismiyashiro.popularmovies.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Created by Davis Miyashiro on 26/02/2017.
 */

@JsonClass(generateAdapter = true)
data class Review(
        @field:Json(name = "id") var id: String,
        @field:Json(name = "author") var author: String,
        @field:Json(name = "content") var content: String,
        @field:Json(name = "url") var url: String
)