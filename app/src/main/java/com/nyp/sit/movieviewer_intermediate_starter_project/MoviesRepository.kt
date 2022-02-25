package com.nyp.sit.movieviewer_intermediate_starter_project

import com.nyp.sit.movieviewer_intermediate_starter_project.entity.MovieItem

class MoviesRepository(private val moviesDAO: MoviesDAO) {

    val allMovies = moviesDAO.retrieveAllMovies()

    suspend fun insert(movie: MovieItem) {
        moviesDAO.insert(movie)
    }

    suspend fun deleteAll() {
        moviesDAO.deleteAll()
    }

}