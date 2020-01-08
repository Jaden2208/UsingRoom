package com.whalez.usingroom

import androidx.room.*

@Dao // Data Access Object
interface TodoDao {
    @Query("select * from Todo")
    fun getAll(): List<Todo>

    @Insert
    fun insert(todo: Todo)

    @Update
    fun update(todo: Todo)

    @Delete
    fun delete(todo: Todo)
}