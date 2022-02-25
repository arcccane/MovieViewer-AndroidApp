package com.nyp.sit.movieviewer_intermediate_starter_project

import android.content.Context
import com.nyp.sit.movieviewer_intermediate_starter_project.entity.MovieItem

import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class movieDBJsonUtils() {


    companion object {


        @Throws(JSONException::class)
        fun getMovieDetailsFromJson(context: Context, movieDetailsJsonStr: String): ArrayList<MovieItem>? {

            val M_RESULTS = "results"
            val M_POSTER_PATH = "poster_path"
            val M_ADULT = "adult"
            val M_OVERVIEW = "overview"
            val M_RELEASE_DATE = "release_date"
            val M_GENRE_IDS = "genre_ids"
            val M_ID = "id"
            val M_ORIGINAL_TITLE = "original_title"
            val M_ORIGINAL_LANGUAGE = "original_language"
            val M_TITLE = "title"
            val M_BACKDROP_PATH = "backdrop_path"
            val M_POPULARITY = "popularity"
            val M_VOTE_COUNT = "vote_count"
            val M_VIDEO = "video"
            val M_VOTE_AVERAGE = "vote_average"


            val parsedMovieData = ArrayList<MovieItem>()

            val movieJson = JSONObject(movieDetailsJsonStr)

            val movieArrays = movieJson.getJSONArray(M_RESULTS)

            for (i in 0 until movieArrays.length()){

                val movieArray = movieArrays.getJSONObject(i)

                val posterPath = movieArray.getString(M_POSTER_PATH)
                val adult = movieArray.getBoolean(M_ADULT)
                val overview = movieArray.getString(M_OVERVIEW)
                val releaseDate = movieArray.getString(M_RELEASE_DATE)
                val genreIds = movieArray.getJSONArray(M_GENRE_IDS).toString()
                val id = movieArray.getInt(M_ID)
                val ogTitle = movieArray.getString(M_ORIGINAL_TITLE)
                val ogLang = movieArray.getString(M_ORIGINAL_LANGUAGE)
                val title = movieArray.getString(M_TITLE)
                val backdropPath = movieArray.getString(M_BACKDROP_PATH)
                val popularity = movieArray.getDouble(M_POPULARITY)
                val voteCount = movieArray.getInt(M_VOTE_COUNT)
                val video = movieArray.getBoolean(M_VIDEO)
                val voteAverage = movieArray.getDouble(M_VOTE_AVERAGE)

                parsedMovieData.add(MovieItem(posterPath,adult,overview,releaseDate,genreIds,id,
                    ogTitle,ogLang,title,backdropPath,popularity,voteCount,video,voteAverage))
            }

            return parsedMovieData
        }
    }
}