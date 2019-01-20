package com.exercise.davismiyashiro.popularmovies.movies;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.exercise.davismiyashiro.popularmovies.R;
import com.exercise.davismiyashiro.popularmovies.databinding.MovieListItemBinding;
import com.exercise.davismiyashiro.popularmovies.moviedetails.MovieDetailsObservable;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Davis Miyashiro on 27/01/2017.
 */
public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.MovieListHolder> {

    private List<MovieDetailsObservable> mMovieList;
    private OnMovieClickListener mClickListener;

    public MovieListAdapter(LinkedList<MovieDetailsObservable> movieList,OnMovieClickListener listener) {
        mMovieList = movieList;
        mClickListener = listener;
    }

    public interface OnMovieClickListener {
        void getMovieClicked(MovieDetailsObservable movieDetails);
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

    public void replaceData(List<MovieDetailsObservable> movies) {
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
