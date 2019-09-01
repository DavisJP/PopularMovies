package com.exercise.davismiyashiro.popularmovies.moviedetails;

import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Toast;

import com.exercise.davismiyashiro.popularmovies.App;
import com.exercise.davismiyashiro.popularmovies.R;
import com.exercise.davismiyashiro.popularmovies.data.Repository;
import com.exercise.davismiyashiro.popularmovies.data.Review;
import com.exercise.davismiyashiro.popularmovies.data.Trailer;
import com.exercise.davismiyashiro.popularmovies.databinding.ActivityMovieDetailsBinding;

import java.util.ArrayList;

import timber.log.Timber;

public class MovieDetailsActivity extends AppCompatActivity implements
        TrailerListAdapter.OnTrailerClickListener,
        ReviewListAdapter.OnReviewClickListener {

    public static String MOVIE_DETAILS = "THEMOVIEDBDETAILS";
    private MovieDetailsObservable mMovieDetails;

    private ActivityMovieDetailsBinding binding;

    private TrailerListAdapter mTrailersAdapter;
    private ReviewListAdapter mReviewAdapter;

    private MovieDetailsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Repository repository = ((App) getApplication()).getRepository();

        MovieDetailsViewModel.Factory factory = new MovieDetailsViewModel.Factory(getApplication(), repository);
        viewModel = ViewModelProviders.of(this, factory).get(MovieDetailsViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_movie_details);
        binding.setLifecycleOwner(this);
//        binding.included.setLifecycleOwner(this);
        binding.included.setViewmodel(viewModel);

        setSupportActionBar(binding.toolbar);

        if (getIntent().hasExtra(MOVIE_DETAILS)) {
            mMovieDetails = getIntent().getParcelableExtra(MOVIE_DETAILS);
        }

        mTrailersAdapter = new TrailerListAdapter(new ArrayList<>(), this);
        LinearLayoutManager layout = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        binding.included.rvTrailersList.setLayoutManager(layout);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        binding.included.rvTrailersList.addItemDecoration(itemDecoration);
        binding.included.rvTrailersList.setAdapter(mTrailersAdapter);

        mReviewAdapter = new ReviewListAdapter(new ArrayList<>(), this);
        LinearLayoutManager layoutRev = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        binding.included.rvReviewsList.setLayoutManager(layoutRev);
        binding.included.rvReviewsList.addItemDecoration(itemDecoration);
        binding.included.rvReviewsList.setAdapter(mReviewAdapter);

        viewModel.setMovieDetailsLivedatas(mMovieDetails);

        viewModel.getReviews().observe(this, reviews -> {
            mReviewAdapter.replaceData(reviews);
        });

        viewModel.getTrailers().observe(this, trailers -> {
            if (trailers != null && !trailers.isEmpty()) {
                mTrailersAdapter.replaceData(trailers);
            }
        });

        viewModel.getMovieObservable(mMovieDetails.getId()).observe(this, value -> {
            if (value != null) {
                Timber.e("Is true!");
            } else {
                Timber.e("Is False!");
            }
        });
    }

    public void showDbResultMessage(@StringRes int msg) {
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
