package com.nyp.sit.movieviewer_intermediate_starter_project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.SignOutOptions
import com.nyp.sit.movieviewer_intermediate_starter_project.entity.MovieItem
import kotlinx.android.synthetic.main.activity_view_list_of_movies.*
import kotlinx.coroutines.*
import java.lang.Exception
import com.squareup.picasso.Picasso
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat

class ViewListOfMoviesActivity : AppCompatActivity() {

    private fun displayToast(message : String){
        Toast.makeText(this,message, Toast.LENGTH_LONG).show()
    }

    val SHOW_BY_TOP_RATED = 1
    val SHOW_BY_POPULAR = 2

    private var displayType = SHOW_BY_TOP_RATED

    private var moviesAdapter: MovieListAdapter? = null
    var allMovies: List<MovieItem>? = null

    val STATE_SCROLL_POSITION = "scrollPosition"
    var savedPosition = 0

    private var mTopToolbar: Toolbar? = null

    private val moviesViewModel: MoviesViewModel by viewModels(){
        MoviesViewModelFactory((application as MyMovies).repo)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_list_of_movies)

        mTopToolbar = findViewById(R.id.app_bar)
        setSupportActionBar(mTopToolbar)

        app_bar.setNavigationOnClickListener(NavigationIconClickListener(this, grid,
            AccelerateDecelerateInterpolator(),
            ContextCompat.getDrawable(this, R.drawable.menu_icon),
            ContextCompat.getDrawable(this, R.drawable.menu_close)))

        moviesViewModel.allMovies.observe(this@ViewListOfMoviesActivity, Observer {
            val moviesConvertList = mutableListOf<String>()
            allMovies = it
            for (movie in it){
                moviesConvertList.add(movie.title.toString())
            }
            it?.let {
                moviesAdapter = MovieListAdapter(this, moviesConvertList as ArrayList<String>, it)
                movielist.adapter = moviesAdapter
                movielist.setSelection(savedPosition)
            }
        })

