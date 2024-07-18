package com.example.bookappkotlin.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    //view Binding
    private lateinit var binding: ActivityMainBinding

    private val sharedPref: SharedPreferences by lazy {
        getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        //handle click, login
        binding.loginBtn.setOnClickListener {

            startActivity(Intent(this, LoginActivity::class.java))

        }

        //handle skip btn

        binding.skipBtn.setOnClickListener{

            startActivity(Intent(this, DashboardUserActivity::class.java))
        }
    }

    private fun checkAndRequestPermission() {
        if (sharedPref.getBoolean("isPermissionGranted", false)) {
            // İzin daha önce verilmiş
            Log.d(TAG, "İzin daha önce verilmiş")
        } else {
            // İzin daha önce verilmemiş, izin iste
            requestStoragePermission.launch(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    android.Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                }
            )
        }
    }

    private val requestStoragePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "İzin verildi")
            sharedPref.edit().putBoolean("isPermissionGranted", true).apply()
        } else {
            Log.d(TAG, "İzin verilmedi")
            Toast.makeText(this, "İzin reddedildi", Toast.LENGTH_SHORT).show()
        }
    }
}