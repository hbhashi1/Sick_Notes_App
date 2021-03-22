package com.example.sicknotes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Page4 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page4)

        val newReading = findViewById<Button>(R.id.new_reading)
        newReading.setOnClickListener {
            val intent = Intent(this, Page5::class.java)
            startActivity(intent)

        }

        val myChartButton = findViewById<Button>(R.id.my_chart)   // app breaks right here
        myChartButton.setOnClickListener {
            val intent = Intent(this, Page6b::class.java)
            startActivity(intent)

        }

        val backButton = findViewById<Button>(R.id.back)   // sending user back to login page
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }

    }
}