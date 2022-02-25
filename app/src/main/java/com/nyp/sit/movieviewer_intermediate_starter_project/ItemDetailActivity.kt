package com.nyp.sit.movieviewer_intermediate_starter_project

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.nyp.sit.movieviewer_intermediate_starter_project.databinding.ActivityItemDetailBinding
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_item_detail.*
import kotlinx.android.synthetic.main.activity_view_list_of_movies.*
import kotlinx.coroutines.*
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ItemDetailActivity : AppCompatActivity() {

    private fun displayToast(message : String){
        Toast.makeText(this,message, Toast.LENGTH_LONG).show()
    }

    private lateinit var binding: ActivityItemDetailBinding
    var activityCoroutineScope: CoroutineScope? = null
    var dynamoDBMapper: DynamoDBMapper? = null
    var currentMovieDO: MoviesDO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        activityCoroutineScope = CoroutineScope(Job() + Dispatchers.IO)

        activityCoroutineScope?.launch() {
            try{
                val credentials: AWSCredentials = AWSMobileClient.getInstance().awsCredentials
                val dynamoDBClient = AmazonDynamoDBClient(credentials)
                dynamoDBMapper = DynamoDBMapper.builder()
                    .dynamoDBClient(dynamoDBClient)
                    .awsConfiguration(
                        AWSMobileClient.getInstance().configuration
                    ).build()
            } catch (ex: Exception){
                Log.d("ItemDetail","Exception ${ex.message}")
            }
        }

        movie_overview.text = intent.getStringExtra("overview")
        movie_release_date.text = intent.getStringExtra("release_date")
        movie_popularity.text = intent.getDoubleExtra("popularity",0.0).toString()
        movie_vote_count.text = intent.getIntExtra("vote_count",0).toString()
        movie_vote_avg.text = intent.getDoubleExtra("vote_average",0.0).toString()
        movie_langauge.text = intent.getStringExtra("original_language")
        movie_is_adult.text = intent.getBooleanExtra("adult",false).toString()
        movie_hasvideo.text = intent.getBooleanExtra("video",false).toString()

        val poster_path = intent.getStringExtra("poster_path")

        val ImageJob = CoroutineScope(Job() + Dispatchers.IO).async {
            val imageRequestUrl = NetworkUtils.buildImageUrl(poster_path).toString()
            try {
                imageRequestUrl
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        GlobalScope.launch(Dispatchers.Main) {
            val ImageData = ImageJob.await()
            withContext(Dispatchers.Main){
                Picasso.get().load(ImageData).into(posterIV)
            }
        }
    }

    fun addFav() {

        val newFav = MoviesDO.MovieItem()
        newFav.poster_path = intent.getStringExtra("poster_path")
        newFav.adult = intent.getBooleanExtra("adult", false)
        newFav.overview = intent.getStringExtra("overview")
        newFav.release_date = intent.getStringExtra("release_date")
        newFav.genre_ids = intent.getStringExtra("genre_ids")
        newFav.itemId = intent.getIntExtra("id",0)
        newFav.original_title = intent.getStringExtra("original_title")
        newFav.original_language = intent.getStringExtra("original_language")
        newFav.title = intent.getStringExtra("title")
        newFav.backdrop_path = intent.getStringExtra("backdrop_path")
        newFav.popularity = intent.getDoubleExtra("popularity",0.0)
        newFav.vote_count = intent.getIntExtra("vote_count",0)
        newFav.video = intent.getBooleanExtra("video",false)
        newFav.vote_avg = intent.getDoubleExtra("vote_average",0.0)

        var duplicate = false

        for (movie in currentMovieDO?.favMovie!!) {
            if (movie.itemId == newFav.itemId) {
                duplicate = true
            }
        }

        if (!duplicate) {
            currentMovieDO?.favMovie?.add(newFav)
            activityCoroutineScope?.launch() {
                dynamoDBMapper?.save(currentMovieDO)
            }
            runOnUiThread {
                displayToast("Movie added to Fav")
            }
        }
        else {
            runOnUiThread {
                displayToast("Movie already in Fav")
            }
        }
    }

    fun runAddFav() {

        activityCoroutineScope?.launch() {
            val eav = HashMap<String, AttributeValue>()
            eav[":val1"] = AttributeValue().withS(AWSMobileClient.getInstance().username)
            val queryExpression = DynamoDBScanExpression().withFilterExpression("movieUser = :val1")
                .withExpressionAttributeValues(eav)
            val itemList = dynamoDBMapper?.scan(MoviesDO::class.java,queryExpression)

            if (itemList?.size != 0 && itemList != null){
                for (i in itemList.iterator()){
                    currentMovieDO = i
                }
                addFav()
            }
            else{
                currentMovieDO = MoviesDO()
                currentMovieDO?.apply {
                    user = AWSMobileClient.getInstance().username
                    favMovie = mutableListOf<MoviesDO.MovieItem>()
                }
                addFav()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?):Boolean {
        menu?.add(0,0,0,"Add as Favourite")?.setShowAsAction(0)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == 0) {
            runAddFav()
        }
        return super.onOptionsItemSelected(item)
    }
}
