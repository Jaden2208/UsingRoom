package com.whalez.usingroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewModel = ViewModelProviders.of(this)[MainViewModel::class.java]

        // UI 갱신
        viewModel.getAll().observe(this, Observer {
            txt_result.text = it.toString()
        })

        // 버튼 클릭 시 DB에 insert
        btn_add.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
               viewModel.insert(Todo(edit_todo.text.toString()))
            }
        }
    }
}
