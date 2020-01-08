package com.whalez.usingroom

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao // Data Access Object
interface TodoDao {
    @Query("select * from Todo")
    fun getAll(): LiveData<List<Todo>>

    @Insert
    fun insert(todo: Todo)

    @Update
    fun update(todo: Todo)

    @Delete
    fun delete(todo: Todo)
}