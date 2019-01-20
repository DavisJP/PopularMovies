package com.exercise.davismiyashiro.popularmovies.moviedetails

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import android.os.Parcelable

import com.exercise.davismiyashiro.popularmovies.BR
import kotlinx.android.parcel.Parcelize

/**
 *
 * Created by Davis Miyashiro.
 */
@Parcelize
class MovieDetailsObservable (var idParam: Int,
                              var titleParam: String,
                              var posterPathParam: String,
                              var overviewParam: String,
                              var releaseDateParam: String,
                              var voteAverageParam: Double): BaseObservable(), Parcelable {
    @get:Bindable
    var id: Int = 0
    set(value) {
        field = value
        notifyPropertyChanged(BR.id)
    }

    @get:Bindable var title: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.title)
        }

    @get:Bindable var posterPath: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.posterPath)
        }

    @get:Bindable var overview: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.overview)
        }

    @get:Bindable var releaseDate: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.releaseDate)
        }

    @get:Bindable var voteAverage: Double = 0.0
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