package com.exercise.davismiyashiro.popularmovies.movies;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by Davis Miyashiro.
 */

public class ImageBindingAdapter {

    public static final String IMG_BASE_URL = "https://image.tmdb.org/t/p/w500";

    @BindingAdapter({"imgUrl"})
    public static void loadImage(ImageView view, String url) {
        if (!url.equals("")) {
            Picasso.with(view.getContext())
                    .load(IMG_BASE_URL + url)
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .into(view);
        }
    }
}
