package com.example.bookappkotlin.activities

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.databinding.ActivityPdfTtsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pspdfkit.document.PdfDocument
import com.pspdfkit.document.PdfDocumentLoader
import java.io.File
import java.util.Locale

class pdfTtsActivity : AppCompatActivity(), TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {

    var bookId = ""
    var bookTitle = ""
    private lateinit var tts: TextToSpeech

    private val TAG = "PdfTtsActivity"
    private var currentPage = 0
    private var document: PdfDocument? = null
    private var startPage = 0
    private var endPage = 0

    private lateinit var binding: ActivityPdfTtsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPdfTtsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!
        bookTitle = intent.getStringExtra("bookTitle")!!

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        tts = TextToSpeech(this, this).apply {
            setOnUtteranceCompletedListener(this@pdfTtsActivity)
        }

        checkStartEndPages()
    }

    private fun checkStartEndPages() {
        //initialize start and end page numbers

        //firebase referance
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get numbers
                    startPage = "${snapshot.child("startPage").value}".toInt()
                    endPage = "${snapshot.child("endPage").value}".toInt()
                    checkLocalPdf()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to read startPage and endPage from database", error.toException())
                }
            })
    }

    private fun checkLocalPdf() {

        //check pdf from Download folder
        val nameExtension = "$bookTitle.pdf"
        val downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val bookPath = downloadFolder.path + "/" + nameExtension
        val file = File(bookPath)

        if (file.exists()) {
            Log.d(TAG, "checkLocalPdf: PDF file exists")
            processPdf(Uri.fromFile(file))
        } else {
            Log.d(TAG, "checkLocalPdf: PDF file does not exist")
        }
    }



    private fun processPdf(uri: Uri) {
        Log.d(TAG, "Processing PDF URI: $uri")

        try {
            document = PdfDocumentLoader.openDocument(this, uri)
            currentPage = startPage
            displayCurrentPage()
        } catch (e: Exception) {
            Log.e(TAG, "Error processing PDF", e)
        }
    }

    private fun displayCurrentPage() {
        document?.let {
            val pageText = it.getPageText(currentPage)
            val cleanedText = removeExtraSpaces(pageText)
            binding.currentTV.text = cleanedText
            binding.pagesCounterTv.text = String.format("%d/%d", currentPage + 1, it.pageCount)
            speakOut(cleanedText)
        }
    }

    private fun speakOut(text: String) {
        Log.d(TAG, "speakOut: Speaking text: $text")
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID")
        val result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "UniqueID")
        if (result == TextToSpeech.ERROR) {
            Log.e(TAG, "speakOut: Error in speaking text")
        }
    }

    override fun onUtteranceCompleted(utteranceId: String?) {

        //after tts reading
        Log.d(TAG, "onUtteranceCompleted: Utterance completed for ID: $utteranceId")
        runOnUiThread {
            if (currentPage < endPage) {
                currentPage++
                displayCurrentPage()
            }
        }
    }

    private fun removeExtraSpaces(input: String): String {
        return input.split("\\s+".toRegex()).joinToString(" ")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language not supported")
            } else {
                Log.d(TAG, "onInit: Text to Speech initialized successfully")

                //
                displayCurrentPage()
            }
        } else {
            Log.e(TAG, "Initialization failed")
        }
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }

}
