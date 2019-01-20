package com.exercise.davismiyashiro.popularmovies.movies;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageView;

import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsObservable;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Davis Miyashiro.
 */

public class CustomBindingAdapters {

    public static final String IMG_BASE_URL = "https://image.tmdb.org/t/p/w500";

    @BindingAdapter({"imgUrl"})
    public static void loadImage(ImageView view, String url) {
        if (url!= null && !url.equals("")) {
            Picasso.with(view.getContext())
                    .load(IMG_BASE_URL + url)
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .into(view);
        }
    }

    @BindingAdapter({"app:data"})
    public static void loadMovieDetails(RecyclerView recyclerView, List<MovieDetailsObservable> movies) {
        if (recyclerView.getAdapter() instanceof MovieListAdapter) {
            ((MovieListAdapter)recyclerView.getAdapter()).replaceData(movies);
        }
    }
}
