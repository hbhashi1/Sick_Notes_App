package com.example.sicknotes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Page4 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page4)

        val button2 = findViewById<Button>(R.id.new_reading)
        button2.setOnClickListener {
            val intent = Intent(this, Page5::class.java)
            startActivity(intent)

        }
    }
}