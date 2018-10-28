package com.exercise.davismiyashiro.popularmovies.movies;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.exercise.davismiyashiro.popularmovies.R;
import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;
import com.exercise.davismiyashiro.popularmovies.databinding.MovieListItemBinding;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Davis Miyashiro on 27/01/2017.
 */
public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.MovieListHolder> {

    private List<MovieDetails> mMovieList;
    private OnMovieClickListener mClickListener;

    public MovieListAdapter(LinkedList<MovieDetails> movieList,OnMovieClickListener listener) {
        mMovieList = movieList;
        mClickListener = listener;
    }

    public interface OnMovieClickListener {
        void getMovieClicked(MovieDetails movieDetails);
    }

    @Override
    public MovieListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MovieListItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.movie_list_item, parent, false);
        binding.setCallback(mClickListener);
        return new MovieListHolder(binding);
    }

    @Override
    public void onBindViewHolder(MovieListHolder holder, int position) {
        holder.binding.setMovieDetails(mMovieList.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mMovieList.size();
    }

    public void replaceData(List<MovieDetails> movies) {
        if (movies != null) {
            mMovieList = movies;
            notifyDataSetChanged();
        }
    }

    public class MovieListHolder extends RecyclerView.ViewHolder {
        final MovieListItemBinding binding;

        public MovieListHolder(MovieListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
