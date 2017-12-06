package com.exercise.davismiyashiro.popularmovies.movies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.exercise.davismiyashiro.popularmovies.BuildConfig;
import com.exercise.davismiyashiro.popularmovies.R;
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.data.Response;
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDbContract;
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient;
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsActivity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;

public class MoviesActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        MovieListAdapter.OnMovieClickListener {

    public static final String POPULARITY_DESC_PARAM = "popular";
    public static final String HIGHEST_RATED_PARAM = "top_rated";
    public static final String FAVORITES_PARAM = "favorites";
    public static final int ID_LOADER_FAVORITES = 91;

    @BindView(R.id.rv_movie_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.tv_error_message_display)
    TextView mErrorMsg;

    private MovieListAdapter mMovieListAdapter;

    private String mSortOpt = POPULARITY_DESC_PARAM;
    private String mSortKey = "SORT_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        ButterKnife.bind(this);

        if (savedInstanceState != null) {

//            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//            String option = sharedPref.getString(mSortKey, MoviesLoader.POPULARITY_DESC_PARAM);

            String option = savedInstanceState.getString(mSortKey);

            if (option.equals(FAVORITES_PARAM)) {
//                setSortOptionToAPICall(ID_LOADER_FAVORITES);
                setSortOptionToAPICall();
            } else {
                loadMovies(option);
            }
        }

        GridLayoutManager layout = new GridLayoutManager(this, calculateNoOfColumns(this));
        mRecyclerView.setLayoutManager(layout);
        mRecyclerView.setHasFixedSize(true);

        mMovieListAdapter = new MovieListAdapter(new LinkedList<MovieDetails>(), this, this);
        mRecyclerView.setAdapter(mMovieListAdapter);

        loadMovies(mSortOpt);

        getSupportLoaderManager().initLoader(ID_LOADER_FAVORITES, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(mSortKey, mSortOpt);
        super.onSaveInstanceState(outState);
    }

//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//    }

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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_LOADER_FAVORITES:
                return new CursorLoader(this,
                        MoviesDbContract.MoviesEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);
            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor != null) {
            List<MovieDetails> listFavoriteMovies = new ArrayList<>();

            while (cursor.moveToNext()) {
                String columnMovieId = MoviesDbContract.MoviesEntry.COLUMN_MOVIE_ID;
                String columnMovieTitle = MoviesDbContract.MoviesEntry.COLUMN_MOVIE_TITLE;
                String columnMoviePoster = MoviesDbContract.MoviesEntry.COLUMN_MOVIE_POSTER;
                String columnSynopsis = MoviesDbContract.MoviesEntry.COLUMN_MOVIE_SYNOPSIS;
                String columnUserRatings = MoviesDbContract.MoviesEntry.COLUMN_USER_RATINGS;
                String columnReleaseDate = MoviesDbContract.MoviesEntry.COLUMN_RELEASE_DATE;

                Integer movieId = cursor.getInt(cursor.getColumnIndex(columnMovieId));
                String title = cursor.getString(cursor.getColumnIndex(columnMovieTitle));
                String posterPath = cursor.getString(cursor.getColumnIndex(columnMoviePoster));
                String synopsis = cursor.getString(cursor.getColumnIndex(columnSynopsis));
                Double userRatings = cursor.getDouble(cursor.getColumnIndex(columnUserRatings));
                String releaseDate = cursor.getString(cursor.getColumnIndex(columnReleaseDate));

                MovieDetails movieDetails = new MovieDetails(movieId, title, posterPath, synopsis, userRatings, releaseDate);
                listFavoriteMovies.add(movieDetails);
            }

            mMovieListAdapter.replaceData(listFavoriteMovies);
            showMoviewList();
        } else {
            showErrorMsg();
        }
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
    public void onLoaderReset(Loader<Cursor> loader) {
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

                mSortOpt = POPULARITY_DESC_PARAM;
                loadMovies(mSortOpt);

                getSupportActionBar().setTitle("Popular Movies");
                return true;

            case R.id.action_highest_rated:

                mSortOpt = HIGHEST_RATED_PARAM;
                loadMovies(mSortOpt);

                getSupportActionBar().setTitle("Highest Rated Movies");
                return true;
            case R.id.action_favorites:

                mSortOpt = FAVORITES_PARAM;

                setSortOptionToAPICall();
                getSupportActionBar().setTitle("Favorite Movies");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setSortOptionToAPICall() {
        if (getSupportLoaderManager() == null) {
            getSupportLoaderManager().initLoader(ID_LOADER_FAVORITES, null, this);
        } else {
            getSupportLoaderManager().restartLoader(ID_LOADER_FAVORITES, null, this);
        }
    }

    @Override
    public void getMovieClicked(MovieDetails movieDetails) {
        Intent intent = new Intent(this, MovieDetailsActivity.class);
        intent.putExtra(MovieDetailsActivity.MOVIE_DETAILS, movieDetails);
        startActivity(intent);
    }

    private void loadMovies(String sorting) {
        Call call = MovieDbApiClient.getService().getPopular(sorting, BuildConfig.API_KEY);

        MovieDbApiClient.enqueue(call, new MovieDbApiClient.RequestListener<Response<MovieDetails>>() {
            @Override
            public void onRequestFailure(Throwable throwable) {
                Log.d("DAVISLOG", "FAIL! = " + throwable.getLocalizedMessage());
                throwable.printStackTrace();
                //TODO: Add exception handling
            }

            @Override
            public void onRequestSuccess(Response<MovieDetails> result) {
                List<MovieDetails> movies = result.getResults();
                if (movies != null && !movies.isEmpty()) {
                    mMovieListAdapter.replaceData(movies);
                    showMoviewList();
                } else {
                    showErrorMsg();
                }
            }
        });
    }
}
