package com.exercise.davismiyashiro.popularmovies.moviedetails

import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import com.exercise.davismiyashiro.popularmovies.R
import com.exercise.davismiyashiro.popularmovies.data.Review
import com.exercise.davismiyashiro.popularmovies.databinding.ReviewListItemBinding

/**
 * Created by Davis Miyashiro on 01/03/2017.
 */

class ReviewListAdapter(private var reviews: List<Review>,
                        private val listener: OnReviewClickListener) :
        RecyclerView.Adapter<ReviewListAdapter.ReviewHolder>() {

    interface OnReviewClickListener {
        fun onReviewClick(review: Review)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewHolder {
        val binding = DataBindingUtil.inflate<ReviewListItemBinding>(LayoutInflater.from(parent.context), R.layout.review_list_item, parent, false)
        return ReviewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewHolder, position: Int) {
        holder.binding.review = reviews[position]
        holder.binding.reviewCallback = listener
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    fun replaceData(reviews: List<Review>?) {
        if (reviews != null) {
            this.reviews = reviews
            notifyDataSetChanged()
        }
    }

    inner class ReviewHolder(internal var binding: ReviewListItemBinding) : RecyclerView.ViewHolder(binding.root)
}
