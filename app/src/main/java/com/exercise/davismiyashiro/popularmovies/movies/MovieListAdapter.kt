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
