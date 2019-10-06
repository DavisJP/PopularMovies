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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.exercise.davismiyashiro.popularmovies.R
import com.exercise.davismiyashiro.popularmovies.databinding.MovieListItemBinding
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsObservable

/**
 * Created by Davis Miyashiro on 27/01/2017.
 */
class MovieListAdapter(private var movieList: List<MovieDetailsObservable>,
                       private val mClickListener: OnMovieClickListener) :
        RecyclerView.Adapter<MovieListAdapter.MovieListHolder>() {

    interface OnMovieClickListener {
        fun getMovieClicked(movieDetails: MovieDetailsObservable)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieListHolder {
        val binding = DataBindingUtil.inflate<MovieListItemBinding>(LayoutInflater.from(parent.context), R.layout.movie_list_item, parent, false)
        binding.callback = mClickListener
        return MovieListHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieListHolder, position: Int) {
        holder.binding.movieDetails = movieList[position]
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return movieList.size
    }

    fun replaceData(movies: List<MovieDetailsObservable>?) {
        if (movies != null) {
            movieList = movies
            notifyDataSetChanged()
        }
    }

    inner class MovieListHolder(internal val binding: MovieListItemBinding) : RecyclerView.ViewHolder(binding.root)
}
