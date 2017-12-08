package com.exercise.davismiyashiro.popularmovies.movies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.exercise.davismiyashiro.popularmovies.R;
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Davis Miyashiro on 27/01/2017.
 */
public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.MovieListHolder> {

    private List<MovieDetails> mMovieList;
    private Context mContext;
    private OnMovieClickListener mClickListener;

    public final String IMG_BASE_URL = "https://image.tmdb.org/t/p/w500";

    public MovieListAdapter(LinkedList<MovieDetails> movieList, Context context, OnMovieClickListener listener) {
        mContext = context;
        mMovieList = movieList;
        mClickListener = listener;
    }

    interface OnMovieClickListener {
        void getMovieClicked(MovieDetails movieDetails);
    }

    @Override
    public MovieListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_list_item, parent, false);
        return new MovieListHolder(rootView);
    }

    @Override
    public void onBindViewHolder(MovieListHolder holder, int position) {
        holder.bindView(mMovieList.get(position));
    }

    @Override
    public int getItemCount() {
        return mMovieList.size();
    }

    public void replaceData(List<MovieDetails> movies) {
        mMovieList = movies;
        notifyDataSetChanged();
    }

    public class MovieListHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_poster)
        ImageView mImageView;

        private MovieDetails mMovieDetails;

        public MovieListHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mClickListener.getMovieClicked(mMovieDetails);
                }
            });
        }

        public void bindView(MovieDetails movieDetails) {
            mMovieDetails = movieDetails;
            Picasso.with(mContext)
                    .load(IMG_BASE_URL + mMovieDetails.getPosterPath())
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .into(mImageView);
        }
    }
}
