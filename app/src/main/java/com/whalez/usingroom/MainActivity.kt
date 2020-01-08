package com.whalez.usingroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 앱 데이터베이스 생성
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "todo-db"
        ).allowMainThreadQueries().build()

        db.todoDao().getAll().observe(this, Observer {
            txt_result.text = it.toString()
        })

        btn_add.setOnClickListener {
            db.todoDao().insert(Todo(edit_todo.text.toString()))
            txt_result.text = db.todoDao().getAll().toString()
            edit_todo.text.clear()
        }

    }
}
