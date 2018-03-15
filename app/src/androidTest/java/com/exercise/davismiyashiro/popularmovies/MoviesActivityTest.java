package com.exercise.davismiyashiro.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.exercise.davismiyashiro.popularmovies.movies.MoviesActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class MoviesActivityTest {

    @Rule
    public final ActivityTestRule<MoviesActivity> activityRule = new ActivityTestRule<>(MoviesActivity.class, false, false);
    private MockWebServer server;


    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.exercise.davismiyashiro.popularmovies", appContext.getPackageName());
    }

    @Test
    public void testQuoteIsShown() throws Exception {
        String fileName = "popularJSON.json";
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(JsonFileReaderHelper.getStringFromFile(getInstrumentation().getContext(), fileName)));

        Intent intent = new Intent();
        activityRule.launchActivity(intent);
    }
}
