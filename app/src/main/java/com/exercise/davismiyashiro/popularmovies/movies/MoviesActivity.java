package com.exercise.davismiyashiro.popularmovies.movies;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.data.Response;
import com.exercise.davismiyashiro.popularmovies.R;
import com.exercise.davismiyashiro.popularmovies.data.loaders.FavoritesLoader;
import com.exercise.davismiyashiro.popularmovies.data.loaders.MoviesLoader;
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsActivity;

import java.util.LinkedList;
import java.util.List;

import static com.exercise.davismiyashiro.popularmovies.data.loaders.FavoritesLoader.ID_LOADER_FAVORITES;

public class MoviesActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Response>,
        MovieListAdapter.OnMovieClickListener {

    private RecyclerView mRecyclerView;
    private TextView mErrorMsg;

    private MovieListAdapter mMovieListAdapter;

    private static final String LOADER_TYPE = "savedInstance";

    private static final int ID_LOADER = 51;

    private Bundle queryBundle = new Bundle();
    private Bundle loaderBundle = new Bundle();
    public final static String QUERY_BUNDLE_ID = "Retrieve this in the Loader";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(LOADER_TYPE)) {
                int loaderType = savedInstanceState.getInt(LOADER_TYPE);
                setSortOptionToAPICall(loaderType);
            }
        } else {
            queryBundle.putString(QUERY_BUNDLE_ID, MoviesLoader.POPULARITY_DESC_PARAM);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_movie_list);

        mErrorMsg = (TextView) findViewById(R.id.tv_error_message_display);

        GridLayoutManager layout = new GridLayoutManager(this, calculateNoOfColumns(this));
        mRecyclerView.setLayoutManager(layout);
        mRecyclerView.setHasFixedSize(true);

        mMovieListAdapter = new MovieListAdapter(new LinkedList<MovieDetails>(), this, this);
        mRecyclerView.setAdapter(mMovieListAdapter);

        getSupportLoaderManager().initLoader(ID_LOADER, queryBundle, this);


        getSupportLoaderManager().initLoader(ID_LOADER_FAVORITES, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int loaderType = loaderBundle.getInt(LOADER_TYPE);
        outState.putInt(LOADER_TYPE, loaderType);
        outState.putString(QUERY_BUNDLE_ID, queryBundle.getString(QUERY_BUNDLE_ID));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
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
    public Loader<Response> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_LOADER:
                return new MoviesLoader(getApplicationContext(), queryBundle);
            case ID_LOADER_FAVORITES:
                return new FavoritesLoader(getApplicationContext());
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Response> loader, Response data) {
        if (data != null && data.getResults() != null) {
            List<MovieDetails> movieDetails = data.getResults();
            mMovieListAdapter.replaceData(movieDetails);
            showMoviewList();
        } else
            showErrorMsg();
    }

    private void showErrorMsg() {
        mErrorMsg.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void showMoviewList() {
        mErrorMsg.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Response> loader) {
        mMovieListAdapter.replaceData(null);
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
            case R.id.action_search:
                queryBundle.putString(QUERY_BUNDLE_ID, MoviesLoader.POPULARITY_DESC_PARAM);
                setSortOptionToAPICall(ID_LOADER);
                getSupportActionBar().setTitle("Popular Movies");
                return true;

            case R.id.action_highest_rated:
                queryBundle.putString(QUERY_BUNDLE_ID, MoviesLoader.HIGHEST_RATED_PARAM);
                setSortOptionToAPICall(ID_LOADER);
                getSupportActionBar().setTitle("Highest Rated Movies");
                return true;
            case R.id.action_favorites:
                setSortOptionToAPICall(ID_LOADER_FAVORITES);
                getSupportActionBar().setTitle("Favorite Movies");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setSortOptionToAPICall(int loaderType) {
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader loader = loaderManager.getLoader(loaderType);

        switch (loaderType) {
            case ID_LOADER:
                if (loader == null) {
                    loaderManager.initLoader(ID_LOADER, queryBundle, this);
                } else {
                    loaderManager.restartLoader(ID_LOADER, queryBundle, this);
                }
                loaderBundle.putInt(LOADER_TYPE, ID_LOADER);
                break;
            case ID_LOADER_FAVORITES:
                if (loader == null) {
                    loaderManager.initLoader(ID_LOADER_FAVORITES, null, this);
                } else {
                    loaderManager.restartLoader(ID_LOADER_FAVORITES, null, this);
                }
                loaderBundle.putInt(LOADER_TYPE, ID_LOADER_FAVORITES);
                break;

            default:
                break;
        }
    }

    @Override
    public void getMovieClicked(MovieDetails movieDetails) {
        Intent intent = new Intent(this, MovieDetailsActivity.class);
        intent.putExtra(MovieDetailsActivity.MOVIE_DETAILS, movieDetails);
        startActivity(intent);
    }
}
