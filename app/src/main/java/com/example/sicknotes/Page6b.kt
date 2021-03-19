package com.example.sicknotes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Page6b : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page6b)

        val button3 = findViewById<Button>(R.id.my_chart)
        button3.setOnClickListener {
            val intent = Intent(this, Page6b::class.java)
            startActivity(intent)

        }
    }
}