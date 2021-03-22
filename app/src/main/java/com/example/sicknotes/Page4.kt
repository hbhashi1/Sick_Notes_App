package com.example.sicknotes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Page4 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page4)

        val userID = intent.getStringExtra("user_id") //I am just hoping that pulling this info is what allows the user to stay logged in, aldo we can use this if we want to show a welcome message
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

        val backButton = findViewById<Button>(R.id.back)   // sending user back to login page
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }

    }
}