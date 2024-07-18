package com.example.bookappkotlin.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.adapters.AdapterPdfAdmin
import com.example.bookappkotlin.databinding.ActivityPdfListAdminBinding
import com.example.bookappkotlin.models.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception


class PdfListAdminActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPdfListAdminBinding

    private lateinit var pdfArrayList : ArrayList<ModelPdf>

    private lateinit var adapterPdfAdmin : AdapterPdfAdmin

    private companion object{
        const val TAG = "PDF_LIST_ADMIN_TAG"
    }

    //category id,title
    private var categoryId = ""
    private var category = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfListAdminBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val intent = intent

        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        //set pdf category
        binding.subTitleTv.text = category

        //load Pdf Books
        loadPdfList()

        //search
        binding.searchEt.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //filtered data
                try {
                    adapterPdfAdmin.filter.filter(s)
                }
                catch (e:Exception){
                    Log.d(TAG, "onTextChanged: ${e.message}")
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        //handle back btn
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

    }

    private fun loadPdfList() {
        pdfArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for(ds in snapshot.children){
                        // get data
                        val model = ds.getValue(ModelPdf::class.java)
                        if (model != null) {
                            pdfArrayList.add(model)
                            Log.d(TAG, "onDataChange: ${model.title} ${model.categoryId}")
                        }
                    }
                    //setup adapter

                    adapterPdfAdmin = AdapterPdfAdmin(this@PdfListAdminActivity,pdfArrayList)
                    binding.booksRv.adapter = adapterPdfAdmin
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }


}