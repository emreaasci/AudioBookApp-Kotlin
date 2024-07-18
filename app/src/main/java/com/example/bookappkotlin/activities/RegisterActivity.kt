package com.example.bookappkotlin.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {


    //binding
    private lateinit var binding: ActivityRegisterBinding

    //firease auth
    private lateinit var firebaseAuth : FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // init firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        //init progress dPialog create user
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //hadnle back button
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

        //hadnle begin register btn

        binding.registerBtn.setOnClickListener {
            /* steps
            1) input data
            2) validate data
            3) create account firebase auth
            4) save user info firecase realtime database
             */

            validateData()
        }
    }

    private var name = ""
    private var email = ""
    private var password = ""

    private fun validateData() {
        //1) input data

        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        var cPassword = binding.cPasswordEt.text.toString().trim()

        //2) validate data

        if(name.isEmpty()){
            Toast.makeText(this,"Enter your name...", Toast.LENGTH_SHORT).show()
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this,"Invalid email address...", Toast.LENGTH_SHORT).show()
        }
        else if(password.isEmpty()) {
            Toast.makeText(this,"Enter password...", Toast.LENGTH_SHORT).show()
        }
        else if(cPassword.isEmpty()) {
            Toast.makeText(this,"Enter confirm password...", Toast.LENGTH_SHORT).show()
        }
        else if(password != cPassword) {
            Toast.makeText(this,"Passwords doesn't match...", Toast.LENGTH_SHORT).show()
        }
        else {
            createUserAccount()
        }
    }



    private fun createUserAccount() {
        //3) create account firebase auth

        progressDialog.setMessage("Creating Account...")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                updateUserInfo()


            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this,"Failed creating account due to ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }



    private fun updateUserInfo() {
        //save user info firecase realtime database

        progressDialog.setMessage("Saving user info...")

        val timestamp = System.currentTimeMillis()

        val uid = firebaseAuth.uid

        val hashMap: HashMap<String,Any?> = HashMap()

        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = ""
        hashMap["userType"] = "user"
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this,"Account created...", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
                finish()
            }

            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this,"Failed saving user info due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}