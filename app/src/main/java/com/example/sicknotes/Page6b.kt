package com.example.sicknotes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Page6b : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page6b)

        val backButton = findViewById<Button>(R.id.back)   // sending user back to previous page
        backButton.setOnClickListener {
            val intent = Intent(this, Page4::class.java)
            startActivity(intent)

        }
    }
}