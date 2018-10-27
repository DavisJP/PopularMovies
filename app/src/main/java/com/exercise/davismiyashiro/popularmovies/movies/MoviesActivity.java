package com.exercise.davismiyashiro.popularmovies.movies;

import android.arch.lifecycle.ViewModelProviders;
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
import com.exercise.davismiyashiro.popularmovies.data.Repository;
import com.exercise.davismiyashiro.popularmovies.databinding.ActivityMoviesBinding;
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsActivity;

import java.util.LinkedList;
import java.util.List;

public class MoviesActivity extends AppCompatActivity implements
        MovieListAdapter.OnMovieClickListener {

    public static final String POPULARITY_DESC_PARAM = "popular";
    public static final String HIGHEST_RATED_PARAM = "top_rated";
    public static final String FAVORITES_PARAM = "favorites";

    private ActivityMoviesBinding binding;

    private MovieListAdapter mMovieListAdapter;

    private String mSortOpt = POPULARITY_DESC_PARAM;
    private String SORT_KEY = "SORT_KEY";

    private MoviesViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Repository repository = ((App)getApplication()).getRepository();

        MoviesViewModel.Factory factory = new MoviesViewModel.Factory(getApplication(), repository);

        viewModel = ViewModelProviders.of(this, factory).get(MoviesViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_movies);
        binding.setLifecycleOwner(this);

        binding.setViewmodel(viewModel);

        if (savedInstanceState != null) {
            mSortOpt = savedInstanceState.getString(SORT_KEY);
        }

        GridLayoutManager layout = new GridLayoutManager(this, calculateNoOfColumns(this));
        binding.rvMovieList.setLayoutManager(layout);
        binding.rvMovieList.setHasFixedSize(true);

        mMovieListAdapter = new MovieListAdapter(new LinkedList<>(),this);
        binding.rvMovieList.setAdapter(mMovieListAdapter);


        viewModel.getMoviesBySortingOption(mSortOpt).observe(this, movies -> {
            if (movies != null && !movies.isEmpty()) {
                updateMovieData(movies);
                showMovieList();
            } else {
                showErrorMsg();
            }
        });

        setTitleBar (mSortOpt);
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

    public void showErrorMsg() {
        binding.tvErrorMessageDisplay.setVisibility(View.VISIBLE);
        binding.rvMovieList.setVisibility(View.INVISIBLE);
    }

    public void showMovieList() {
        binding.tvErrorMessageDisplay.setVisibility(View.INVISIBLE);
        binding.rvMovieList.setVisibility(View.VISIBLE);
    }

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
                viewModel.getMoviesBySortingOption(mSortOpt);
                setTitleBar(mSortOpt);

                return true;

            case R.id.action_highest_rated:

                mSortOpt = HIGHEST_RATED_PARAM;
                viewModel.getMoviesBySortingOption(mSortOpt);
                setTitleBar(mSortOpt);

                return true;

            case R.id.action_favorites:

                mSortOpt = FAVORITES_PARAM;
                viewModel.getMoviesBySortingOption(mSortOpt);
                setTitleBar(mSortOpt);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

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
        viewModel.cancelAsyncTasks();
    }
}
