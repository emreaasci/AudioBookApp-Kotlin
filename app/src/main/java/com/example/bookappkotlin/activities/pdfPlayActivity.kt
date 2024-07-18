package com.example.bookappkotlin.activities

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bookappkotlin.MyApplication
import com.example.bookappkotlin.R
import com.example.bookappkotlin.databinding.ActivityPdfPlayBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.TimeUnit

class pdfPlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfPlayBinding
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var audioUri: Uri? = null
    private val handler = Handler(Looper.getMainLooper())

    private val TAG = "PLAY_BOOK_TAG"

    var bookId = ""

    var bookTitle = ""
    var bookUrl = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!


        loadBookDetails()
        loadBookUrl()


        binding.backBtn.setOnClickListener {
            onBackPressed()
        }


        binding.playBtn.setOnClickListener {
            if (audioUri != null && !isPlaying) {
                mediaPlayer = MediaPlayer.create(this, audioUri)
                mediaPlayer?.start()
                isPlaying = true
                binding.playBtn.isEnabled = false
                binding.pauseBtn.isEnabled = true
                binding.stopBtn.isEnabled = true
                binding.seekBar.isEnabled = true
                binding.seekBar.max = mediaPlayer!!.duration
                binding.totalTime.text = formatTime(mediaPlayer!!.duration)
                updateSeekBar()
            }
        }

        binding.pauseBtn.setOnClickListener {
            if (isPlaying) {
                mediaPlayer?.pause()
                isPlaying = false
                binding.playBtn.isEnabled = true
                binding.pauseBtn.isEnabled = false
            }
        }

        binding.stopBtn.setOnClickListener {
            if (isPlaying) {
                mediaPlayer?.stop()
                mediaPlayer?.reset()
                isPlaying = false
                binding.playBtn.isEnabled = true
                binding.pauseBtn.isEnabled = false
                binding.stopBtn.isEnabled = false
                binding.seekBar.isEnabled = false
                binding.currentTime.text = "00:00"
                binding.seekBar.progress = 0
                handler.removeCallbacksAndMessages(null)
            }

        }

        binding.fifteenSecondsAheadBtn.setOnClickListener {
            if (isPlaying) {
                val currentPosition = mediaPlayer?.currentPosition ?: 0
                val newPosition = currentPosition + 15000 // 15 seconds
                mediaPlayer?.seekTo(newPosition)
                binding.seekBar.progress = newPosition
                binding.currentTime.text = formatTime(newPosition)
            }
        }

        binding.fifteenSecondsBackBtn.setOnClickListener {
            if (isPlaying) {
                val currentPosition = mediaPlayer?.currentPosition ?: 0
                val newPosition = currentPosition - 15000 // 15 seconds
                mediaPlayer?.seekTo(newPosition)
                binding.seekBar.progress = newPosition
                binding.currentTime.text = formatTime(newPosition)
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && isPlaying) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun loadBookDetails(){

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val bookTitle = "${snapshot.child("title").value}"
                    val bookUrl = "${snapshot.child("url").value}"
                    val author = "${snapshot.child("author").value}"

                    MyApplication.loadPdfFromUrlSinglePage("$bookUrl","$bookTitle",binding.pdfView,binding.progressBar,null)


                    //set data
                    binding.titleTv.text = bookTitle
                    binding.authorTv.text = author
                }

                override fun onCancelled(error: DatabaseError) {

                }


            })

    }


    private fun loadBookUrl() {
        Log.d(TAG, "loadBookDetails: gettin voice firebase")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get pdf url
                    val url = snapshot.child("voiceUrl").value
                    bookUrl = "$url"
                    Log.d(TAG, "onDataChange: VOICE_URL $url")

                    if (url != null){
                        loadVoiceFromUrl("$url")
                    }
                    else
                        Toast.makeText(this@pdfPlayActivity, "no voice", Toast.LENGTH_SHORT).show()


                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadVoiceFromUrl(url: String) {
        audioUri = Uri.parse(url)
        binding.playBtn.isEnabled = true
        Toast.makeText(this, "Audio loaded from URL", Toast.LENGTH_SHORT).show()
    }


    private fun updateSeekBar() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isPlaying) {
                    binding.seekBar.progress = mediaPlayer?.currentPosition ?: 0
                    binding.currentTime.text = formatTime(mediaPlayer?.currentPosition ?: 0)
                    handler.postDelayed(this, 1000)
                }
            }
        }, 1000)
    }


    private fun formatTime(milliseconds: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
    }}