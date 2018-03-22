package com.exercise.davismiyashiro.popularmovies.moviedetails;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.StringRes;
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
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDao;
import com.exercise.davismiyashiro.popularmovies.data.local.MoviesDb;
import com.exercise.davismiyashiro.popularmovies.data.remote.TheMovieDb;
import com.exercise.davismiyashiro.popularmovies.databinding.ActivityMovieDetailsBinding;

import java.util.ArrayList;
import java.util.List;

import static com.exercise.davismiyashiro.popularmovies.data.local.MovieDataService.AsyncTaskDelete;
import static com.exercise.davismiyashiro.popularmovies.data.local.MovieDataService.AsyncTaskInsert;
import static com.exercise.davismiyashiro.popularmovies.data.local.MovieDataService.AsyncTaskQueryAll;

public class MovieDetailsActivity extends AppCompatActivity implements MovieDetailsInterfaces.View,
        TrailerListAdapter.OnTrailerClickListener,
        ReviewListAdapter.OnReviewClickListener {

    public static String MOVIE_DETAILS = "THEMOVIEDBDETAILS";
    private MovieDetails mMovieDetails;

    private ActivityMovieDetailsBinding binding;

    private TrailerListAdapter mTrailersAdapter;
    private ReviewListAdapter mReviewAdapter;

    private MovieDetailsPresenter presenter;

    private MoviesDb db;
    private MoviesDao moviesDao;

    private AsyncTaskInsert asyncTaskInsert;
    private AsyncTaskDelete asyncTaskDelete;
    private AsyncTaskQueryAll asyncTaskQueryAll;

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

        mTrailersAdapter = new TrailerListAdapter(new ArrayList<>(), this);
        LinearLayoutManager layout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.included.rvTrailersList.setLayoutManager(layout);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        binding.included.rvTrailersList.addItemDecoration(itemDecoration);
        binding.included.rvTrailersList.setAdapter(mTrailersAdapter);

        mReviewAdapter = new ReviewListAdapter(new ArrayList<>(), this);
        LinearLayoutManager layoutRev = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.included.rvReviewsList.setLayoutManager(layoutRev);
        binding.included.rvReviewsList.addItemDecoration(itemDecoration);
        binding.included.rvReviewsList.setAdapter(mReviewAdapter);

        if (mMovieDetails != null) {
            binding.included.setMovieDetails(mMovieDetails);
            binding.included.movieVoteAverage.setText((mMovieDetails.getVoteAverage() != null) ? mMovieDetails.getVoteAverage().toString() : "0");

            presenter.loadTrailers(mMovieDetails.getId());
            presenter.loadReviews(mMovieDetails.getId());

            Bundle movieId = new Bundle();
            movieId.putInt("MOVIEID", mMovieDetails.getId());
        }

        db = MoviesDb.getDatabase(getApplication());
        moviesDao = db.moviesDao();

        refreshFavoriteMoviesList();
    }

    private TheMovieDb getTheMovieDbClient () {
        return ((App)getApplication()).getMovieDbApi();
    }

    public void toggleFavoriteMovieBtn(View view) {
        if (!presenter.hasFavoriteMovie(mMovieDetails)) {
//            MovieDataService.insertNewMovie(this, contentValues);
            asyncTaskInsert = new AsyncTaskInsert(moviesDao, () -> refreshFavoriteMoviesList());
            asyncTaskInsert.execute(mMovieDetails);
            toggleFavoriteStar(true);
        } else {
            //delete
//            MovieDataService.deleteMovie(this, uri);
            asyncTaskDelete = new AsyncTaskDelete(moviesDao, () -> refreshFavoriteMoviesList());
            asyncTaskDelete.execute(mMovieDetails);
            toggleFavoriteStar(false);
        }
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
        asyncTaskQueryAll = new AsyncTaskQueryAll(moviesDao, movieList -> {
            if (movieList != null) {
                presenter.setFavoriteMovies(movieList);
                toggleFavoriteStar(presenter.hasFavoriteMovie(mMovieDetails));
            }
        });
        asyncTaskQueryAll.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mContentResolver = getContentResolver();
//        mContentResolver.registerContentObserver(MoviesEntry.CONTENT_URI, true, mMovieContentObserver);

//        IntentFilter mIntentFilter = new IntentFilter();
//        mIntentFilter.addAction(MovieDataService.ACTION_INSERT);
//        mIntentFilter.addAction(MovieDataService.ACTION_DELETE);
//        registerReceiver(mReceiver, mIntentFilter);
    }

//    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals(MovieDataService.ACTION_INSERT)) {
//                showDbResultMessage(R.string.movie_added_msg);
//            } else if (intent.getAction().equals(MovieDataService.ACTION_DELETE)) {
//                showDbResultMessage(R.string.movie_deleted_msg);
//            }
//        }
//    };

    @Override
    public void showDbResultMessage (@StringRes int msg) {
        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        unregisterReceiver(mReceiver);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.dettachView();
        cancelAsyncTasks();
    }

    public void cancelAsyncTasks() {
        if (asyncTaskInsert != null) asyncTaskInsert.cancel(true);
        if (asyncTaskDelete != null) asyncTaskDelete.cancel(true);
        if (asyncTaskQueryAll != null) asyncTaskQueryAll.cancel(true);
    }
}
