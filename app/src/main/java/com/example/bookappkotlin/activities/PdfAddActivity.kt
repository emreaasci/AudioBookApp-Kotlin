package com.example.bookappkotlin.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.databinding.ActivityPdfAddBinding
import com.example.bookappkotlin.models.ModelCategory
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPdfAddBinding

    private lateinit var firebaseAuth:FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private lateinit var categoryArrayList:ArrayList<ModelCategory>

    private var pdfUri: Uri? =null
    private var voiceUri: Uri? = null

    private val TAG = "PDF_ADD_TAG"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)

        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        loadPdfCategories()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle back btn
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle show category pick dialog
        binding.categoryTv.setOnClickListener {
            categoryPicDialog()
        }

        //handle pick pdf intent
        binding.attachPdfBtn.setOnClickListener {
            pdfPickIntent()
        }

        //handle click voice btn
        binding.attachVoiceBtn.setOnClickListener {
            voicePickIntent()
        }


        //handle upload pdf
        binding.submitBtn.setOnClickListener {
            //validate data
            validateData()

        }

    }

    private var title = ""
    private var description = ""
    private var category = ""
    private var startPage = ""
    private var endPage = ""
    private var author = ""

    private fun validateData() {
        Log.d(TAG, "validateData: validating data")

        //get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()
        startPage = binding.startPageEt.text.toString().trim()
        endPage = binding.endPageEt.text.toString().trim()
        author = binding.authorEt.text.toString().trim()

        if(title.isEmpty()){
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show()
        }

        else if(description.isEmpty()){
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show()
        }

        else if(category.isEmpty()){
            Toast.makeText(this, "Pick Category...", Toast.LENGTH_SHORT).show()
        }
        else if(startPage.isEmpty()){
            Toast.makeText(this, "Pick Category...", Toast.LENGTH_SHORT).show()
        }
        else if(endPage.isEmpty()){
            Toast.makeText(this, "Pick Category...", Toast.LENGTH_SHORT).show()
        }

        else if(pdfUri== null){
            Toast.makeText(this, "Pick PDF File...", Toast.LENGTH_SHORT).show()
        }

        else if(voiceUri == null){
            Toast.makeText(this, "Pick Voice File...", Toast.LENGTH_SHORT).show()
        }

        else{
            //begin upload
            Log.d(TAG, "validateData: $voiceUri")
            uploadPdftoStorage()
            //uploadVoicetoStorage()
        }
    }

    private fun uploadPdftoStorage() {
        //upload pdf to firebase storage
        Log.d(TAG, "uploadPdftoStorage: uploading to storage")

        progressDialog.setMessage("Uploading PDF...")
        progressDialog.show()

        var timestamp = System.currentTimeMillis()
        val firePathAndName = "Books/$timestamp"

        var uploadedPdfUrl:String = ""

        val storageReference = FirebaseStorage.getInstance().getReference(firePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener {taskSnapshot->
                Log.d(TAG, "uploadPdftoStorage: PDF uploaded now gettin url...")

                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while(!uriTask.isSuccessful);
                uploadedPdfUrl = "${uriTask.result}"

                val voicePathAndName = "Voices/$timestamp"
                var uploadedVoiceUrl:String = ""

                val voiceStorage = FirebaseStorage.getInstance().getReference(voicePathAndName)
                voiceStorage.putFile(voiceUri!!)
                    .addOnSuccessListener { taskSnapshot->
                        Log.d(TAG, "uploadPdftoStorage: VOICE uploaded now gettin url...")
                        val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                        while(!uriTask.isSuccessful);
                        uploadedVoiceUrl = "${uriTask.result}"
                        uploadPdfInfoToDb(uploadedPdfUrl,timestamp,uploadedVoiceUrl)

                    }
                    .addOnFailureListener{e->
                        Log.d(TAG, "uploadPdftoStorage: failed voice o upload due to ${e.message}")
                        progressDialog.dismiss()

                        Toast.makeText(this, "Failed o voice upload due to ${e.message}", Toast.LENGTH_SHORT).show()
                    }


            }
            .addOnFailureListener{e->
                Log.d(TAG, "uploadPdftoStorage: failed o upload due to ${e.message}")
                progressDialog.dismiss()

                Toast.makeText(this, "Failed o upload due to ${e.message}", Toast.LENGTH_SHORT).show()
            }

        Log.d(TAG, "uploadPdftoStorage: $uploadedPdfUrl")



        Log.d(TAG, "uploadPdftoStorage: $uploadedPdfUrl")





    }

    private fun uploadPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long,uploadedVoiceUrl:String) {
        Log.d(TAG, "uploadPdfInfoToDb: uploading to db")
        progressDialog.setMessage("Uploading pdf info...")

        val uid = firebaseAuth.uid

        //setup data for upload
        val hashMap: HashMap<String,Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadedPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["downloadsCount"] = 0
        hashMap["startPage"] = "$startPage"
        hashMap["endPage"] = "$endPage"
        hashMap["voiceUrl"] = "$uploadedVoiceUrl"
        hashMap["author"] = "$author"

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "uploadPdfInfoToDb: uploaded to db")
                progressDialog.dismiss()
                Toast.makeText(this, "Uploaded...", Toast.LENGTH_SHORT).show()

                pdfUri = null

            }
            .addOnFailureListener {e->
                Log.d(TAG, "uploadPdfInfoToDb: failed o upload due to ${e.message}")
                progressDialog.dismiss()

                Toast.makeText(this, "Failed o upload due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading pdf categories")

        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //celar list
                categoryArrayList.clear()

                for(ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelCategory::class.java)
                    // add to arraylist
                    categoryArrayList.add(model!!)

                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryPicDialog(){
        Log.d(TAG, "categoryPicDialog: Showing pdf category pick dialog")

        //get cetegory names array from arraylist
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for(i in categoryArrayList.indices){
            categoriesArray[i] = categoryArrayList[i].category
        }

        //alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArray){dialog,which->
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id

                binding.categoryTv.text = selectedCategoryTitle

                Log.d(TAG, "categoryPicDialog: categoryPickDialog: Selected Category ID : $selectedCategoryId")
                Log.d(TAG, "categoryPicDialog: categoryPickDialog: Selected Category Title : $selectedCategoryTitle")
            }
            .show()

    }

    private fun pdfPickIntent(){
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent")
        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT

        pdfActivityResultLauncher.launch(intent)

    }

    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{result->
            if(result.resultCode == RESULT_OK){
                Log.d(TAG, "PDF Picked ")
                pdfUri = result.data!!.data
            }
            else{
                Log.d(TAG, "Pdf Pick Cancelled")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }

        }
    )

    val voiceActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> {result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "Voice Picked")
                voiceUri = result.data!!.data
            } else {
                Log.d(TAG, "Voice Pick Cancelled")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )



    private fun voicePickIntent() {
        Log.d(TAG, "voicePickIntent: starting voice pick intent")
        val intent = Intent()
        intent.type = "audio/mpeg"
        intent.action = Intent.ACTION_GET_CONTENT

        voiceActivityResultLauncher.launch(intent)
    }


}