        movielist.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, p2, _ ->
                val thisMovie = allMovies?.get(p2)
                if (thisMovie != null){
                    val detailIntent = Intent(this@ViewListOfMoviesActivity, ItemDetailActivity::class.java)
                    detailIntent.putExtra("poster_path",thisMovie.poster_path)
                    detailIntent.putExtra("adult",thisMovie.adult)
                    detailIntent.putExtra("overview",thisMovie.overview)
                    detailIntent.putExtra("release_date",thisMovie.release_date)
                    detailIntent.putExtra("genre_ids",thisMovie.genre_ids)
                    detailIntent.putExtra("id",thisMovie.id)
                    detailIntent.putExtra("original_title",thisMovie.original_title)
                    detailIntent.putExtra("original_language",thisMovie.original_langauge)
                    detailIntent.putExtra("title",thisMovie.title)
                    detailIntent.putExtra("backdrop_path",thisMovie.backdrop_path)
                    detailIntent.putExtra("popularity",thisMovie.popularity)
                    detailIntent.putExtra("vote_count",thisMovie.vote_count)
                    detailIntent.putExtra("video",thisMovie.video)
                    detailIntent.putExtra("vote_average",thisMovie.vote_average)
                    startActivity(detailIntent)
                }
            }
    }

    override fun onStart() {
        super.onStart()
        loadMovieData(displayType)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        val currentPosition: Int = movielist.firstVisiblePosition
        savedInstanceState.putInt(STATE_SCROLL_POSITION,
            currentPosition)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedPosition = savedInstanceState
            .getInt(STATE_SCROLL_POSITION)
        movielist.setSelection(savedPosition)
    }

    fun loadMovieData(viewType: Int) {

        moviesViewModel.deleteAll()

        var showTypeStr: String? = null
        when (viewType) {
            SHOW_BY_TOP_RATED -> showTypeStr = NetworkUtils.TOP_RATED_PARAM
            SHOW_BY_POPULAR -> showTypeStr = NetworkUtils.POPULAR_PARAM
        }

        if (showTypeStr != null) {
            displayType = viewType

            val movieJob = CoroutineScope(Job() + Dispatchers.IO).async {
                val movieRequestUrl = NetworkUtils.buildUrl(showTypeStr,"0109bf9484456665385a3fa881404243")
                try {
                    val jsonMovieResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl!!)
                    val responseList = movieDBJsonUtils.getMovieDetailsFromJson(
                        this@ViewListOfMoviesActivity, jsonMovieResponse!!)
                    responseList
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            GlobalScope.launch(Dispatchers.Main) {
                val movieData = movieJob.await()
                if (movieData != null){
                    for (movieEntry in movieData){
                        val poster_path = movieEntry.poster_path
                        val adult = movieEntry.adult
                        val overview = movieEntry.overview
                        val release_date = movieEntry.release_date
                        val genre_ids = movieEntry.genre_ids
                        val id = movieEntry.id
                        val original_title = movieEntry.original_title
                        val original_language = movieEntry.original_langauge
                        val title = movieEntry.title
                        val backdrop_path = movieEntry.backdrop_path
                        val popularity = movieEntry.popularity
                        val vote_count = movieEntry.vote_count
                        val video = movieEntry.video
                        val vote_average = movieEntry.vote_average

                        withContext(Dispatchers.Main){
                            moviesViewModel.insert(MovieItem(poster_path,adult,overview,release_date,genre_ids,id,
                                original_title,original_language,title,backdrop_path,popularity,vote_count,video,
                                vote_average))
                        }
                    }
                }
            }
        }
    }

    fun sortPopular(v: View) {
        if (displayType != SHOW_BY_POPULAR){
            app_bar.title = "Popular Movies"
            loadMovieData(SHOW_BY_POPULAR)
            Log.d("sort by popularity","cleared db and added popularity movies")
        }
    }

    fun sortTopRated(v: View) {
        if (displayType != SHOW_BY_TOP_RATED){
            app_bar.title = "Top Rated Movies"
            Log.d("sort by top rated","cleared db and added top rated movies")
        }
    }

    fun viewFav(v: View) {
        val i = Intent(this, FavouriteListOfMoviesActivity::class.java)
        startActivity(i)
    }

    fun signOut(v: View) {
        AWSMobileClient.getInstance().signOut(
            SignOutOptions.builder().signOutGlobally(false).build(),
            object : Callback<Void> {
                override fun onResult(result: Void?) {
                    val i = Intent(applicationContext, LoginActivity::class.java)
                    startActivity(i)
                    finish()
                    runOnUiThread {
                        displayToast("Signed Out")
                    }
                }
                override fun onError(e: Exception?) {
                    Log.d("Sign Out", "Error signing out")
                }
            }
        )
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.main, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        when (item?.itemId) {
//            R.id.sortPopular -> {
//                if (displayType != SHOW_BY_POPULAR){
//                    app_bar.title = "Popular Movies"
//                    loadMovieData(SHOW_BY_POPULAR)
//                    Log.d("sort by popularity","cleared db and added popularity movies")
//                }
//            }
//            R.id.sortTopRated -> {
//                if (displayType != SHOW_BY_TOP_RATED){
//                    app_bar.title = "Top Rated Movies"
//                    loadMovieData(SHOW_BY_TOP_RATED)
//                    Log.d("sort by top rated","cleared db and added top rated movies")
//                }
//            }
//            R.id.viewFav -> {
//                val i = Intent(this, FavouriteListOfMoviesActivity::class.java)
//                startActivity(i)
//            }
//            R.id.signOut -> {
//
//                AWSMobileClient.getInstance().signOut(
//                    SignOutOptions.builder().signOutGlobally(false).build(),
//                    object : Callback<Void> {
//                        override fun onResult(result: Void?) {
//                            val i = Intent(applicationContext, LoginActivity::class.java)
//                            startActivity(i)
//                            finish()
//                            runOnUiThread {
//                                displayToast("Signed Out")
//                            }
//                        }
//                        override fun onError(e: Exception?) {
//                            Log.d("Sign Out", "Error signing out")
//                        }
//                    }
//                )
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }

    class MovieListAdapter(context: Context, data:ArrayList<String>, movies: List<MovieItem>?): BaseAdapter() {

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
