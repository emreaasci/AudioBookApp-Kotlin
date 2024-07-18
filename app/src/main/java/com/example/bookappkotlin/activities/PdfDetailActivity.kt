package com.example.bookappkotlin.activities


import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.bookappkotlin.Constants
import com.example.bookappkotlin.MyApplication
import com.example.bookappkotlin.MyApplication.Companion.removeFromFav
import com.example.bookappkotlin.R
import com.example.bookappkotlin.adapters.AdapterComment
import com.example.bookappkotlin.databinding.ActivityPdfDetailBinding
import com.example.bookappkotlin.databinding.DialogAddCommentBinding
import com.example.bookappkotlin.models.ModelComment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream
import java.lang.Exception

class PdfDetailActivity : AppCompatActivity() {

    companion object{
        const val TAG = "BOOK_DETAILS_TAG"
    }

    private lateinit var binding:ActivityPdfDetailBinding

    //get from intent
    private var bookId = ""

    //get from firebase
    private var bookTitle = ""
    private var bookUrl = ""
    private var voiceUrl = ""

    private var isFavorite = false

    private lateinit var firebaseAuth:FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private lateinit var commentList:ArrayList<ModelComment>

    private lateinit var adapterComment: AdapterComment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)

        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!


        //init progress Dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        if(firebaseAuth.currentUser != null){
            checkFav()
        }

        MyApplication.incrementBookViewCounter(bookId)
        loadBookDetails()
        showComments()

        //back btn
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle read btn
        binding.readBtn.setOnClickListener {
            val intent = Intent(this, PdfViewActivity::class.java)
            intent.putExtra("bookId",bookId)
            startActivity(intent)
        }

        //handle play butn
        binding.voiceBtn.setOnClickListener {
            listeningChoice()
        }

        //handle download btn
        binding.downloadBtn.setOnClickListener {
            Log.d(TAG, "onCreate: permission already granted")
            downloadBook()
        }

        //handle fav btn
        binding.favBtn.setOnClickListener {

            //check user log in
            if(firebaseAuth.currentUser == null){
                //not log in cant fav
                Toast.makeText(this, "You are not logged in", Toast.LENGTH_SHORT).show()

            }
            else{
                //user log in
                if (isFavorite){
                    MyApplication.removeFromFav(this,bookId)

                }
                else{
                    addToFav()
                }
            }
        }

        //handle show comment
        binding.addCommBtn.setOnClickListener {
            if(firebaseAuth.currentUser == null){
                //not logged in
                Toast.makeText(this, "You're not logged in", Toast.LENGTH_SHORT).show()
            }

            else{
                //logged in
                addCommentDialog()

            }
        }
    }

    private fun listeningChoice(){
        //setup menu
        val popupMenu = PopupMenu(this,binding.voiceBtn)
        popupMenu.menu.add(Menu.NONE,0,0,"Human")
        popupMenu.menu.add(Menu.NONE,1,1,"Text to Speech")
        popupMenu.menu.add(Menu.NONE,2,2,"Goggle Cloud TTS")
        popupMenu.show()


        //handle popup manu
        popupMenu.setOnMenuItemClickListener {item->
            val id = item.itemId
            if(id == 0){
                //click Human
                val intent = Intent(this,pdfPlayActivity::class.java)
                intent.putExtra("bookId",bookId)
                startActivity(intent)

            }
            else if(id == 1){
                //click Text to Speech
                val intent = Intent(this,pdfTtsActivity::class.java)
                intent.putExtra("bookId",bookId)
                intent.putExtra("bookTitle",bookTitle)

                startActivity(intent)

            }

            else if(id == 2){
                //click Google Cloud Text to Speech
                val intent = Intent(this,GogleCloudTtsActivity::class.java)
                intent.putExtra("bookId",bookId)
                intent.putExtra("bookTitle",bookTitle)

                startActivity(intent)
            }


            true
        }

    }

    private fun showComments() {
        commentList = ArrayList()

        //db path
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments")
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear list
                    commentList.clear()
                    for (ds in snapshot.children){
                        //get data from model
                        val model = ds.getValue(ModelComment::class.java)

                        commentList.add(model!!)
                    }

                    //setup adapter
                    adapterComment = AdapterComment(this@PdfDetailActivity,commentList)

                    //setup Rv
                    binding.commentRv.adapter = adapterComment
                }


                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private var comment = ""

    private fun addCommentDialog() {
        val addCommentBinding = DialogAddCommentBinding.inflate(LayoutInflater.from(this))


        //setup alert dialog
        val builder = AlertDialog.Builder(this,R.style.CustomDialog)
        builder.setView(addCommentBinding.root)

        //create dialog
        val alertDialog = builder.create()
        alertDialog.show()

        //dismiss dialog
        addCommentBinding.backBtn.setOnClickListener { alertDialog.dismiss() }


        //add comment
        addCommentBinding.submitBtn.setOnClickListener {
            //get data
            comment = addCommentBinding.commentEt.text.toString().trim()

            //validate data
            if(comment.isEmpty()){
                Toast.makeText(this, "Enter comment...", Toast.LENGTH_SHORT).show()
            }
            else{
                alertDialog.dismiss()
                addComment()
            }
        }



    }

    private fun addComment() {

        //show progress dialog
        progressDialog.setMessage("Adding comment")
        progressDialog.show()

        //timestamp for comment id
        val timestamp = "${System.currentTimeMillis()}"

        //set data
        val hashMap = HashMap<String,Any>()
        hashMap["id"] = "$timestamp"
        hashMap["bookId"] = "$bookId"
        hashMap["timestamp"] = "$timestamp"
        hashMap["comment"] = "$comment"
        hashMap["uid"] = "${firebaseAuth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add comment due to ${e.message}", Toast.LENGTH_SHORT).show()

            }

    }

    private fun downloadBook(){
        Log.d(TAG, "downloadBook: Downloading book")
        progressDialog.setMessage("Downloading book")
        progressDialog.show()

        //download book from firebase
        val ref = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        ref.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes->
                Log.d(TAG, "downloadBook: book downloaded")
                saveDownloadsFolder(bytes)

            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Log.d(TAG, "downloadBook: failed download due to${e.message}")
                Toast.makeText(this, "failed download due to${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun saveDownloadsFolder(bytes: ByteArray){
        Log.d(TAG, "saveDownloadsFolder: saving book pdf")

        val nameExtension = "$bookTitle.pdf"

        try {
            val downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadFolder.mkdirs() //create folder

            val filePath = downloadFolder.path + "/" + nameExtension

            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()

            Log.d(TAG, "saveDownloadsFolder: saved to downloads")
            Toast.makeText(this, "saved to downloads", Toast.LENGTH_LONG).show()
            progressDialog.dismiss()
            incrementDownloadCounter()


        }
        catch (e:Exception){
            progressDialog.dismiss()
            Log.d(TAG, "saveDownloadsFolder: failed save due to ${e.message}")
            Toast.makeText(this, "failed save due to${e.message}", Toast.LENGTH_SHORT).show()

        }
    }

    private fun incrementDownloadCounter() {
        Log.d(TAG, "incrementDownloadCounter: ")
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get count
                    var downloadCount = "${snapshot.child("downloadsCount").value}"
                    Log.d(TAG, "onDataChange: current download count $downloadCount")

                    if(downloadCount == "" || downloadCount == "null"){
                        downloadCount = "0"
                    }


                    val newDownloadCount = downloadCount.toLong() + 1
                    Log.d(TAG, "onDataChange: New download count $newDownloadCount")

                    //setup new data
                    val hashMap = HashMap<String,Any>()
                    hashMap["downloadsCount"] = newDownloadCount

                    //update new data
                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {
                            Log.d(TAG, "onDataChange: counter increment")

                        }

                        .addOnFailureListener {e->
                            Log.d(TAG, "onDataChange: failed update new data due to ${e.message}")

                        }
                }

                override fun onCancelled(error: DatabaseError) {

                }


            })
    }

    private fun loadBookDetails() {

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //getDta
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    bookTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    bookUrl = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"


                    //convert date
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    MyApplication.loadCategory(categoryId, binding.categoryTv)

                    MyApplication.loadPdfFromUrlSinglePage(
                        "$bookUrl",
                        "$bookTitle",
                        binding.pdfView,
                        binding.progressBar,
                        binding.pagesTv
                    )

                    MyApplication.loadPdfSize("$bookUrl", "$bookTitle", binding.sizeTv)

                    //setData
                    binding.titleTv.text = bookTitle
                    binding.descriptionTv.text = description
                    binding.viewsTv.text = viewsCount
                    binding.downloadsTv.text = downloadsCount
                    binding.dateTv.text = date


                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun checkFav(){
        Log.d(TAG, "checkFav: Checking fav")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    isFavorite = snapshot.exists()
                    if (isFavorite){
                        //set fav icon
                        Log.d(TAG, "onDataChange: available in fav")
                        binding.favBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            R.drawable.ic_favorite_white,0,0)
                        binding.favBtn.text = "Remove Favorite"

                    }
                    else{
                        //set fav icon
                        Log.d(TAG, "onDataChange: not available in fav")
                        binding.favBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            R.drawable.ic_favorite_border_white,0,0)
                        binding.favBtn.text = "Add Favorite"

                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }

    private fun addToFav(){

        Log.d(TAG, "addToFav: adding to fav")
        val timestamp = System.currentTimeMillis()

        //setup data
        val hashMap = HashMap<String,Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp

        //save to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                //added fav
                Log.d(TAG, "addToFav: Added to fav")
                Toast.makeText(this, "Added to Fav", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener {e->
                //failed fav
                Log.d(TAG, "addToFav: Failed to fav due to ${e.message}")
                Toast.makeText(this, "Failed to fav due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}