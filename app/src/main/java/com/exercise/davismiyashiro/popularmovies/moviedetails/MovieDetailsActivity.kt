package com.exercise.davismiyashiro.popularmovies.moviedetails

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.exercise.davismiyashiro.popularmovies.App
import com.exercise.davismiyashiro.popularmovies.R
import com.exercise.davismiyashiro.popularmovies.data.Review
import com.exercise.davismiyashiro.popularmovies.data.Trailer
import com.exercise.davismiyashiro.popularmovies.databinding.ActivityMovieDetailsBinding
import timber.log.Timber
import java.util.*

class MovieDetailsActivity : AppCompatActivity(), TrailerListAdapter.OnTrailerClickListener, ReviewListAdapter.OnReviewClickListener {
    private var mMovieDetails: MovieDetailsObservable? = null

    private lateinit var binding: ActivityMovieDetailsBinding
    private lateinit var mTrailersAdapter: TrailerListAdapter
    private lateinit var mReviewAdapter: ReviewListAdapter

    private lateinit var viewModel: MovieDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = (application as App).repository

        val factory = MovieDetailsViewModel.Factory(application, repository!!)
        viewModel = ViewModelProviders.of(this, factory).get(MovieDetailsViewModel::class.java)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_movie_details)
        binding.lifecycleOwner = this
        //        binding.included.setLifecycleOwner(this);
        binding.included?.viewmodel = viewModel

        setSupportActionBar(binding.toolbar)

        if (intent.hasExtra(MOVIE_DETAILS)) {
            mMovieDetails = intent.getParcelableExtra(MOVIE_DETAILS)
        }

        mTrailersAdapter = TrailerListAdapter(ArrayList(), this)
        val layout = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.included?.rvTrailersList?.layoutManager = layout
        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.included?.rvTrailersList?.addItemDecoration(itemDecoration)
        binding.included?.rvTrailersList?.adapter = mTrailersAdapter

        mReviewAdapter = ReviewListAdapter(ArrayList(), this)
        val layoutRev = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.included?.rvReviewsList?.layoutManager = layoutRev
        binding.included?.rvReviewsList?.addItemDecoration(itemDecoration)
        binding.included?.rvReviewsList?.adapter = mReviewAdapter

        mMovieDetails?.let {
            viewModel.setMovieDetailsLivedatas(it)
        }

        viewModel.reviews.observe(this, Observer { reviews ->
            mReviewAdapter.replaceData(reviews)
        })

        viewModel.trailers.observe(this, Observer { trailers ->
            if (trailers != null && trailers.isNotEmpty()) {
                mTrailersAdapter.replaceData(trailers)
            }
        })

        mMovieDetails?.let {
            viewModel.getMovieObservable(it.id).observe(this, Observer { value ->
                if (value != null) {
                    Timber.e("Is true!")
                } else {
                    Timber.e("Is False!")
                }
            })
        }
    }

    fun showDbResultMessage(@StringRes msg: Int) {
        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
    }

    private fun openTrailer(param: String) {
        val webpage = Uri.parse("https://m.youtube.com/watch?v=$param")
        val openPage = Intent(Intent.ACTION_VIEW, webpage)
        if (openPage.resolveActivity(packageManager) != null) {
            startActivity(openPage)
        }
    }

    override fun onTrailerClick(trailer: Trailer) {
        openTrailer(trailer.key)
    }

    override fun onReviewClick(review: Review) {

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelAsyncTasks()
    }

    companion object {

        var MOVIE_DETAILS = "THEMOVIEDBDETAILS"
    }
}
