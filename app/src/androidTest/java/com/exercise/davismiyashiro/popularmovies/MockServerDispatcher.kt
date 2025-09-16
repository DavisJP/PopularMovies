package com.exercise.davismiyashiro.popularmovies

import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class MockServerDispatcher {
    fun successDispatcher(map: Map<String, String>): Dispatcher {
        return object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when (request.path) {
                    "/3/movie/popular" -> {
                        var json = ""
                        if (map.containsKey("/3/movie/popular")) {
                            json = map["/3/movie/popular"]!!
                        }
                        MockResponse().setResponseCode(200).setBody(
                            JsonFileReaderHelper.getStringFromFile(
                                InstrumentationRegistry.getInstrumentation().context,
                                json
                            )
                        )
                    }

                    else -> MockResponse().setResponseCode(404).setBody(
                        JsonFileReaderHelper.getStringFromFile(
                            InstrumentationRegistry.getInstrumentation().context,
                            "not_foundJSON.json"
                        )
                    )
                }
            }
        }
    }
}