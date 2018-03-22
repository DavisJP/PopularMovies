package com.exercise.davismiyashiro.popularmovies.movies;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.exercise.davismiyashiro.popularmovies.App;
import com.exercise.davismiyashiro.popularmovies.R;
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDao;
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDb;
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb;
import com.exercise.davismiyashiro.popularmovies.databinding.ActivityMoviesBinding;
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsActivity;

import java.util.LinkedList;
import java.util.List;

import static com.exercise.davismiyashiro.popularmovies.data.local.MovieDataService.AsyncTaskQueryAll;

public class MoviesActivity extends AppCompatActivity implements MoviesInterfaces.View,
        MovieListAdapter.OnMovieClickListener {

    public static final String POPULARITY_DESC_PARAM = "popular";
    public static final String HIGHEST_RATED_PARAM = "top_rated";
    public static final String FAVORITES_PARAM = "favorites";

    private ActivityMoviesBinding binding;

    private MovieListAdapter mMovieListAdapter;

    private MoviesInterfaces.Presenter presenter;

    private String mSortOpt = POPULARITY_DESC_PARAM;
    private String SORT_KEY = "SORT_KEY";

    private MoviesDb db;
    private MoviesDao moviesDao;
    private AsyncTaskQueryAll asyncTaskQueryAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_movies);

        presenter = new MoviesPresenter(getTheMovieDbClient());
        presenter.attachView(this);

        if (savedInstanceState != null) {
            mSortOpt = savedInstanceState.getString(SORT_KEY);
        }

        GridLayoutManager layout = new GridLayoutManager(this, calculateNoOfColumns(this));
        binding.rvMovieList.setLayoutManager(layout);
        binding.rvMovieList.setHasFixedSize(true);

        mMovieListAdapter = new MovieListAdapter(new LinkedList<>(),this);
        binding.rvMovieList.setAdapter(mMovieListAdapter);

        if (mSortOpt.equals(FAVORITES_PARAM)) {
            refreshFavoriteMovies();
        } else {
            presenter.loadMovies(mSortOpt);
        }

        setTitleBar (mSortOpt);

        db = MoviesDb.getDatabase(getApplication());
        moviesDao = db.moviesDao();
    }

    private void setTitleBar (String favoritesParam) {
        switch (favoritesParam) {
            case FAVORITES_PARAM:
                getSupportActionBar().setTitle(R.string.favorites);
                break;

            case HIGHEST_RATED_PARAM:
                getSupportActionBar().setTitle(R.string.highest_rated_movies);
                break;

            case POPULARITY_DESC_PARAM:
                getSupportActionBar().setTitle(R.string.popular_movies);
                break;

            default:
                getSupportActionBar().setTitle(R.string.popular_movies);
                break;
        }
    }

    private TheMovieDb getTheMovieDbClient () {
        return ((App)getApplication()).getMovieDbApi();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(SORT_KEY, mSortOpt);
        super.onSaveInstanceState(outState);
    }

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 180);
        return noOfColumns;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void showErrorMsg() {
        binding.tvErrorMessageDisplay.setVisibility(View.VISIBLE);
        binding.rvMovieList.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showMovieList() {
        binding.tvErrorMessageDisplay.setVisibility(View.INVISIBLE);
        binding.rvMovieList.setVisibility(View.VISIBLE);
    }

    @Override
    public void updateMovieData(List<MovieDetails> listMovies) {
        mMovieListAdapter.replaceData(listMovies);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.movies_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuSelected = item.getItemId();

        switch (menuSelected) {
            case R.id.action_popular:

                mSortOpt = POPULARITY_DESC_PARAM;

                presenter.loadMovies(mSortOpt);
                setTitleBar(mSortOpt);

                return true;

            case R.id.action_highest_rated:

                mSortOpt = HIGHEST_RATED_PARAM;

                presenter.loadMovies(mSortOpt);
                setTitleBar(mSortOpt);

                return true;

            case R.id.action_favorites:

                mSortOpt = FAVORITES_PARAM;

                refreshFavoriteMovies();
                setTitleBar(mSortOpt);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void refreshFavoriteMovies() {
        asyncTaskQueryAll = new AsyncTaskQueryAll(moviesDao, movieList -> {
            if (movieList != null) {
                updateMovieData(movieList);
                showMovieList();
            } else {
                showErrorMsg(); //TODO: Show no favorites
            }
        });
        asyncTaskQueryAll.execute();
    }

    @Override
    public void getMovieClicked(MovieDetails movieDetails) {
        Intent intent = new Intent(this, MovieDetailsActivity.class);
        intent.putExtra(MovieDetailsActivity.MOVIE_DETAILS, movieDetails);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.dettachView();
        cancelAsyncTasks();
    }

    public void cancelAsyncTasks() {
        if (asyncTaskQueryAll != null) asyncTaskQueryAll.cancel(true);
    }
}
