/*
 * MIT License
 *
 * Copyright (c) 2019 Davis Miyashiro
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.exercise.davismiyashiro.popularmovies.moviedetails

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
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
import com.squareup.picasso.Picasso

const val IMG_BASE_URL = "https://image.tmdb.org/t/p/w500"

class MovieDetailsActivity : AppCompatActivity(), TrailerListAdapter.OnTrailerClickListener,
    ReviewListAdapter.OnReviewClickListener {

    private var mMovieDetails: MovieDetailsObservable? = null

    private lateinit var binding: ActivityMovieDetailsBinding
    private lateinit var mTrailersAdapter: TrailerListAdapter
    private lateinit var mReviewAdapter: ReviewListAdapter

    private lateinit var viewModel: MovieDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = (application as App).repository

        val factory = MovieDetailsViewModel.Factory(application, repository)
        viewModel = ViewModelProviders.of(this, factory).get(MovieDetailsViewModel::class.java)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_movie_details)
        binding.lifecycleOwner = this

        setSupportActionBar(binding.toolbar)

        if (intent.hasExtra(MOVIE_DETAILS)) {
            mMovieDetails = intent.getParcelableExtra(MOVIE_DETAILS)
        }

        mTrailersAdapter = TrailerListAdapter(ArrayList(), this)
        val layout = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.included.rvTrailersList.layoutManager = layout
        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.included.rvTrailersList.addItemDecoration(itemDecoration)
        binding.included.rvTrailersList.adapter = mTrailersAdapter

        mReviewAdapter = ReviewListAdapter(ArrayList(), this)
        val layoutRev = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.included.rvReviewsList.layoutManager = layoutRev
        binding.included.rvReviewsList.addItemDecoration(itemDecoration)
        binding.included.rvReviewsList.adapter = mReviewAdapter
        binding.included.favouriteStar.setOnClickListener {
            viewModel.setFavorite()
        }

        mMovieDetails?.let {
            viewModel.setMovieDetailsLivedatas(it)
            viewModel.movieLiveData.observe(this, Observer { value ->
                Picasso.get()
                    .load(IMG_BASE_URL + it.backdropPath)
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .into(binding.included.movieBackdrop)
                Picasso.get()
                    .load(IMG_BASE_URL + it.posterPath)
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .into(binding.included.imgUrl)
                binding.included.movieTitle.text = it.title
                binding.included.movieReleaseDate.text = it.releaseDate
                binding.included.movieVoteAverage.text = it.voteAverage.toString()
                binding.included.movieSinopsis.text = it.overview
            })
        }

        viewModel.reviews.observe(this, Observer { reviews ->
            mReviewAdapter.replaceData(reviews)
        })

        viewModel.trailers.observe(this, Observer { trailers ->
            if (trailers != null && trailers.isNotEmpty()) {
                mTrailersAdapter.replaceData(trailers)
            }
        })

        viewModel.favoriteCheckBoxLivedata.observe(this, Observer { value ->
            binding.included.favouriteStar.isChecked = value
        })
    }

    fun showDbResultMessage(@StringRes msg: Int) {
        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
    }

    private fun openTrailer(param: String) {
        val videoLink = "https://m.youtube.com/watch?v=$param".toUri()
        val intent = Intent(Intent.ACTION_VIEW, videoLink)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "No app found to open YouTube link", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onTrailerClick(trailer: Trailer) {
        openTrailer(trailer.key)
    }

    override fun onReviewClick(review: Review) {

    }

    companion object {

        var MOVIE_DETAILS = "THEMOVIEDBDETAILS"
    }
}
