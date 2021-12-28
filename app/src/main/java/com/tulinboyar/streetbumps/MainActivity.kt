package com.tulinboyar.streetbumps

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth= FirebaseAuth.getInstance()

        val activeUser=auth.currentUser
        if(activeUser!=null){
            val intent=Intent(applicationContext,MapsActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
    fun logIn(view:View) {
        val email = emailText.text.toString()
        val password = passwordText.text.toString()

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                Toast.makeText(
                    applicationContext,
                    "Welcome:${auth.currentUser?.email.toString()}",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(applicationContext, MapsActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener { exception ->
            if (exception != null) {
                Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
    fun signUp(view: View){
        val email=emailText.text.toString()
        val password=passwordText.text.toString()
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{task->
            if(task.isSuccessful){
                val intent= Intent(applicationContext,MapsActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener{exception->
            if(exception!=null){
                Toast.makeText(applicationContext,exception.localizedMessage, Toast.LENGTH_SHORT).show()

            }
        }
    }
}
