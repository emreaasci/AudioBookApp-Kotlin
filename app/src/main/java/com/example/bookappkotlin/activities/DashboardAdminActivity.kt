package com.example.bookappkotlin.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.adapters.AdapterCategory
import com.example.bookappkotlin.databinding.ActivityDashboardAdminBinding
import com.example.bookappkotlin.models.ModelCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class DashboardAdminActivity : AppCompatActivity() {


    private lateinit var binding: ActivityDashboardAdminBinding

    //firease auth
    private lateinit var firebaseAuth : FirebaseAuth

    //arraylist to hold categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    //adapter
    private lateinit var adapterCategory: AdapterCategory



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()

        //search
        binding.searchEt.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterCategory.filter.filter(s)
                }
                catch (e: Exception){
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })


        //handle logout btn

        binding.logoutBtn.setOnClickListener{
            firebaseAuth.signOut()
            checkUser()
        }

        //handle add category btn
        binding.addCategoryBtn.setOnClickListener {
            startActivity(Intent(this, CategoryAddActivity::class.java))
            //finish()
        }

        //handle pdf add btn
        binding.addPdfFab.setOnClickListener {
            startActivity(Intent(this, PdfAddActivity::class.java))

        }

        //handle profile btn
        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this,ProfileActivity::class.java))
        }

    }

    private fun loadCategories() {
        categoryArrayList = ArrayList()

        //get categories from firebase
        val ref = FirebaseDatabase.getInstance().getReference("Categories")

        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list
                categoryArrayList.clear()
                for(ds in snapshot.children){
                    //get data as model
                    val model = ds.getValue(ModelCategory::class.java)
                    //add arraylist
                    categoryArrayList.add(model!!)

                }
                //setup adapter
                adapterCategory = AdapterCategory(this@DashboardAdminActivity,categoryArrayList)

                //set adapter to rv
                binding.categoriesRv.adapter = adapterCategory


            }

            override fun onCancelled(error: DatabaseError) {

            }


        })
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser

        if(firebaseUser == null){
            //not logging in back main screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        else{
            //logging in show user info

            val email = firebaseUser.email

            binding.subTitleTv.text = email

        }
    }
}