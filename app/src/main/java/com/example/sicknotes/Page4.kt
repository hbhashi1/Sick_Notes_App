package com.example.sicknotes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Page4 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page4)

        val userID = intent.getStringExtra("user_id")
        val emailID = intent.getStringExtra("email_id")

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

    }
}