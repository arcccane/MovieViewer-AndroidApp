package com.nyp.sit.movieviewer_intermediate_starter_project

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBDocument
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable
import com.nyp.sit.movieviewer_intermediate_starter_project.entity.MovieItem
import java.util.*

@DynamoDBTable(tableName = "UserData")
class MoviesDO {

    @DynamoDBHashKey(attributeName = "id")
    var id: String? = UUID.randomUUID().toString()

    @DynamoDBAttribute(attributeName = "movieUser")
    var user: String? = null

    @DynamoDBAttribute(attributeName = "favMovie")
    var favMovie: MutableList<MovieItem>? = null

    @DynamoDBDocument
    class MovieItem() {
        @DynamoDBAttribute(attributeName = "poster_path")
        var poster_path: String? = null
        @DynamoDBAttribute(attributeName = "adult")
        var adult: Boolean? = null
        @DynamoDBAttribute(attributeName = "overview")
        var overview: String? = null
        @DynamoDBAttribute(attributeName = "release_date")
        var release_date: String? = null
        @DynamoDBAttribute(attributeName = "genre_ids")
        var genre_ids: String? = null
        @DynamoDBAttribute(attributeName = "itemId")
        var itemId: Int? = null
        @DynamoDBAttribute(attributeName = "original_title")
        var original_title: String? = null
        @DynamoDBAttribute(attributeName = "original_language")
        var original_language: String? = null
        @DynamoDBAttribute(attributeName = "title")
        var title: String? = null
        @DynamoDBAttribute(attributeName = "backdrop_path")
        var backdrop_path: String? = null
        @DynamoDBAttribute(attributeName = "popularity")
        var popularity: Double = 0.0
        @DynamoDBAttribute(attributeName = "vote_count")
        var vote_count: Int = 0
        @DynamoDBAttribute(attributeName = "video")
        var video: Boolean? = null
        @DynamoDBAttribute(attributeName = "vote_avg")
        var vote_avg: Double = 0.0
    }
}