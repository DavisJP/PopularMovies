package com.exercise.davismiyashiro.popularmovies.moviedetails;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.exercise.davismiyashiro.popularmovies.R;
import com.exercise.davismiyashiro.popularmovies.data.Trailer;
import com.exercise.davismiyashiro.popularmovies.databinding.TrailerListItemBinding;

import java.util.List;

/**
 * Created by Davis Miyashiro on 26/02/2017.
 */

public class TrailerListAdapter extends RecyclerView.Adapter<TrailerListAdapter.TrailerHolder> {

    private List<Trailer> trailers;
    private OnTrailerClickListener listener;

    public TrailerListAdapter(List<Trailer> trailers, OnTrailerClickListener listener) {
        this.trailers = trailers;
        this.listener = listener;
    }

    public interface OnTrailerClickListener {
        void onTrailerClick(Trailer trailer);
    }

    @Override
    public TrailerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TrailerListItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.trailer_list_item, parent, false);
        return new TrailerHolder(binding);
    }

    @Override
    public void onBindViewHolder(TrailerHolder holder, int position) {
        holder.binding.setTrailer(trailers.get(position));
        holder.binding.setTrailerCallback(listener);
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return trailers.size();
    }

    public void replaceData(List<Trailer> trailers) {
        this.trailers = trailers;
        notifyDataSetChanged();
    }

    public class TrailerHolder extends RecyclerView.ViewHolder {

        private TrailerListItemBinding binding;

        public TrailerHolder(TrailerListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
