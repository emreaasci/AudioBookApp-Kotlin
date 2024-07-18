package com.example.bookappkotlin.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.databinding.ActivityCategoryAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CategoryAddActivity : AppCompatActivity() {


    //binding
    private lateinit var binding: ActivityCategoryAddBinding

    //firease auth
    private lateinit var firebaseAuth : FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)

        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()


        //configure progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle click go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle click, upload category
        binding.submitBtn.setOnClickListener {
            validateData()
        }


    }

    private var category = ""

    private fun validateData() {

        category = binding.categoryEt.text.toString().trim()
        
        if(category.isEmpty()){
            Toast.makeText(this, "Enter category name...", Toast.LENGTH_SHORT).show()
        }

        else{
            addCategoryFirebase()
        }

    }

    private fun addCategoryFirebase() {
        progressDialog.show()

        val timestamp = System.currentTimeMillis()
        val hashMap = HashMap<String,Any>()


        //setup data
        hashMap["id"] = "$timestamp"
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        //add data on firebase

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Added successfully...", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add due to ${e.message}", Toast.LENGTH_SHORT).show()
            }


    }
}