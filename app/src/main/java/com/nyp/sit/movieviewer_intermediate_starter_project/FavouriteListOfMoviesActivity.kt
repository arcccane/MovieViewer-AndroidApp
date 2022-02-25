package com.nyp.sit.movieviewer_intermediate_starter_project

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.nyp.sit.movieviewer_intermediate_starter_project.databinding.ActivityFavouriteListOfMoviesBinding
import com.nyp.sit.movieviewer_intermediate_starter_project.databinding.ActivityItemDetailBinding
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_favourite_list_of_movies.*
import kotlinx.android.synthetic.main.activity_view_list_of_movies.*
import kotlinx.coroutines.*
import java.lang.Exception

class FavouriteListOfMoviesActivity : AppCompatActivity() {

    private fun displayToast(message : String){
        Toast.makeText(this,message, Toast.LENGTH_LONG).show()
    }

    private lateinit var binding: ActivityFavouriteListOfMoviesBinding
    var activityCoroutineScope: CoroutineScope? = null
    var dynamoDBMapper: DynamoDBMapper? = null
    var currentMovieDO: MoviesDO? = null

    private var moviesAdapter: MovieListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFavouriteListOfMoviesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        title = "Your Favourite Movies"

        activityCoroutineScope = CoroutineScope(Job() + Dispatchers.IO)

        favMovieList.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, p2, _ ->
                val thisMovie = currentMovieDO?.favMovie?.get(p2)
                if (thisMovie != null){
                    val detailIntent = Intent(this, ItemDetailActivity::class.java)
                    detailIntent.putExtra("poster_path",thisMovie.poster_path)
                    detailIntent.putExtra("adult",thisMovie.adult)
                    detailIntent.putExtra("overview",thisMovie.overview)
                    detailIntent.putExtra("release_date",thisMovie.release_date)
                    detailIntent.putExtra("genre_ids",thisMovie.genre_ids)
                    detailIntent.putExtra("id",thisMovie.itemId)
                    detailIntent.putExtra("original_title",thisMovie.original_title)
                    detailIntent.putExtra("original_language",thisMovie.original_language)
                    detailIntent.putExtra("title",thisMovie.title)
                    detailIntent.putExtra("backdrop_path",thisMovie.backdrop_path)
                    detailIntent.putExtra("popularity",thisMovie.popularity)
                    detailIntent.putExtra("vote_count",thisMovie.vote_count)
                    detailIntent.putExtra("video",thisMovie.video)
                    detailIntent.putExtra("vote_average",thisMovie.vote_avg)
                    startActivity(detailIntent)
                }
            }

    }

    override fun onStart() {
        super.onStart()
        runRefreshFav()
    }

    override fun onCreateOptionsMenu(menu: Menu?):Boolean {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    fun runRefreshFav() {

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
                Log.d("FavList","Exception ${ex.message}")
            }

            val eav = HashMap<String, AttributeValue>()
            eav[":val1"] = AttributeValue().withS(AWSMobileClient.getInstance().username)
            val queryExpression = DynamoDBScanExpression().withFilterExpression("movieUser = :val1")
                .withExpressionAttributeValues(eav)
            val itemList = dynamoDBMapper?.scan(MoviesDO::class.java,queryExpression)

            if (itemList?.size != 0 && itemList != null){
                for (i in itemList.iterator()){
                    currentMovieDO = i
                }

                val moviesConvertList = mutableListOf<String>()

                for (movie in currentMovieDO?.favMovie!!.iterator()){
                    moviesConvertList.add(movie.title.toString())
                }

                withContext(Dispatchers.Main){
                    moviesAdapter = MovieListAdapter(this@FavouriteListOfMoviesActivity,
                        moviesConvertList as ArrayList<String>, currentMovieDO!!.favMovie!!)
                    favMovieList.adapter = moviesAdapter
                }
            }
        }
    }

    class MovieListAdapter(context: Context, data:ArrayList<String>, movies: List<MoviesDO.MovieItem>?): BaseAdapter() {

        private val sList : ArrayList<String> = ArrayList()
        private val mInflater : LayoutInflater = LayoutInflater.from(context)

        init{
            sList.addAll(data)
        }

        override fun getItem(p0: Int): Any {
            return sList[p0]
        }

        override fun getItemId(p0: Int): Long {
            return 0
        }

        override fun getCount(): Int {
            return sList.size
        }

        val ImageJob = CoroutineScope(Job() + Dispatchers.IO).async {
            val allPosterUrl: ArrayList<String> = ArrayList()
            if (movies != null) {
                for (movie in movies) {
                    val poster = movie.poster_path.toString()
                    val imageRequestUrl = NetworkUtils.buildImageUrl(poster).toString()
                    allPosterUrl.add(imageRequestUrl)
                }
            }
            try {
                allPosterUrl
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val v : View = this.mInflater.inflate(R.layout.list_item,parent,false)
            val label: TextView = v.findViewById(R.id.movieTitle)
            label.text  = sList[position]
            val iv: ImageView = v.findViewById(R.id.movieImage)

            GlobalScope.launch(Dispatchers.Main) {
                val ImageData = ImageJob.await()
                withContext(Dispatchers.Main){
                    if (ImageData != null) {
                        Picasso.get().load(ImageData[position]).resize(300,300).into(iv)
                    }
                }
            }
            return v
        }
    }
}