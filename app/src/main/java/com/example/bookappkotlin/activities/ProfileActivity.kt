package com.example.bookappkotlin.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.bookappkotlin.MyApplication

import com.example.bookappkotlin.R
import com.example.bookappkotlin.adapters.AdapterPdfFavorite
import com.example.bookappkotlin.databinding.ActivityProfileBinding
import com.example.bookappkotlin.models.ModelPdf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding:ActivityProfileBinding
    private lateinit var firebaseAuth:FirebaseAuth

    private lateinit var booksArrayList :ArrayList<ModelPdf>

    private lateinit var adapterPdfFavorite: AdapterPdfFavorite


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)

        firebaseAuth = FirebaseAuth.getInstance()

        loadUserInfo()
        loadFavBooks()

        setContentView(binding.root)


        //handle back btn
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle edit btn
        binding.editBtn.setOnClickListener {
            startActivity(Intent(this,ProfileEditActivity::class.java))
        }

    }

    private fun loadUserInfo() {
        //ref db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get info
                    val email = "${snapshot.child("email").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val name = "${snapshot.child("name").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val userType = "${snapshot.child("userType").value}"

                    //convert date
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    //set data
                    binding.userNameTv.text = name
                    binding.mailTv.text = email
                    binding.memberDateTv.text = date
                    binding.accountTypeTv.text = userType

                    //set profile image
                    try {
                        Glide.with(this@ProfileActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileIv)
                    }
                    catch (e:Exception){

                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadFavBooks(){
        booksArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    booksArrayList.clear()
                    for (ds in snapshot.children){
                        val bookId = "${ds.child("bookId").value}"

                        //set model
                        val modelPdf = ModelPdf()
                        modelPdf.id = bookId

                        //add model to list
                        booksArrayList.add(modelPdf)
                    }

                    binding.favBookCountTv.text = "${booksArrayList.size}"

                    //setup adapter
                    adapterPdfFavorite = AdapterPdfFavorite(this@ProfileActivity,booksArrayList)
                    binding.favoriteRv.adapter = adapterPdfFavorite
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }
}