package com.whalez.usingroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 앱 데이터베이스 생성
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "todo-db"
        ).build()

        db.todoDao().getAll().observe(this, Observer {
            txt_result.text = it.toString()
        })

        btn_add.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                db.todoDao().insert(Todo(edit_todo.text.toString()))
            }
        }
    }
}
