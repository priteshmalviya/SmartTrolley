package com.example.smarttrolly.Activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.smarttrolly.R
import com.example.smarttrolly.modles.User
import com.google.firebase.database.*
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var mDbRef : DatabaseReference
    private lateinit var TrolleyId : String
    private lateinit var Username : String
    var UserData : User? = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mDbRef = FirebaseDatabase.getInstance().reference

        Username=intent.getStringExtra("UserName").toString()

        val userImage=findViewById<ImageView>(R.id.ProfileImage)


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        mDbRef.child("Users").child(Username.substring(0,Username.indexOf('.'))).get().addOnSuccessListener {
            UserData=it.getValue(User::class.java)

            findViewById<TextView>(R.id.welcomMessege).text="Welcome\n"+UserData?.name
            Glide.with(this).load(UserData!!.imageUrl).circleCrop().into(userImage)
            findViewById<View>(R.id.LodingPage).isVisible=false
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }.addOnFailureListener{
            Toast.makeText(this, "Failed To Add Product...", Toast.LENGTH_SHORT).show()
        }


        userImage.setOnClickListener {
            val intent= Intent(this, UserProfileActivity::class.java)//MainActivity::class.java)
            //intent.putExtra("TrolleyId",TrolleyId)
            intent.putExtra("UserName",Username)
            intent.putExtra("TrolleyId","")
            finish()
            startActivity(intent)
        }

        CheckTrolley()

        findViewById<Button>(R.id.Button).setOnClickListener {
            TrolleyId=findViewById<EditText>(R.id.inpute).text.toString()
            mDbRef.child("Tolleys").child(TrolleyId).get().addOnSuccessListener {
                //Toast.makeText(this, it.value.toString(), Toast.LENGTH_SHORT).show()
                if (it.value==null){
                    Toast.makeText(this, "Wrong Trolley ID", Toast.LENGTH_SHORT).show()
                    findViewById<EditText>(R.id.inpute).text.clear()
                }else{
                    //Toast.makeText(this, it.value.toString(), Toast.LENGTH_SHORT).show()
                    if (it.value.toString()=="" || it.value.toString()==Username){
                        mDbRef.child("Tolleys").child(TrolleyId).setValue(Username)
                        divert(TrolleyId)
                    }else{
                        Toast.makeText(this, "This Trolley Is Already In Use.", Toast.LENGTH_SHORT).show()
                        findViewById<EditText>(R.id.inpute).text.clear()
                    }
                }
            }.addOnFailureListener{
                Toast.makeText(this, "Failed To Add Product...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun CheckTrolley() {
        //*
        mDbRef.child("Tolleys").addValueEventListener(object :
            ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                //nodeProductList.clear()
                for (postSnapshot in snapshot.children) {
                    val Trolleyid=postSnapshot.getValue(String::class.java)
                    if (Trolleyid==Username){
                        divert(postSnapshot.key.toString())
                    }
                }
                //uploadData(nodeProductList)
            }
            override fun onCancelled(e: DatabaseError){
                Toast.makeText(this@MainActivity, "hello"+e.toString(), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun divert(trolleyid: String) {
        saveDataToStorage(trolleyid)
        val intent= Intent(this, Cart::class.java)
        intent.putExtra("TrolleyId",trolleyid)
        intent.putExtra("UserName",Username)
        finish()
        startActivity(intent)
    }

    private fun saveDataToStorage(trolleyId: String) {
        val fos : FileOutputStream?
        try {
            fos=openFileOutput("Trolley.txt", MODE_PRIVATE)
            fos.write(trolleyId.toByteArray())
            //Toast.makeText(this, filesDir.toString(), Toast.LENGTH_SHORT).show()
        }catch (e : Exception){
            Toast.makeText(this, "hi"+e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            finishAffinity() // Close all activites
            System.exit(0)  // closing files, releasing resources
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Press Again To Exit", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

}