package com.nyp.sit.movieviewer_intermediate_starter_project

import androidx.room.*
import com.nyp.sit.movieviewer_intermediate_starter_project.entity.MovieItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MoviesDAO {
    @Query("Select * from `movies_table`")
    fun retrieveAllMovies(): Flow<List<MovieItem>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(newMovie: MovieItem)

    @Query("Delete from `movies_table`")
    fun deleteAll()

}