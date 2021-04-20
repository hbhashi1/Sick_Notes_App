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
import com.example.sicknotes.R.id.register_button2 as register_button1

//this below line is used in the video but for some reason it gives me an error...
//import kotlinx.android.synthetic.main.activity_register.*

class Register : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // must create the values for the edit texts and buttons and stuff
        val et_Name = findViewById<EditText>(R.id.register_Name)
        val et_register_email = findViewById<EditText>(R.id.register_email)
        val et_register_password = findViewById<EditText>(R.id.register_password)
        val et_confirm_password = findViewById<EditText>(R.id.confirm_password)

        val registerButton2 = findViewById<Button>(R.id.register_button2)
        registerButton2.setOnClickListener {
            //val intent = Intent(this, Page4::class.java)
            //startActivity(intent)
            //I took out the above 2 lines because as soon as the button is pressed, it sends the user to the next page before my code can validate the user...
            //therefore, the proceeding lines of code are needed before sending the user on..
            when {
                TextUtils.isEmpty(et_Name.text.toString().trim {it <=' '}) ->{
                    Toast.makeText(
                        this@Register,
                        "Gotta at least have a name, right?",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                TextUtils.isEmpty(et_register_email.text.toString().trim {it <=' '}) ->{
                    Toast.makeText(
                        this@Register,
                        "We're gonna need that email please",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(et_register_password.text.toString().trim { it <= ' '}) -> {
                    Toast.makeText(
                        this@Register,
                        "Yikes, have you ever made an account before?",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(et_confirm_password.text.toString().trim { it <= ' '}) -> {
                    Toast.makeText(
                        this@Register,
                        "Ummm that password did not match Mr. butterfingers",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    val email: String = et_register_email.text.toString(). trim { it <= ' ' }
                    val password: String = et_register_password.text.toString(). trim { it <= ' ' }

                    //Create an instance and create a "register a user" email and password through Firebase
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(
                            OnCompleteListener<AuthResult> { task ->
                                //If registration is successfully done
                                if(task.isSuccessful) {
                                    //Firebase registered user
                                    val firebaseUser: FirebaseUser = task.result!!.user!!

                                    Toast.makeText(
                                        this@Register,
                                        "YES! You finally registered!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    /** now the new user is automatically registered, so we send to Profile Activity
                                     *
                                     */
                                    val intent =
                                        Intent( this@Register, Page4::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    intent.putExtra( "user_id", firebaseUser.uid)
                                    intent.putExtra("email_id", email)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    //if register is unsuccessful, show error message
                                    Toast.makeText(
                                        this@Register,
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