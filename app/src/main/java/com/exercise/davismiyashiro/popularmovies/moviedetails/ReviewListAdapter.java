package com.exercise.davismiyashiro.popularmovies.moviedetails;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.exercise.davismiyashiro.popularmovies.R;
import com.exercise.davismiyashiro.popularmovies.data.Review;

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

    interface OnReviewClickListener {
        void onReviewClick(Review review);
    }

    @Override
    public ReviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_list_item, parent, false);
        return new ReviewHolder(root);
    }

    @Override
    public void onBindViewHolder(ReviewHolder holder, int position) {
        holder.bindView(reviews.get(position));
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
        private TextView reviewName;
        private TextView reviewContent;
        private Review review;

        public ReviewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onReviewClick(review);
                }
            });
            reviewName = (TextView) itemView.findViewById(R.id.review_name);
            reviewContent = (TextView) itemView.findViewById(R.id.review_content);
        }

        void bindView(Review review) {
            this.review = review;
            reviewName.setText(review.getAuthor());
            reviewContent.setText(review.getContent());
        }
    }
}
