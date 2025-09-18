package com.exercise.davismiyashiro.popularmovies.movies

import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.exercise.davismiyashiro.popularmovies.MockServerDispatcher
import com.exercise.davismiyashiro.popularmovies.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.AfterClass
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * TODO: Dispatcher response needs to be setup for each test
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MoviesListTest {

    companion object {
        private lateinit var server: MockWebServer

        var serviceMap: Map<String, String> = mapOf(
            Pair("/3/movie/popular", "popularJSON.json")
        )

        @BeforeClass
        @JvmStatic
        fun setup() {
            server = MockWebServer()
            server.dispatcher = MockServerDispatcher().successDispatcher(serviceMap)
            server.start(port = 8080)
        }

        @AfterClass()
        @JvmStatic
        fun tearDown() {
            server.shutdown()
        }
    }

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MoviesActivity>()

    @Before
    @Throws(Exception::class)
    fun setUp() {
        hiltRule.inject()
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
    fun onSuccessNoErrorDisplayed() {
        val request = server.takeRequest(5, TimeUnit.SECONDS)
        Assert.assertNotNull("MockWebServer did not receive a request", request)
        if (request != null) {
            println("MockWebServer received request: ${request.path}")
        }

        val expectedMessage =
            composeTestRule.activity.getString(R.string.popular_movies)
        val errorMessage =
            composeTestRule.activity.getString(R.string.please_check_your_network_status_or_try_again_later)
        composeTestRule.onNodeWithText(expectedMessage).assertIsDisplayed()
        composeTestRule.onNodeWithText(errorMessage).assertIsNotDisplayed()
    }

    @Test
    fun whenNetworkErrorThenErrorIsDisplayed() {
        val expectedMessage =
            composeTestRule.activity.getString(R.string.popular_movies)
        val expectedErrorMessage =
            composeTestRule.activity.getString(R.string.please_check_your_network_status_or_try_again_later)
        composeTestRule.onNodeWithText(expectedMessage).assertIsDisplayed()

        val request = server.takeRequest(5, TimeUnit.SECONDS) // Wait for 5s
        Assert.assertNotNull("MockWebServer did not receive a request", request)
        if (request != null) {
            println("MockWebServer received request: ${request.path}")
        }

        val errorMessageFound = try {
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                composeTestRule
                    .onAllNodesWithText(expectedErrorMessage)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            true
        } catch (ex: ComposeTimeoutException) {
            Timber.e("Timeout waiting with message ${ex.message}")
            false
        }

        if (errorMessageFound) {
            composeTestRule.onNodeWithText(expectedErrorMessage).assertIsDisplayed()
        } else {
            assert(false) { "Error message '$expectedErrorMessage' was not displayed within the timeout." }
        }
    }
}