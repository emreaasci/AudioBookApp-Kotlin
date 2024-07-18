package com.example.bookappkotlin.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.Constants
import com.example.bookappkotlin.databinding.ActivityPdfViewBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfViewActivity : AppCompatActivity() {

    private lateinit var binding:ActivityPdfViewBinding

    var bookId = ""

    private companion object{
        const val TAG = "PDF_VIEW_TAG"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!

        loadBookDetails()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

    }

    private fun loadBookDetails() {
        Log.d(TAG, "loadBookDetails: get pdf from database...")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get pdf url
                    val url = snapshot.child("url").value
                    Log.d(TAG, "onDataChange: PDF_URL $url")

                    loadFromUrl("$url")
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }



    private fun loadFromUrl(url: String) {
        Log.d(TAG, "loadFromUrl: get pdf from storage")

        val ref = FirebaseStorage.getInstance().getReferenceFromUrl(url)
        ref.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes->
                Log.d(TAG, "loadFromUrl: pdf got rom url")
                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange{page,pageCount->
                        val currnetPage = page + 1
                        binding.toolbarSubtitleTv.text = "$currnetPage/$pageCount"
                        Log.d(TAG, "loadFromUrl: $currnetPage/$pageCount")
                    }
                    .onError { t->

                        Log.d(TAG, "loadFromUrl: ${t.message}")
                    }
                    .onPageError{page,t->
                        Log.d(TAG, "loadFromUrl: ${t.message}")
                    }
                    .load()
                binding.progressBar.visibility = View.GONE

            }
            .addOnFailureListener {e->
                Log.d(TAG, "loadFromUrl: failed to get url due to ${e.message}")
                binding.progressBar.visibility = View.GONE

            }
    }
}