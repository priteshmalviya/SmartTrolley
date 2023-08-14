package com.example.smarttrolly.Other

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.smarttrolly.R
import com.example.smarttrolly.Activity.LogInActyvity

class SpalshScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spalsh_screen)

        Handler().postDelayed({
            val intent = Intent(this, LogInActyvity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }
}