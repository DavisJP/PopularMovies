package com.exercise.davismiyashiro.popularmovies

import android.content.Context

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Created by Davis Miyashiro on 07/12/2017.
 */

object JsonFileReaderHelper {

    @Throws(Exception::class)
    fun convertStreamToString(`is`: InputStream): String {
        val reader = BufferedReader(InputStreamReader(`is`))
        val sb = StringBuilder()
        while (true) {
            val line = reader.readLine() ?: break
            sb.append(line).append("\n")
        }
        reader.close()
        return sb.toString()
    }

    @Throws(Exception::class)
    fun getStringFromFile(context: Context, filePath: String): String {
        val stream = context.resources.assets.open(filePath)

        val ret = convertStreamToString(stream)
        //Make sure you close all streams.
        stream.close()
        return ret
    }
}
