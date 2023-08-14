package com.example.smarttrolly.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.smarttrolly.R
import com.example.smarttrolly.modles.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SinUpActivity : AppCompatActivity() {

    private lateinit var mDbRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sin_up)

        mDbRef = FirebaseDatabase.getInstance().reference

        findViewById<Button>(R.id.LogInBtn).setOnClickListener {
            val intent= Intent(this, LogInActyvity::class.java)
            finish()
            startActivity(intent)
        }

        findViewById<Button>(R.id.SinUpBtn).setOnClickListener {
            findViewById<Button>(R.id.SinUpBtn).isEnabled=false
            sinupWithUs()
        }

        val passseen=findViewById<ImageView>(R.id.passSeen)

        passseen.setOnClickListener {
            if(findViewById<EditText>(R.id.Password).inputType==129){
                findViewById<EditText>(R.id.Password).inputType=1
                Glide.with(this).load(R.drawable.closseyeforpassword).into(passseen)
            }else{
                findViewById<EditText>(R.id.Password).inputType=129
                Glide.with(this).load(R.drawable.openeyeforpassword).into(passseen)
            }
        }

        val consenn=findViewById<ImageView>(R.id.confirmSeen)

        findViewById<ImageView>(R.id.confirmSeen).setOnClickListener {
            if(findViewById<EditText>(R.id.ConfirmPassword).inputType==129){
                findViewById<EditText>(R.id.ConfirmPassword).inputType=1
                Glide.with(this).load(R.drawable.closseyeforpassword).into(consenn)
            }else{
                findViewById<EditText>(R.id.ConfirmPassword).inputType=129
                Glide.with(this).load(R.drawable.openeyeforpassword).into(consenn)
            }
        }
    }

    private fun sinupWithUs() {
        val name=findViewById<EditText>(R.id.Name).text.toString()
        val email=findViewById<EditText>(R.id.Email).text.toString()
        val password=findViewById<EditText>(R.id.Password).text.toString()
        val confirmpassword=findViewById<EditText>(R.id.ConfirmPassword).text.toString()
        findViewById<ProgressBar>(R.id.progressBar).isVisible=true

        if (name !="" && email!="" && password != "" && confirmpassword!=""){
            if (password == confirmpassword){
                if(email.indexOf('.')==-1 || email.indexOf('@')==-1){
                    Toast.makeText(this, "Plese Enter Valid Email Address", Toast.LENGTH_SHORT).show()
                    findViewById<Button>(R.id.SinUpBtn).isEnabled=true
                    findViewById<ProgressBar>(R.id.progressBar).isVisible=false
                    return
                }
                val path=email.substring(0,email.indexOf('.'))
                mDbRef.child("Users").child(path).get().addOnSuccessListener {
                    if (it.value==null){
                        mDbRef.child("Users").child(path).setValue(User(name,email,password))
                        //Toast.makeText(this, "User Is Created", Toast.LENGTH_SHORT).show()
                        divert();
                        //Toast.makeText(this, "is NUll", Toast.LENGTH_SHORT).show()
                    }else{
                        val user=it.getValue(User::class.java)
                        if (user?.password==""){
                            mDbRef.child("Users").child(path).child("password").setValue(password)
                            divert();
                            Toast.makeText(this, "User Is Created", Toast.LENGTH_SHORT).show()
                        }else {
                            Toast.makeText(this, "User is Already Exist", Toast.LENGTH_SHORT).show()
                        }
                    }

                    findViewById<EditText>(R.id.Name).text.clear()
                    findViewById<EditText>(R.id.Email).text.clear()
                    findViewById<EditText>(R.id.Password).text.clear()
                    findViewById<EditText>(R.id.ConfirmPassword).text.clear()
                    findViewById<Button>(R.id.SinUpBtn).isEnabled=true
                    findViewById<ProgressBar>(R.id.progressBar).isVisible=false


                }.addOnFailureListener{
                    Toast.makeText(this, "Failed To Add User...", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Password And Confirm Password Is Different", Toast.LENGTH_SHORT).show()
                findViewById<ProgressBar>(R.id.progressBar).isVisible=false
                findViewById<Button>(R.id.SinUpBtn).isEnabled=true
            }
        }else{
            Toast.makeText(this, "Every Feild Is Mandatory", Toast.LENGTH_SHORT).show()
            findViewById<ProgressBar>(R.id.progressBar).isVisible=false
            findViewById<Button>(R.id.SinUpBtn).isEnabled=true
        }

    }

    private fun divert() {
        val intent = Intent(this,LogInActyvity::class.java)
        finish()
        startActivity(intent)
    }

    override fun onBackPressed() {
        divert()
    }
}