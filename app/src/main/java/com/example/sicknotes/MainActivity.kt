package com.example.sicknotes

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.sicknotes.Register
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lateinit var et_email : EditText
        lateinit var login_button: Button
        lateinit var et_password: EditText

        et_email = findViewById<EditText>(R.id.email)
        et_password = findViewById<EditText>(R.id.password)
        //login_button = findViewById(R.id.login_button) as Button

//* this is supposed to ensure that the email address is valid. but I am struggling to get the edittexts and button names to get grabbed by the system.
        et_email.addTextChangedListener()
        fun afterTextChanged(p0: Editable?) {
        }
        fun beforeTextChanged(p0: CharSequence?, p1:Int, p2: Int, p3: Int) {
        }
        fun onTextChanged(p0: CharSequence?, p1:Int, p2: Int, p3: Int) {
            if(android.util.Patterns.EMAIL_ADDRESS.matcher(et_email.text.toString()).matches())
                login_button.isEnabled = true
            else {
                login_button.isEnabled = false
                et_email.setError("Invalid Email")
            }
        }

        val registerButton = findViewById<Button>(R.id.register_button) //directing new users to register page
        registerButton.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        val resetButton = findViewById<Button>(R.id.reset_button) //directing existing users to login
        // change based on user authentication process
        resetButton.setOnClickListener {
            val intent = Intent(this, Reset_password::class.java)
            startActivity(intent)
        }

        val loginButton = findViewById<Button>(R.id.login_button) //directing existing users to login
        // change based on user authentication process
        loginButton.setOnClickListener {
            val intent = Intent(this, Page4::class.java)
            startActivity(intent)
            //*start my conditional to ensure that user enters email and password

            when { //is for login button
                TextUtils.isEmpty(et_email.text.toString().trim {it <=' '}) ->{
                    Toast.makeText(
                        this@MainActivity,
                        "Please Enter Email",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(et_password.text.toString().trim { it <= ' '}) -> {
                    Toast.makeText(
                        this@MainActivity,
                        "Please Enter Password",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    val email: String = et_email.text.toString(). trim { it <= ' ' }
                    val password: String = et_password.text.toString(). trim { it <= ' ' }

                    //Create an instance and create a "register a user" email and password
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(
                            OnCompleteListener<AuthResult> { task ->
                                //If login is successfully done
                                if(task.isSuccessful) {
                                    //Firebase registered user
                                    val firebaseUser: FirebaseUser = task.result!!.user!!

                                    Toast.makeText(
                                        this@MainActivity,
                                        "You have logged in successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    //* sending user to profile activity page
                                    val intent =
                                        Intent( this@MainActivity, Page4::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK   // this clears old tasks/pages so that when yo upress back or when you move forward to a new page, it clears the one you left
                                    intent.putExtra( "user_id",
                                        FirebaseAuth.getInstance().currentUser!!.uid)
                                    intent.putExtra("email_id", email)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    //if login is unsuccessful, show error message
                                    Toast.makeText(
                                        this@MainActivity,
                                        task.exception!!.message.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                }
            }
        }
    }
}