package com.nyp.sit.movieviewer_intermediate_starter_project

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MyMovies(): Application() {
    val appScope = CoroutineScope(SupervisorJob())
    val db by lazy { MoviesRoomDatabase.getDatabase(this,appScope) }
    val repo by lazy { MoviesRepository(db.moviesDao()) }
}