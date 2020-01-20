package com.whalez.usingroom

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao // Data Access Object
interface TodoDao {
    @Query("select * from Todo")
    fun getAll(): LiveData<List<Todo>>

    @Insert
    suspend fun insert(todo: Todo)

    @Update
    suspend fun update(todo: Todo)

    @Delete
    suspend fun delete(todo: Todo)
}