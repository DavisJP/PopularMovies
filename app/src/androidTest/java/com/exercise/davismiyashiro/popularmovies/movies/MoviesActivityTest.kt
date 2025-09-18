package com.exercise.davismiyashiro.popularmovies.movies

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.exercise.davismiyashiro.popularmovies.JsonFileReaderHelper
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MoviesActivityTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val activityRule = ActivityTestRule(MoviesActivity::class.java, false, false)
    private lateinit var server: MockWebServer

    @Before
    @Throws(Exception::class)
    fun setUp() {
        hiltRule.inject()

        server = MockWebServer()
        server.start()
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        server.shutdown()
    }

    @Test
    @Throws(Exception::class)
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        Assert.assertEquals("com.exercise.davismiyashiro.popularmovies", appContext.packageName)
    }

    @Test
    @Throws(Exception::class)
    fun testQuoteIsShown() {
        val fileName = "popularJSON.json"
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(JsonFileReaderHelper.getStringFromFile(InstrumentationRegistry.getInstrumentation().context, fileName)))

        val intent = Intent()
        activityRule.launchActivity(intent)
    }
}