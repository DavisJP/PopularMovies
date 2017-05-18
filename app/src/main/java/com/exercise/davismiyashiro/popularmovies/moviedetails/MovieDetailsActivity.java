package com.exercise.davismiyashiro.popularmovies.moviedetails;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.exercise.davismiyashiro.popularmovies.BuildConfig;
import com.exercise.davismiyashiro.popularmovies.R;
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.data.Response;
import com.exercise.davismiyashiro.popularmovies.data.Review;
import com.exercise.davismiyashiro.popularmovies.data.Trailer;
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDbContract;
import com.exercise.davismiyashiro.popularmovies.data.remote.MovieDbApiClient;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

import static com.exercise.davismiyashiro.popularmovies.data.local.MoviesDbContract.MoviesEntry;

public class MovieDetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        TrailerListAdapter.OnTrailerClickListener,
        ReviewListAdapter.OnReviewClickListener {

    public static final int ID_LOADER_FAVORITES = 91;
    public static String MOVIE_DETAILS = "THEMOVIEDBDETAILS";
    public final String IMG_BASE_URL = "https://image.tmdb.org/t/p/w500";
    private MovieDetails mMovieDetails;
    private TextView mMovieTitle;
    private TextView mMovieReleaseDate;
    private TextView mMovieVoteAverage;
    private TextView mMovieSinopsis;
    private ImageView mMoviePoster;
    private Button mFavoriteBtn;
    private RecyclerView mTrailersList;
    private RecyclerView mRecyclerReviews;
    private CheckBox mFavoritesStar;

    private TrailerListAdapter mTrailersAdapter;
    private ReviewListAdapter mReviewAdapter;
    private List<MovieDetails> mFavoriteMovies;

    private ContentResolver mContentResolver;
    private MovieContentObserver mMovieContentObserver = MovieContentObserver.getTestContentObserver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getIntent().hasExtra(MOVIE_DETAILS)) {
            mMovieDetails = getIntent().getParcelableExtra(MOVIE_DETAILS);
        }

        mMovieTitle = (TextView) findViewById(R.id.movie_title);
        mMovieReleaseDate = (TextView) findViewById(R.id.movie_release_date);
        mMovieVoteAverage = (TextView) findViewById(R.id.movie_vote_average);
        mMovieSinopsis = (TextView) findViewById(R.id.movie_sinopsis);
        mMoviePoster = (ImageView) findViewById(R.id.movie_poster);
        mFavoriteBtn = (Button) findViewById(R.id.mark_favorite);
        mTrailersList = (RecyclerView) findViewById(R.id.rv_trailers_list);
        mRecyclerReviews = (RecyclerView) findViewById(R.id.rv_reviews_list);
        mFavoritesStar = (CheckBox) findViewById(R.id.favourite_star);

        mTrailersAdapter = new TrailerListAdapter(new ArrayList<Trailer>(), this);
        LinearLayoutManager layout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mTrailersList.setLayoutManager(layout);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mTrailersList.addItemDecoration(itemDecoration);
        mTrailersList.setAdapter(mTrailersAdapter);

        mReviewAdapter = new ReviewListAdapter(new ArrayList<Review>(), this);
        LinearLayoutManager layoutRev = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerReviews.setLayoutManager(layoutRev);
        mRecyclerReviews.addItemDecoration(itemDecoration);
        mRecyclerReviews.setAdapter(mReviewAdapter);

        if (mMovieDetails != null) {
            mMovieTitle.setText(mMovieDetails.getTitle());
            mMovieReleaseDate.setText(mMovieDetails.getReleaseDate());

            mMovieVoteAverage.setText((mMovieDetails.getVoteAverage() != null) ? mMovieDetails.getVoteAverage().toString() : "0");
            mMovieSinopsis.setText(mMovieDetails.getOverview());
            Picasso.with(this).load(IMG_BASE_URL + mMovieDetails.getPosterPath()).into(mMoviePoster);

            mFavoriteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!mFavoriteMovies.contains(mMovieDetails)) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(MoviesEntry.COLUMN_MOVIE_ID, mMovieDetails.getId());
                        contentValues.put(MoviesEntry.COLUMN_MOVIE_TITLE, mMovieDetails.getTitle());
                        contentValues.put(MoviesEntry.COLUMN_MOVIE_POSTER, mMovieDetails.getPosterPath());
                        contentValues.put(MoviesEntry.COLUMN_MOVIE_SYNOPSIS, mMovieDetails.getOverview());
                        contentValues.put(MoviesEntry.COLUMN_RELEASE_DATE, mMovieDetails.getReleaseDate());
                        contentValues.put(MoviesEntry.COLUMN_USER_RATINGS, mMovieDetails.getVoteAverage());

                        //TODO: move to a thread, suggestion: IntentService and BroadcastReceiver
                        //https://code.tutsplus.com/tutorials/android-fundamentals-intentservice-basics--mobile-6183
                        //Uri uri = getContentResolver().insert(MoviesEntry.CONTENT_URI, contentValues);
                        Uri uri = mContentResolver.insert(MoviesEntry.CONTENT_URI, contentValues);
                        Log.d(MovieDetailsActivity.class.getSimpleName(), "ContentProvider url: " + uri.toString());
                        Toast.makeText(getBaseContext(), "Movie added to Favorites", Toast.LENGTH_SHORT).show();

                        mFavoritesStar.setChecked(true);
                    } else {
                        //delete
                        Uri uri = MoviesEntry.CONTENT_URI;
                        uri = uri.buildUpon().appendPath(mMovieDetails.getId().toString()).build();

                        //getContentResolver().delete(uri, null, null);
                        mContentResolver.delete(uri, null, null);
                        Toast.makeText(getBaseContext(), "Movie deleted from Favorites", Toast.LENGTH_SHORT).show();

                        mFavoritesStar.setChecked(false);
                    }
                    refreshFavoriteMoviesList();
                }
            });
        }

        mFavoriteMovies = new ArrayList<>();

        Bundle movieId = new Bundle();
        movieId.putInt("MOVIEID", mMovieDetails.getId());
        loadTrailers();
        loadReviews();
        getSupportLoaderManager().initLoader(ID_LOADER_FAVORITES, movieId, this);
    }

    private void loadTrailers() {
        Call call = MovieDbApiClient.getService().getTrailers(String.valueOf(mMovieDetails.getId()), BuildConfig.API_KEY);

        MovieDbApiClient.enqueue(call, new MovieDbApiClient.RequestListener<Response<Trailer>>() {
            @Override
            public void onRequestFailure(Throwable throwable) {
                Log.d("DAVISLOG", "FAIL! = " + throwable.getLocalizedMessage());
                throwable.printStackTrace();
                //TODO: Add exception handling
            }

            @Override
            public void onRequestSuccess(Response<Trailer> result) {
                List<Trailer> trailers = result.getResults();
                if (trailers != null && !trailers.isEmpty()) {
                    mTrailersAdapter.replaceData(trailers);
                }
            }
        });
    }

    private void loadReviews() {
        Call call = MovieDbApiClient.getService().getReviews(String.valueOf(mMovieDetails.getId()), BuildConfig.API_KEY);

        MovieDbApiClient.enqueue(call, new MovieDbApiClient.RequestListener<Response<Review>>() {
            @Override
            public void onRequestFailure(Throwable throwable) {
                Log.d("DAVISLOG", "FAIL! = " + throwable.getLocalizedMessage());
                throwable.printStackTrace();
                //TODO: Add exception handling
            }

            @Override
            public void onRequestSuccess(Response<Review> result) {
                List<Review> reviews = result.getResults();
                if (reviews != null && !reviews.isEmpty()) {
                    mReviewAdapter.replaceData(reviews);
                }
            }
        });
    }

    private void refreshFavoriteMoviesList() {
        getSupportLoaderManager().restartLoader(ID_LOADER_FAVORITES, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mContentResolver = getContentResolver();
        mContentResolver.registerContentObserver(MoviesEntry.CONTENT_URI, true, mMovieContentObserver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mContentResolver.unregisterContentObserver(mMovieContentObserver);
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

                mFavoriteMovies = listFavoriteMovies;
                Log.d("Favorites retrieved: ", String.valueOf(mFavoriteMovies.size()));
                if (mFavoriteMovies.contains(mMovieDetails)) {
                    mFavoritesStar.setChecked(true);
                } else {
                    mFavoritesStar.setChecked(false);
                }
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
}
