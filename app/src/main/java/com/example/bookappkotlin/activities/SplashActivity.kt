package com.example.bookappkotlin.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : AppCompatActivity() {

    //firease auth
    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        firebaseAuth = FirebaseAuth.getInstance()

        Handler().postDelayed( {
            checkUser()
        },1000)

    }

    private fun checkUser() {

        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){
            //ser not logged in go to main screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()

        }

        else{
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {

                        val userType = snapshot.child("userType").value

                        //get user type (user or admin)

                        if(userType == "user"){
                            //its simple user, open user dashboard
                            startActivity(Intent(this@SplashActivity, DashboardUserActivity::class.java))
                            finish()

                        }

                        else if(userType == "admin"){
                            //its admin user, open admin dashboard
                            startActivity(Intent(this@SplashActivity, DashboardAdminActivity::class.java))
                            finish()

                        }
                    }
                    override fun onCancelled(error: DatabaseError) {

                    }
                    
                })
        }
    }
}