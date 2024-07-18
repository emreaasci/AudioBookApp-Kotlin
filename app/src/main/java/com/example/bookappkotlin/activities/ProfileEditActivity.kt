package com.example.bookappkotlin.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

import com.example.bookappkotlin.R
import com.example.bookappkotlin.databinding.ActivityProfileEditBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.lang.Exception

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding:ActivityProfileEditBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private var imageUri: Uri? = null

    private lateinit var progressDialog:ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileEditBinding.inflate(layoutInflater)

        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait...")
        progressDialog.setCanceledOnTouchOutside(false)


        loadUserInfo()

        //handle back btn
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle pick photo from gallery
        binding.profileIv.setOnClickListener{
            showImageAttach()
        }

        //handle update btn
        binding.updateBtn.setOnClickListener {
            validateData()
        }
    }

    private var name = ""
    private fun validateData() {
        // get data
        name = binding.nameEt.text.toString().trim()

        //validate data
        if (name.isEmpty()){
            Toast.makeText(this, "empty name", Toast.LENGTH_SHORT).show()
        }

        else{
            // name is not empty
            if(imageUri == null){
                updateProfile("")
            }

            else{
                uploadImage()
            }
        }

    }

    private fun updateProfile(uploadedImageUrl: String) {
        progressDialog.setMessage("updating profile...")

        //setup data
        val hashMap: HashMap<String,Any> = HashMap()
        hashMap["name"] = "$name"
        if(imageUri != null){
            hashMap["profileImage"] = uploadedImageUrl
        }

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Toast.makeText(this, "profile updated", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()

            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "failed to update profile due to ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun uploadImage(){
        progressDialog.setMessage("uploading image")
        progressDialog.show()

        val filePath = "ProfileImages/" + firebaseAuth.uid

        //storage ref
        val ref = FirebaseStorage.getInstance().getReference(filePath)
        ref.putFile(imageUri!!)
            .addOnSuccessListener {taskSnapshot->

                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"

                updateProfile(uploadedImageUrl)


            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this, "failed to upload image due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserInfo() {
        //ref db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get info
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val name = "${snapshot.child("name").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"

                    //set data
                    binding.nameEt.setText(name)

                    //set profile image
                    try {
                        Glide.with(this@ProfileEditActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileIv)
                    }
                    catch (e: Exception){

                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }


    private fun showImageAttach(){
        //setup menu
        val popupMenu = PopupMenu(this,binding.profileIv)
        popupMenu.menu.add(Menu.NONE,0,0,"Camera")
        popupMenu.menu.add(Menu.NONE,1,1,"Gallery")
        popupMenu.show()

        //handle popup manu
        popupMenu.setOnMenuItemClickListener {item->
            val id = item.itemId
            if(id == 0){
                //click camera
                pickImageCamera()

            }
            else if(id == 1){
                //click gallery
                pickImageGallery()
            }
            true
        }

    }

    private fun pickImageGallery() {
        //intent uri
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResults.launch(intent)

    }

    private fun pickImageCamera() {
        //intent to pick
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE,"Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
        cameraActivityResults.launch(intent)

    }

    private val cameraActivityResults = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> {result ->
            //get uri
            if(result.resultCode== Activity.RESULT_OK){
                val data = result.data
                //imageUri = data!!.data

                //set image
                binding.profileIv.setImageURI(imageUri)
            }
            else{
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }


        }
    )

    private val galleryActivityResults = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> {result ->
            //get uri
            if(result.resultCode== Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data

                //set image
                binding.profileIv.setImageURI(imageUri)
            }
            else{
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }


        }
    )
}