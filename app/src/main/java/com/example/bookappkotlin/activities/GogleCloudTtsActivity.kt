package com.example.bookappkotlin.activities

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.R
import com.example.bookappkotlin.databinding.ActivityGogleCloudTtsBinding
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.texttospeech.v1.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.protobuf.ByteString
import com.pspdfkit.document.PdfDocument
import com.pspdfkit.document.PdfDocumentLoader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class GogleCloudTtsActivity : AppCompatActivity() {

    private val TAG = "TAG_GOOGLE_CLOUD_TTS"
    private val PICK_PDF_FILE = 2
    private var currentPage = 0
    private var document: PdfDocument? = null
    private var startPage = 0
    private var endPage = 0
    private var mediaPlayer: android.media.MediaPlayer? = null

    var bookId = ""
    var bookTitle = ""

    private lateinit var binding: ActivityGogleCloudTtsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGogleCloudTtsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!
        bookTitle = intent.getStringExtra("bookTitle")!!

        binding.backBtn.setOnClickListener {
            stopAudio()
            onBackPressed()
        }

        checkStartEndPages()
    }

    private fun checkStartEndPages() {
        //initialize start and end page numbers

        //firebase reference
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
            val cleanedText = removeSpaces(pageText)
            binding.textV.text = cleanedText
            speakOut(cleanedText)
        }
    }

    private fun speakOut(text: String) {
        Log.d(TAG, "speakOut function called with text: $text")
        try {
            val textToSpeechClient = TextToSpeechClient.create(
                TextToSpeechSettings.newBuilder()
                    .setCredentialsProvider {
                        val inputStream: InputStream = resources.openRawResource(R.raw.text_to_speech_service_account_key)
                        GoogleCredentials.fromStream(inputStream)
                    }
                    .build()
            )
            Log.d(TAG, "TextToSpeechClient created successfully")

            val input = SynthesisInput.newBuilder().setText(text).build()
            val voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode("en-US")
                .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                .build()
            val audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.MP3)
                .build()

            val response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig)
            Log.d(TAG, "Text synthesized successfully")

            val audioContents: ByteString = response.audioContent

            val audioPath = cacheDir.absolutePath + "/output.mp3"
            val audioFile = File(audioPath)
            val outputStream = FileOutputStream(audioFile)
            outputStream.write(audioContents.toByteArray())
            outputStream.close()
            Log.d(TAG, "Audio file written to $audioPath")

            runOnUiThread {
                playAudio(audioPath)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in speakOut", e)
        }
    }

    private fun playAudio(filePath: String) {
        Log.d(TAG, "playAudio function called with filePath: $filePath")
        mediaPlayer = android.media.MediaPlayer().apply {
            setDataSource(filePath)
            prepare()
            start()
        }

        mediaPlayer?.setOnCompletionListener {
            if (currentPage < endPage - 1) {
                currentPage++
                displayCurrentPage()
            }
        }
    }

    private fun stopAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }

    fun removeSpaces(input: String): String {
        return input.split("\\s+".toRegex()).joinToString(" ")
    }
}
