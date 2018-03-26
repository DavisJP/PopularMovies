package com.exercise.davismiyashiro.popularmovies.moviedetails;

import android.arch.lifecycle.ViewModelProviders;
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

import com.exercise.davismiyashiro.popularmovies.R;
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.data.Review;
import com.exercise.davismiyashiro.popularmovies.data.Trailer;
import com.exercise.davismiyashiro.popularmovies.databinding.ActivityMovieDetailsBinding;

import java.util.ArrayList;

public class MovieDetailsActivity extends AppCompatActivity implements
        TrailerListAdapter.OnTrailerClickListener,
        ReviewListAdapter.OnReviewClickListener {

    public static String MOVIE_DETAILS = "THEMOVIEDBDETAILS";
    private MovieDetails mMovieDetails;

    private ActivityMovieDetailsBinding binding;

    private TrailerListAdapter mTrailersAdapter;
    private ReviewListAdapter mReviewAdapter;

    private MovieDetailsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_movie_details);
        setSupportActionBar(binding.toolbar);

        if (getIntent().hasExtra(MOVIE_DETAILS)) {
            mMovieDetails = getIntent().getParcelableExtra(MOVIE_DETAILS);
        }

        viewModel = ViewModelProviders.of(this).get(MovieDetailsViewModel.class);

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

            viewModel.loadTrailers(mMovieDetails.getId());
            viewModel.loadReviews(mMovieDetails.getId());
            viewModel.refreshFavoriteMoviesList();

            Bundle movieId = new Bundle();
            movieId.putInt("MOVIEID", mMovieDetails.getId());
        }

        viewModel.getReviewsObservable().observe(this, reviews -> {
            mReviewAdapter.replaceData(reviews);
        });

        viewModel.getTrailersObservable().observe(this, trailers -> {
            mTrailersAdapter.replaceData(trailers);
        });

        viewModel.getMoviesObservable().observe(this, movies -> {
            if (movies!= null && !movies.isEmpty()){
                toggleFavoriteStar(movies.contains(mMovieDetails));
            }
        });
    }

    public void toggleFavoriteMovieBtn(View view) {
        if (!binding.included.favouriteStar.isChecked()) {
            viewModel.insertMovie(mMovieDetails);
        } else {
            viewModel.deleteMovie(mMovieDetails);
        }
    }

    public void toggleFavoriteStar(boolean value) {
        binding.included.favouriteStar.setChecked(value);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void showDbResultMessage (@StringRes int msg) {
        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        viewModel.cancelAsyncTasks();
    }
}
