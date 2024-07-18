package com.example.bookappkotlin.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.databinding.ActivityPdfEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class PdfEditActivity : AppCompatActivity() {

    private lateinit var binding:ActivityPdfEditBinding

    private lateinit var progressDialog:ProgressDialog

    private lateinit var categoryTitleArrayList:ArrayList<String>

    private lateinit var categoryIdArrayList:ArrayList<String>

    private companion object{
        private const val TAG = "PDF_EDIT_TAG"
    }

    private var bookId = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPdfEditBinding.inflate(layoutInflater)

        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)



        loadCategories()
        loadBookInfo()

        //back btn handle
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //pick category
        binding.categoryTv.setOnClickListener {
            categoryDialog()
        }

        //update btn
        binding.submitBtn.setOnClickListener {
            validateData()
        }

    }

    private fun loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Loading book info")
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get books info
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()
                    val author = snapshot.child("author").value.toString()
                    val startPage = snapshot.child("startPage").value.toString()
                    val endPage = snapshot.child("endPage").value.toString()

                    //set book info
                    binding.titleEt.setText(title)
                    binding.descriptionEt.setText(description)
                    binding.authorEt.setText(author)
                    binding.startPageEt.setText(startPage)
                    binding.endPageEt.setText(endPage)

                    //load book category using category id
                    Log.d(TAG, "onDataChange: Loading book category info")
                    val refBookCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refBookCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object :ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                //get category
                                val category = snapshot.child("category").value

                                //set category
                                binding.categoryTv.text = category.toString()
                            }
                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }

    private var title = ""
    private var description = ""
    private var startPage = ""
    private var endPage = ""
    private var author = ""

    private fun validateData() {
        //get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        startPage = binding.startPageEt.text.toString().trim()
        endPage = binding.endPageEt.text.toString().trim()
        author = binding.authorEt.text.toString().trim()

        //validate data
        if(title.isEmpty()){
            Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show()
        }
        else if(description.isEmpty()){
            Toast.makeText(this, "Enter description", Toast.LENGTH_SHORT).show()
        }
        else if(startPage.isEmpty()){
            Toast.makeText(this, "Enter start page", Toast.LENGTH_SHORT).show()
        }
        else if(endPage.isEmpty()){
            Toast.makeText(this, "Enter end page", Toast.LENGTH_SHORT).show()
        }

        else if(selectedCategoryId.isEmpty()){
            Toast.makeText(this, "Pick category", Toast.LENGTH_SHORT).show()
        }

        else if(author.isEmpty()){
            Toast.makeText(this, "enter author name", Toast.LENGTH_SHORT).show()
        }

        else{
            updatePdf()
        }
    }

    private fun updatePdf() {
        Log.d(TAG, "updatePdf: Starting update pdf info")

        progressDialog.setMessage("Updating book info")
        progressDialog.show()

        //setup data ve updating db
        val hashMap = HashMap<String,Any>()
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["startPage"] = "$startPage"
        hashMap["endPage"] = "$endPage"
        hashMap["author"] = "$author"


        //start updating
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d(TAG, "updatePdf: Successfully update...")
                Toast.makeText(this, "Successfully update...", Toast.LENGTH_SHORT).show()
                
            }
            .addOnFailureListener { e->
                Log.d(TAG, "updatePdf: failed to update due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "failed to update due to ${e.message}", Toast.LENGTH_SHORT).show()
                
            }

    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""


    private fun categoryDialog() {
        //make string array from arraylist of string
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for(i in categoryTitleArrayList.indices){
            categoriesArray[i] = categoryTitleArrayList[i]

        }

        //alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose category")
            .setItems(categoriesArray){dialog,position->
                selectedCategoryId = categoryIdArrayList[position]
                selectedCategoryTitle = categoryTitleArrayList[position]

                //set textview
                binding.categoryTv.text = selectedCategoryTitle
            }

            .show()

    }

    private fun loadCategories() {
        Log.d(TAG, "loadCategories: Loading categories...")

        categoryIdArrayList = ArrayList()
        categoryTitleArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()

                for(ds in snapshot.children){

                    val id = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"

                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)

                    Log.d(TAG, "onDataChange: category ıd $id")
                    Log.d(TAG, "onDataChange: category title $category")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }


        })


    }
}