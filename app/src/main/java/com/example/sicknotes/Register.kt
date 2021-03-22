package com.example.sicknotes

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

//this below line is used in the video but for some reason it gives me an error...
//import kotlinx.android.synthetic.main.activity_register.*


class RegistrationActivityOLD : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        lateinit var et_Name : EditText
        lateinit var et_register_email : EditText
        lateinit var et_confirm_password : EditText
        lateinit var et_register_password : EditText
        //lateinit var register_button : Button

        val register_button = findViewById<Button>(R.id.register_button)
        register_button.setOnClickListener {
            val intent = Intent(this, Page4::class.java)
            startActivity(intent)

            when {
                TextUtils.isEmpty(et_Name.text.toString().trim {it <=' '}) ->{
                    Toast.makeText(
                        this@RegistrationActivityOLD,
                        "Please Enter Email",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                TextUtils.isEmpty(et_register_email.text.toString().trim {it <=' '}) ->{
                    Toast.makeText(
                        this@RegistrationActivityOLD,
                        "Please Enter Email",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(et_register_password.text.toString().trim { it <= ' '}) -> {
                    Toast.makeText(
                        this@RegistrationActivityOLD,
                        "Please Enter Password",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(et_confirm_password.text.toString().trim { it <= ' '}) -> {
                    Toast.makeText(
                        this@RegistrationActivityOLD,
                        "Please Confirm Password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    val email: String = et_register_email.text.toString(). trim { it <= ' ' }
                    val password: String = et_register_password.text.toString(). trim { it <= ' ' }

                    //Create an instance and create a "register a user" email and password
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(
                            OnCompleteListener<AuthResult> { task ->
                                //If registration is successfully done
                                if(task.isSuccessful) {
                                    //Firebase registered user
                                    val firebaseUser: FirebaseUser = task.result!!.user!!

                                    Toast.makeText(
                                        this@RegistrationActivityOLD,
                                        "You have successfully registered",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    /** now the new user is automatically registered, so we send to Profile Activity
                                     *
                                     */
                                    val intent =
                                        Intent( this@RegistrationActivityOLD, Page4::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    intent.putExtra( "user_id", firebaseUser.uid)
                                    intent.putExtra("email_id", email)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    //if register is unsuccessful, show error message
                                    Toast.makeText(
                                        this@RegistrationActivityOLD,
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