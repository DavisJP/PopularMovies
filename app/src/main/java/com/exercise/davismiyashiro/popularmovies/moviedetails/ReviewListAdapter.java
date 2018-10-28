package com.exercise.davismiyashiro.popularmovies.moviedetails;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.exercise.davismiyashiro.popularmovies.R;
import com.exercise.davismiyashiro.popularmovies.data.Review;
import com.exercise.davismiyashiro.popularmovies.databinding.ReviewListItemBinding;

import java.util.List;

/**
 * Created by Davis Miyashiro on 01/03/2017.
 */

public class ReviewListAdapter extends RecyclerView.Adapter<ReviewListAdapter.ReviewHolder> {

    private List<Review> reviews;
    private OnReviewClickListener listener;

    public ReviewListAdapter(List<Review> reviews, OnReviewClickListener listener) {
        this.reviews = reviews;
        this.listener = listener;
    }

    public interface OnReviewClickListener {
        void onReviewClick(Review review);
    }

    @Override
    public ReviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ReviewListItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.review_list_item, parent, false);
        return new ReviewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ReviewHolder holder, int position) {
        holder.binding.setReview(reviews.get(position));
        holder.binding.setReviewCallback(listener);
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public void replaceData(List<Review> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    public class ReviewHolder extends RecyclerView.ViewHolder {

        ReviewListItemBinding binding;

        public ReviewHolder(ReviewListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
