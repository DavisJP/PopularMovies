package com.exercise.davismiyashiro.popularmovies.moviedetails;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.exercise.davismiyashiro.popularmovies.R;
import com.exercise.davismiyashiro.popularmovies.data.Trailer;

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

    interface OnTrailerClickListener {
        void onTrailerClick(Trailer trailer);
    }

    @Override
    public TrailerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.trailer_list_item, parent, false);
        return new TrailerHolder(root);
    }

    @Override
    public void onBindViewHolder(TrailerHolder holder, int position) {
        holder.bindView(trailers.get(position));
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
        private TextView trailerName;
        private Trailer trailer;

        public TrailerHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onTrailerClick(trailer);
                }
            });
            trailerName = (TextView) itemView.findViewById(R.id.trailer_name);
        }

        public void bindView(Trailer trailer) {
            this.trailer = trailer;
            this.trailerName.setText(trailer.getName());
        }
    }
}
