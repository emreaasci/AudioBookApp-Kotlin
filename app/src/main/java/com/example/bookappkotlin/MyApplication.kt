package com.example.bookappkotlin

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.bookappkotlin.activities.PdfDetailActivity
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Locale
import kotlin.math.log

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
    }


    companion object{

        //date format
        fun formatTimeStamp(timestamp: Long) :String{

            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis= timestamp

            return DateFormat.format("dd/MM/yyyy",cal).toString()
        }

        //get pdf size
        fun loadPdfSize(pdfUrl:String,pdfTitle:String,sizeTv : TextView){
            val TAG = "PDF_SIZE_TAG"

            var ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener {storageMetaData->
                    Log.d(TAG, "loadPdfSize: got metadata")
                    val bytes = storageMetaData.sizeBytes.toDouble()
                    Log.d(TAG, "loadPdfSize: Size bytes $bytes")


                    //conver MB/KB
                    val kb = bytes/1024
                    val mb = kb/1024

                    if(mb >= 1){
                        sizeTv.text = "${String.format("%.2f",mb)} MB"
                    }
                    else if (kb >= 1){
                        sizeTv.text = "${String.format("%.2f",kb)} KB"
                    }
                    else{
                        sizeTv.text = "${String.format("%.2f",bytes)} BYTES"
                    }
                }
                .addOnFailureListener {e->
                    Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message}")
                    
                }

        }

        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pagesTv: TextView?
        ){

            val TAG = "PDF_THUBNAIL_TAG"

            var ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener {bytes->
                    Log.d(TAG, "loadPdfSize: Size bytes $bytes")

                    pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { t->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onPageError { page, t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onLoad { nbPages->
                            Log.d(TAG, "loadPdfFromUrlSinglePage: Pages $nbPages")
                            progressBar.visibility = View.INVISIBLE

                            if(pagesTv != null){
                                pagesTv.text = "$nbPages"
                            }


                        }
                        .load()
                }
                .addOnFailureListener {e->
                    Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message}")

                }


        }

        fun loadCategory(categoryId:String,categoryTv : TextView){

            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val category = "${snapshot.child("category").value}"

                        categoryTv.text = category

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }


                })

        }

        fun deleteBook(context: Context, bookId: String, bookUrl: String, bookTitle: String, voiceUrl: String) {
            val TAG = "DELETE_BOOK_TAG"

            Log.d(TAG, "deleteBook: Deleting...")

            // İlerleme dialogu
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please Wait...")
            progressDialog.setMessage("$bookTitle Deleting...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG, "deleteBook: book Deleting from storage and database")

            val bookStorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            bookStorageReference.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteBook: pdf deleting from storage...")

                    val voiceStorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(voiceUrl)
                    voiceStorageReference.delete()
                        .addOnSuccessListener {
                            Log.d(TAG, "deleteBook: voice deleting from storage...")

                            val ref = FirebaseDatabase.getInstance().getReference("Books")
                            ref.child(bookId)
                                .removeValue()
                                .addOnSuccessListener {
                                    progressDialog.dismiss()
                                    Toast.makeText(context, "Succssesfully Deleted.", Toast.LENGTH_SHORT).show()
                                    Log.d(TAG, "deleteBook: book deleted from database.")
                                }
                                .addOnFailureListener { e ->
                                    Log.d(TAG, "deleteBook: Veritabanından silme başarısız oldu: ${e.message}")
                                    Toast.makeText(context, "Veritabanından silme başarısız oldu: ${e.message}", Toast.LENGTH_SHORT).show()
                                    progressDialog.dismiss()
                                }

                            Log.d(TAG, "deleteBook: Ses dosyası silindi")
                        }
                        .addOnFailureListener { e ->
                            Log.d(TAG, "deleteBook: Ses dosyası depodan silme başarısız oldu: ${e.message}")
                            Toast.makeText(context, "Ses dosyası depodan silme başarısız oldu: ${e.message}", Toast.LENGTH_SHORT).show()
                            progressDialog.dismiss()
                        }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "deleteBook: Depodan silme başarısız oldu: ${e.message}")
                    Toast.makeText(context, "Depodan silme başarısız oldu: ${e.message}", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
        }




        fun incrementBookViewCounter(bookId: String){
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var viewsCount = "${snapshot.child("viewsCount").value}"

                        if (viewsCount == "" || viewsCount == "null"){
                            viewsCount = "0"
                        }

                        val newViewsCount = viewsCount.toLong() + 1

                        //update db
                        val hashMap = HashMap<String,Any>()
                        hashMap["viewsCount"] = newViewsCount

                        var dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

        }

        public fun removeFromFav(context: Context,bookId: String){
            val TAG = "REMOVE_FAV_TAG"

            Log.d(TAG, "removeFromFav: Removing from fav")

            val firebaseAuth = FirebaseAuth.getInstance()

            //connect database
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
                .removeValue()
                .addOnSuccessListener {
                    Log.d(TAG, "removeFromFav: Removed from fav")
                    Toast.makeText(context, "Removed from fav", Toast.LENGTH_SHORT).show()

                }
                .addOnFailureListener {e->
                    Log.d(TAG, "removeFromFav: failed to remove due to ${e.message}")
                    Toast.makeText(context, "failed to remove due to ${e.message}", Toast.LENGTH_SHORT).show()

                }

        }


    }
}