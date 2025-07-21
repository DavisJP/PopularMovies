/*
 * MIT License
 *
 * Copyright (c) 2019 Davis Miyashiro
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.exercise.davismiyashiro.popularmovies.moviedetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.exercise.davismiyashiro.popularmovies.data.Trailer
import com.exercise.davismiyashiro.popularmovies.databinding.TrailerListItemBinding

/**
 * Created by Davis Miyashiro on 26/02/2017.
 */

class TrailerListAdapter(
    private var trailers: List<Trailer>,
    private val listener: OnTrailerClickListener
) : RecyclerView.Adapter<TrailerListAdapter.TrailerHolder>() {

    interface OnTrailerClickListener {
        fun onTrailerClick(trailer: Trailer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrailerHolder {
        val binding =
            TrailerListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrailerHolder(binding)
    }

    override fun onBindViewHolder(holder: TrailerHolder, position: Int) {
        holder.bind(trailers[position])
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

    inner class TrailerHolder(internal val binding: TrailerListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(trailer: Trailer) {
            binding.trailerName.text = trailer.name
            binding.root.setOnClickListener { listener.onTrailerClick(trailer) }
        }
    }
}
