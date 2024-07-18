package com.example.bookappkotlin.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.R
import com.example.bookappkotlin.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)

        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle back btn
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle submit butn
        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var mail = ""
    private fun validateData() {
        mail = binding.emailEt.text.toString().trim()

        //validate email
        if(mail.isEmpty()){
            Toast.makeText(this, "Enter email....", Toast.LENGTH_SHORT).show()
        }

        else if(!Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
            Toast.makeText(this, "Invalid email pattern..", Toast.LENGTH_SHORT).show()
        }

        else{
            resetPassword()
        }

    }

    private fun resetPassword() {

        //sending password url
        progressDialog.setMessage("sending password url to $mail")
        progressDialog.show()

        firebaseAuth.sendPasswordResetEmail(mail)
            .addOnSuccessListener {
                progressDialog.dismiss()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "failed to send deu to ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }


}