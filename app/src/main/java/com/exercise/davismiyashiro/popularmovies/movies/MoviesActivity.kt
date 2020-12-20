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

package com.exercise.davismiyashiro.popularmovies.movies

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.exercise.davismiyashiro.popularmovies.App
import com.exercise.davismiyashiro.popularmovies.R
import com.exercise.davismiyashiro.popularmovies.databinding.ActivityMoviesBinding
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsActivity
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsObservable
import java.util.*

const val POPULARITY_DESC_PARAM = "popular"
const val HIGHEST_RATED_PARAM = "top_rated"
const val FAVORITES_PARAM = "favorites"

class MoviesActivity : AppCompatActivity(), MovieListAdapter.OnMovieClickListener {

    private var mSortOpt: String = POPULARITY_DESC_PARAM
    private val SORT_KEY = "SORT_KEY"

    private lateinit var binding: ActivityMoviesBinding
    private lateinit var mMovieListAdapter: MovieListAdapter

    private lateinit var viewModel: MoviesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = (application as App).repository

        val factory = MoviesViewModel.Factory(application, repository)

        viewModel = ViewModelProviders.of(this, factory).get(MoviesViewModel::class.java)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_movies)
        binding.lifecycleOwner = this
        binding.viewmodel = viewModel

        if (savedInstanceState != null) {
            mSortOpt = savedInstanceState.getString(SORT_KEY) ?: POPULARITY_DESC_PARAM
        }

        val layout = GridLayoutManager(this, calculateNoOfColumns(this))
        binding.rvMovieList.layoutManager = layout
        binding.rvMovieList.setHasFixedSize(true)

        mMovieListAdapter = MovieListAdapter(LinkedList(), this)
        binding.rvMovieList.adapter = mMovieListAdapter

        viewModel.setMoviesBySortingOption(mSortOpt)

        viewModel.moviesObservable.observe(this, Observer { movies ->
            if (movies != null && movies.isNotEmpty()) {
                updateMovieData(movies)
                showMovieList()
            } else {
                showErrorMsg()
            }
        })

        setTitleBar(mSortOpt)
    }

    private fun setTitleBar(favoritesParam: String) {
        when (favoritesParam) {
            FAVORITES_PARAM -> supportActionBar?.setTitle(R.string.favorites)

            HIGHEST_RATED_PARAM -> supportActionBar?.setTitle(R.string.highest_rated_movies)

            POPULARITY_DESC_PARAM -> supportActionBar?.setTitle(R.string.popular_movies)

            else -> supportActionBar?.setTitle(R.string.popular_movies)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(SORT_KEY, mSortOpt)
        super.onSaveInstanceState(outState)
    }

    private fun showErrorMsg() {
        binding.tvErrorMessageDisplay.visibility = View.VISIBLE
        binding.rvMovieList.visibility = View.INVISIBLE
    }

    private fun showMovieList() {
        binding.tvErrorMessageDisplay.visibility = View.INVISIBLE
        binding.rvMovieList.visibility = View.VISIBLE
    }

    private fun updateMovieData(listMovies: List<MovieDetailsObservable>?) {
        mMovieListAdapter.replaceData(listMovies)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.movies_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_popular -> {
                mSortOpt = POPULARITY_DESC_PARAM
                viewModel.setMoviesBySortingOption(mSortOpt)
                setTitleBar(mSortOpt)
                return true
            }

            R.id.action_highest_rated -> {
                mSortOpt = HIGHEST_RATED_PARAM
                viewModel.setMoviesBySortingOption(mSortOpt)
                setTitleBar(mSortOpt)
                return true
            }

            R.id.action_favorites -> {
                mSortOpt = FAVORITES_PARAM
                viewModel.setMoviesBySortingOption(mSortOpt)
                setTitleBar(mSortOpt)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun getMovieClicked(movieDetails: MovieDetailsObservable) {
        val intent = Intent(this, MovieDetailsActivity::class.java)
        intent.putExtra(MovieDetailsActivity.MOVIE_DETAILS, movieDetails)
        startActivity(intent)
    }

    private fun calculateNoOfColumns(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        return (dpWidth / 180).toInt()
    }
}
