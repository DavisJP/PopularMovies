package com.exercise.davismiyashiro.popularmovies.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Created by Davis Miyashiro on 19/02/2017.
 */

@JsonClass(generateAdapter = true)
data class Trailer (
        @field:Json(name = "id") val id: String,
        @field:Json(name = "iso_639_1") var iso6391: String,
        @field:Json(name = "iso_3166_1") var iso31661: String,
        @field:Json(name = "key") var key: String,
        @field:Json(name = "name") var name: String,
        @field:Json(name = "site") var site: String,
        @field:Json(name = "size") var size: Int,
        @field:Json(name = "type") var type: String
)