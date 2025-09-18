package com.exercise.davismiyashiro.popularmovies

import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class MockServerDispatcher {
    fun successDispatcher(map: Map<String, String>): Dispatcher {
        return object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val popularMoviesPath = "/3/movie/popular"
                return if (request.path?.startsWith(popularMoviesPath) == true) {
                    if (map.containsKey(popularMoviesPath)) {
                        MockResponse().setResponseCode(200).setBody(
                            JsonFileReaderHelper.getStringFromFile(
                                InstrumentationRegistry.getInstrumentation().context,
                                map[popularMoviesPath]!!
                            )
                        )
                    } else {
                        responseNotFound()
                    }
                } else {
                    responseNotFound()
                }
            }
        }
    }

    private fun responseNotFound() = MockResponse().setResponseCode(404).setBody(
        JsonFileReaderHelper.getStringFromFile(
            InstrumentationRegistry.getInstrumentation().context,
            "not_foundJSON.json"
        )
    )
}