package com.example.sicknotes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Page5 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page5)

        val sendToPhysician = findViewById<Button>(R.id.button7)   // app breaks right here
        sendToPhysician.setOnClickListener {
            val intent = Intent(this, page8::class.java)
            startActivity(intent)
        }

        val Instructions = findViewById<Button>(R.id.button5)   // app breaks right here
        Instructions.setOnClickListener {
            val intent = Intent(this, Bluetooth::class.java)
            startActivity(intent)
        }

        val backButton = findViewById<Button>(R.id.back)   // sending user back to previous page
        backButton.setOnClickListener {
            val intent = Intent(this, Page4::class.java)
            startActivity(intent)
        }
    }
}