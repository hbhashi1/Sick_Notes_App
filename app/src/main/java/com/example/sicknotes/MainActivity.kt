package com.example.sicknotes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val registerButton = findViewById<Button>(R.id.register_button) //directing new users to register page
        registerButton.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)

        }

        val loginButton = findViewById<Button>(R.id.login_button) //directing existing users to login
        // change based on user authentication process
        loginButton.setOnClickListener {
            val intent = Intent(this, Page4::class.java)
            startActivity(intent)

        }

        val resetButton = findViewById<Button>(R.id.reset_button) //directing existing users to login
        // change based on user authentication process
        resetButton.setOnClickListener {
            val intent = Intent(this, Reset_password::class.java)
            startActivity(intent)

        }


    }
}
