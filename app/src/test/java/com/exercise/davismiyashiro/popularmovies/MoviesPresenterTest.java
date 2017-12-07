package com.exercise.davismiyashiro.popularmovies;

import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb;
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsInterfaces;
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsPresenter;
import com.exercise.davismiyashiro.popularmovies.movies.MoviesActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class MoviesPresenterTest {

    @Mock
    MovieDetailsInterfaces.View view;

    @Mock
    private TheMovieDb serviceApi;

    private String SORTING = MoviesActivity.POPULARITY_DESC_PARAM;

    private MovieDetailsPresenter presenter;

    @Before
    public void setUp() throws Exception {
        presenter = new MovieDetailsPresenter(serviceApi);
        presenter.attachView(view);
    }

    @After
    public void tearDown() {
        presenter.dettachView();
    }

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
}