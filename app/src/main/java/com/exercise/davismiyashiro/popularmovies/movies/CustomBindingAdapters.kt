package com.exercise.davismiyashiro.popularmovies.movies

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView

import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsObservable
import com.squareup.picasso.Picasso

/**
 * Created by Davis Miyashiro.
 */

object CustomBindingAdapters {

    private const val IMG_BASE_URL = "https://image.tmdb.org/t/p/w500"

    @BindingAdapter("imgUrl")
    @JvmStatic fun loadImage(view: ImageView, url: String?) {
        url?.let {
            Picasso.with(view.context)
                    .load(IMG_BASE_URL + it)
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .into(view)
        }
    }

    @BindingAdapter("app:data")
    @JvmStatic fun loadMovieDetails(recyclerView: RecyclerView, movies: List<MovieDetailsObservable>?) {
        if (recyclerView.adapter is MovieListAdapter) {
            (recyclerView.adapter as MovieListAdapter).replaceData(movies)
        }
    }
}
