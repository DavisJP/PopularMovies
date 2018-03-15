package com.exercise.davismiyashiro.popularmovies;

import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.data.Response;
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb;
import com.exercise.davismiyashiro.popularmovies.movies.MoviesActivity;
import com.exercise.davismiyashiro.popularmovies.movies.MoviesInterfaces;
import com.exercise.davismiyashiro.popularmovies.movies.MoviesPresenter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MoviesPresenterTest {

    @Mock
    MoviesInterfaces.View view;

    @Mock
    private TheMovieDb serviceApi;

    @Mock
    Call<Response<MovieDetails>> movieDetailsResponseCall;

    final MovieDetails FAKE_MOVIE_DETAILS = new MovieDetails(Integer.valueOf(1), "title", "posterPath", "synopsis", Double.valueOf(0), "releaseDate");

    private String SORTING = MoviesActivity.POPULARITY_DESC_PARAM;

    private MoviesPresenter presenter;

    @Before
    public void setUp() throws Exception {
        presenter = new MoviesPresenter(serviceApi);
        presenter.attachView(view);
    }

    @After
    public void tearDown() {
        presenter.dettachView();
    }


    @Test
    public void testLoadMovies_WhenParamGivenToMethod_ApiReceives() {
        final ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        when(serviceApi.getPopular(argumentCaptor.capture(), anyString())).thenReturn(movieDetailsResponseCall);

        presenter.loadMovies(SORTING);

        assertEquals(SORTING, argumentCaptor.getValue());
    }

    @Test
    public void testLoadMovies_WhenApiReturnsList_ShowMovieList () {
        when(serviceApi.getPopular(eq(SORTING), anyString())).thenReturn(movieDetailsResponseCall);
        final Response<MovieDetails> moviesResponse = Mockito.mock(Response.class);

        List<MovieDetails> movieDetails = new ArrayList<>();
        movieDetails.add(FAKE_MOVIE_DETAILS);

        when(moviesResponse.getResults()).thenReturn(movieDetails);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Callback<Response<MovieDetails>> callback = invocation.getArgument(0);

                callback.onResponse(movieDetailsResponseCall, retrofit2.Response.success(moviesResponse));

                return null;
            }
        }).when(movieDetailsResponseCall).enqueue(any(Callback.class));

        presenter.loadMovies(SORTING);

        verify(view, times(1)).showMovieList();
        verify(view, never()).showErrorMsg();
    }

    @Test
    public void testLoadMovies_ApiReturnsEmptyList_ShowErrorMsg () {
        when(serviceApi.getPopular(eq(SORTING), anyString())).thenReturn(movieDetailsResponseCall);

        final Response<MovieDetails> moviesResponse = Mockito.mock(Response.class);

        when(moviesResponse.getResults()).thenReturn(Collections.EMPTY_LIST);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Callback<Response<MovieDetails>> callback = invocation.getArgument(0);

                callback.onResponse(movieDetailsResponseCall, retrofit2.Response.success(moviesResponse));

                return null;
            }
        }).when(movieDetailsResponseCall).enqueue(any(Callback.class));

        presenter.loadMovies(SORTING);

        verify(view).showErrorMsg();
        verify(view, never()).showMovieList();
    }

    @Test
    public void testLoadMovies_ApiReturnsEmptyResultsList_ShowErrorMsg () {
        when(serviceApi.getPopular(eq(SORTING), anyString())).thenReturn(movieDetailsResponseCall);

        final Response<MovieDetails> moviesResponse = Mockito.mock(Response.class);

        when(moviesResponse.getResults()).thenReturn(null);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Callback<Response<MovieDetails>> callback = invocation.getArgument(0);

                callback.onResponse(movieDetailsResponseCall, retrofit2.Response.success(moviesResponse));

                return null;
            }
        }).when(movieDetailsResponseCall).enqueue(any(Callback.class));

        presenter.loadMovies(SORTING);

        verify(view).showErrorMsg();
        verify(view, never()).showMovieList();
    }

    @Test
    public void testLoadMovies_ApiReturnsEmptyResultsList_ShowErrorMsg_Catch () {
        when(serviceApi.getPopular(eq(SORTING), anyString())).thenReturn(movieDetailsResponseCall);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Callback<Response<MovieDetails>> callback = invocation.getArgument(0);
//                callback.onResponse(movieDetailsResponseCall, retrofit2.Response.error(404, ResponseBody());
//                callback.onFailure(movieDetailsResponseCall, new UnknownError());

                return null;
            }
        }).when(movieDetailsResponseCall).enqueue(any(Callback.class));

        try {
            presenter.loadMovies(SORTING);
            fail("My method didn't throw when I expected it to");
        } catch (Error error){
            assertThat(error.getMessage(), is("My method didn't throw when I expected it to"));
        }
    }
}