package com.exercise.davismiyashiro.popularmovies.moviedetails;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.StringRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.exercise.davismiyashiro.popularmovies.App;
import com.exercise.davismiyashiro.popularmovies.R;
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.data.Review;
import com.exercise.davismiyashiro.popularmovies.data.Trailer;
import com.exercise.davismiyashiro.popularmovies.data.local.MovieDataService;
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDbContract;
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb;
import com.exercise.davismiyashiro.popularmovies.databinding.ActivityMovieDetailsBinding;

import java.util.ArrayList;
import java.util.List;

import static com.exercise.davismiyashiro.popularmovies.data.local.MoviesDbContract.MoviesEntry;

public class MovieDetailsActivity extends AppCompatActivity implements MovieDetailsInterfaces.View,
        LoaderManager.LoaderCallbacks<Cursor>,
        TrailerListAdapter.OnTrailerClickListener,
        ReviewListAdapter.OnReviewClickListener {

    public static final int ID_LOADER_FAVORITES = 91;
    public static String MOVIE_DETAILS = "THEMOVIEDBDETAILS";
    private MovieDetails mMovieDetails;

    private ActivityMovieDetailsBinding binding;

    private TrailerListAdapter mTrailersAdapter;
    private ReviewListAdapter mReviewAdapter;

    private MovieDetailsPresenter presenter;

    private ContentResolver mContentResolver;
    private MovieContentObserver mMovieContentObserver = MovieContentObserver.getTestContentObserver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_movie_details);
        setSupportActionBar(binding.toolbar);

        presenter = new MovieDetailsPresenter(getTheMovieDbClient());
        presenter.attachView(this);

        if (getIntent().hasExtra(MOVIE_DETAILS)) {
            mMovieDetails = getIntent().getParcelableExtra(MOVIE_DETAILS);
        }

        mTrailersAdapter = new TrailerListAdapter(new ArrayList<Trailer>(), this);
        LinearLayoutManager layout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.included.rvTrailersList.setLayoutManager(layout);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        binding.included.rvTrailersList.addItemDecoration(itemDecoration);
        binding.included.rvTrailersList.setAdapter(mTrailersAdapter);

        mReviewAdapter = new ReviewListAdapter(new ArrayList<Review>(), this);
        LinearLayoutManager layoutRev = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.included.rvReviewsList.setLayoutManager(layoutRev);
        binding.included.rvReviewsList.addItemDecoration(itemDecoration);
        binding.included.rvReviewsList.setAdapter(mReviewAdapter);

        if (mMovieDetails != null) {
            binding.included.setMovieDetails(mMovieDetails);
            binding.included.movieVoteAverage.setText((mMovieDetails.getVoteAverage() != null) ? mMovieDetails.getVoteAverage().toString() : "0");
        }

        presenter.loadTrailers(mMovieDetails.getId());
        presenter.loadReviews(mMovieDetails.getId());

        Bundle movieId = new Bundle();
        movieId.putInt("MOVIEID", mMovieDetails.getId());
        getSupportLoaderManager().initLoader(ID_LOADER_FAVORITES, movieId, this);
    }

    private TheMovieDb getTheMovieDbClient () {
        return ((App)getApplication()).getMovieDbApi();
    }

    public void toggleFavoriteMovieBtn(View view) {
        if (!presenter.hasFavoriteMovie(mMovieDetails)) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MoviesEntry.COLUMN_MOVIE_ID, mMovieDetails.getId());
            contentValues.put(MoviesEntry.COLUMN_MOVIE_TITLE, mMovieDetails.getTitle());
            contentValues.put(MoviesEntry.COLUMN_MOVIE_POSTER, mMovieDetails.getPosterPath());
            contentValues.put(MoviesEntry.COLUMN_MOVIE_SYNOPSIS, mMovieDetails.getOverview());
            contentValues.put(MoviesEntry.COLUMN_RELEASE_DATE, mMovieDetails.getReleaseDate());
            contentValues.put(MoviesEntry.COLUMN_USER_RATINGS, mMovieDetails.getVoteAverage());

            MovieDataService.insertNewMovie(this, contentValues);

            toggleFavoriteStar(true);
        } else {
            //delete
            Uri uri = MoviesEntry.CONTENT_URI.buildUpon().appendPath(mMovieDetails.getId().toString()).build();

            MovieDataService.deleteMovie(this, uri);

            toggleFavoriteStar(false);
        }
        refreshFavoriteMoviesList();
    }

    @Override
    public void toggleFavoriteStar(boolean value) {
        binding.included.favouriteStar.setChecked(value);
    }

    @Override
    public void replaceTrailersData(List<Trailer> trailers) {
        mTrailersAdapter.replaceData(trailers);
    }

    @Override
    public void replaceReviewsData(List<Review> reviews) {
        mReviewAdapter.replaceData(reviews);
    }

    @Override
    public void refreshFavoriteMoviesList() {
        getSupportLoaderManager().restartLoader(ID_LOADER_FAVORITES, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mContentResolver = getContentResolver();
        mContentResolver.registerContentObserver(MoviesEntry.CONTENT_URI, true, mMovieContentObserver);

        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(MovieDataService.ACTION_INSERT);
        mIntentFilter.addAction(MovieDataService.ACTION_DELETE);
        registerReceiver(mReceiver, mIntentFilter);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MovieDataService.ACTION_INSERT)) {
                showDbResultMessage(R.string.movie_added_msg);
            } else if (intent.getAction().equals(MovieDataService.ACTION_DELETE)) {
                showDbResultMessage(R.string.movie_deleted_msg);
            }
        }
    };

    @Override
    public void showDbResultMessage (@StringRes int msg) {
        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mContentResolver.unregisterContentObserver(mMovieContentObserver);
        unregisterReceiver(mReceiver);
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
                return null;
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

                presenter.setFavoriteMovies(listFavoriteMovies);

                toggleFavoriteStar(presenter.hasFavoriteMovie(mMovieDetails));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void openTrailer(String param) {
        Uri webpage = Uri.parse("https://m.youtube.com/watch?v=" + param);
        Intent openPage = new Intent(Intent.ACTION_VIEW, webpage);
        if (openPage.resolveActivity(getPackageManager()) != null) {
            startActivity(openPage);
        }
    }

    @Override
    public void onTrailerClick(Trailer trailer) {
        openTrailer(trailer.getKey());
    }

    @Override
    public void onReviewClick(Review review) {

    }

    static class MovieContentObserver extends ContentObserver {

        private MovieContentObserver(HandlerThread handler) {
            super(new Handler(handler.getLooper()));
        }

        static MovieContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new MovieContentObserver(ht);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            //TODO: it calls both, only keep one!
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.dettachView();
    }
}
