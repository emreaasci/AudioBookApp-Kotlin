package com.example.bookappkotlin.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.R
import com.example.bookappkotlin.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding

    //firease auth
    private lateinit var firebaseAuth : FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // init firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        //init progress dPialog create user
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle click no account btn

        binding.noAccountTv.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        //handle login btn

        binding.loginBtn.setOnClickListener {
            /*
            1) ınput data
            2) validate data
            3) login firebase- auth
            4)chrch user type

             */

            validateData()
        }

        //handle forgot password btn
        binding.forgotTv.setOnClickListener {

            startActivity(Intent(this,ForgotPasswordActivity::class.java))

        }
    }


    private var email = ""
    private var password = ""

    private fun validateData() {
        //1) ınput data

        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        //2) validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            Toast.makeText(this,"Invalid email format...", Toast.LENGTH_SHORT).show()

        else if(password.isEmpty())
            Toast.makeText(this,"Enter password...", Toast.LENGTH_SHORT).show()

        else
            loginUser()

    }

    private fun loginUser() {
        progressDialog.setMessage("Logging in...")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                checkUser()

            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this,"Login failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {
        progressDialog.setMessage("Checking user...")

        val firebaseUser = firebaseAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    progressDialog.dismiss()

                    val userType = snapshot.child("userType").value

                    //get user type (user or admin)

                    if(userType == "user"){
                        //its simple user, open user dashboard
                        startActivity(Intent(this@LoginActivity, DashboardUserActivity::class.java))
                        finish()

                    }

                    else if(userType == "admin"){
                        //its admin user, open admin dashboard
                        startActivity(Intent(this@LoginActivity, DashboardAdminActivity::class.java))
                        finish()

                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })

    }
}