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

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import android.os.Parcelable
import androidx.databinding.library.baseAdapters.BR

import kotlinx.android.parcel.Parcelize

/**
 *
 * Created by Davis Miyashiro.
 */
@Parcelize
class MovieDetailsObservable(var idParam: Int,
                             var titleParam: String,
                             var posterPathParam: String,
                             var overviewParam: String,
                             var releaseDateParam: String,
                             var voteAverageParam: Double) : BaseObservable(), Parcelable {
    @get:Bindable
    var id: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.id)
        }

    @get:Bindable
    var title: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.title)
        }

    @get:Bindable
    var posterPath: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.posterPath)
        }

    @get:Bindable
    var overview: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.overview)
        }

    @get:Bindable
    var releaseDate: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.releaseDate)
        }

    @get:Bindable
    var voteAverage: Double = 0.0
        set(value) {
            field = value
            notifyPropertyChanged(BR.voteAverage)
        }

    init {
        id = idParam
        title = titleParam
        posterPath = posterPathParam
        overview = overviewParam
        releaseDate = releaseDateParam
        voteAverage = voteAverageParam
    }
}