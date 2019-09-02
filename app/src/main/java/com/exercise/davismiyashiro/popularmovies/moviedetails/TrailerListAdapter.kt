package com.exercise.davismiyashiro.popularmovies.moviedetails

import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import com.exercise.davismiyashiro.popularmovies.R
import com.exercise.davismiyashiro.popularmovies.data.Trailer
import com.exercise.davismiyashiro.popularmovies.databinding.TrailerListItemBinding

/**
 * Created by Davis Miyashiro on 26/02/2017.
 */

class TrailerListAdapter(private var trailers: List<Trailer>, private val listener: OnTrailerClickListener) : RecyclerView.Adapter<TrailerListAdapter.TrailerHolder>() {

    interface OnTrailerClickListener {
        fun onTrailerClick(trailer: Trailer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrailerHolder {
        val binding = DataBindingUtil.inflate<TrailerListItemBinding>(LayoutInflater.from(parent.context), R.layout.trailer_list_item, parent, false)
        return TrailerHolder(binding)
    }

    override fun onBindViewHolder(holder: TrailerHolder, position: Int) {
        holder.binding.trailer = trailers[position]
        holder.binding.trailerCallback = listener
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return trailers.size
    }

    fun replaceData(trailers: List<Trailer>?) {
        if (trailers != null) {
            this.trailers = trailers
            notifyDataSetChanged()
        }
    }

    inner class TrailerHolder(internal val binding: TrailerListItemBinding) : RecyclerView.ViewHolder(binding.root)
}
