package com.example.smarttrolly.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.smarttrolly.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UpdateMobileNumber : AppCompatActivity() {

    private lateinit var varificationId : String
    private lateinit var dialog : AlertDialog
    private lateinit var auth : FirebaseAuth
    private lateinit var Username : String
    private lateinit var trolleyId : String
    private lateinit var mDbRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_mobile_number)

        auth= FirebaseAuth.getInstance()

        Username=intent.getStringExtra("UserName").toString()
        trolleyId=intent.getStringExtra("TrolleyId").toString()
        mDbRef = FirebaseDatabase.getInstance().reference

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Please Wait ...")
        builder.setTitle("Loading")
        builder.setCancelable(false)

        dialog = builder.create()

        findViewById<Button>(R.id.Button).setOnClickListener {
            val phoneNumber=findViewById<EditText>(R.id.inpute).text.toString()
            if (phoneNumber!="" && phoneNumber.length==10){
                SendOtp(phoneNumber)
                dialog.show()
            }else{
                Toast.makeText(this, "Please Enter Correct Mobile Number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun SendOtp(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91"+phoneNumber) // Phone number to verify
            .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    Toast.makeText(this@UpdateMobileNumber, "verification Complete", Toast.LENGTH_SHORT).show()
                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    Toast.makeText(this@UpdateMobileNumber, "verification Failed", Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(
                    p0: String,
                    p1: PhoneAuthProvider.ForceResendingToken
                ) {
                    super.onCodeSent(p0, p1)
                    Toast.makeText(this@UpdateMobileNumber, "Otp Sent", Toast.LENGTH_SHORT).show()
                    varificationId = p0
                    dialog.hide()
                    verifyOtp(phoneNumber)
                }
            }).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyOtp(phoneNumber: String) {
        findViewById<EditText>(R.id.inpute).text.clear()
        findViewById<TextView>(R.id.heading).text="Enter Your OTP"
        findViewById<Button>(R.id.Button).text= "Verify OTP"
        findViewById<EditText>(R.id.inpute).hint="Enter Your OTP Here"

        findViewById<Button>(R.id.Button).setOnClickListener {
            dialog.show()
            val otp=findViewById<EditText>(R.id.inpute).text.toString()
            if (otp!=""){
                val credential=PhoneAuthProvider.getCredential(varificationId,otp)

                auth.signInWithCredential(credential).addOnCompleteListener {
                    if (it.isSuccessful){
                        mDbRef.child("Users").child(Username.substring(0,Username.indexOf('.'))).child("mobileNumber").setValue("+91"+phoneNumber)
                        gotoProfile()
                    }else{
                        dialog.hide()
                        Toast.makeText(this, "Verification Failed", Toast.LENGTH_SHORT).show()
                        gotoProfile()
                    }
                }

            }else{
                Toast.makeText(this, "Please Enter OTP", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun gotoProfile() {
        val intent= Intent(this, UserProfileActivity::class.java)
        intent.putExtra("UserName",Username)
        intent.putExtra("TrolleyId",trolleyId)
        finish()
        startActivity(intent)
    }


    override fun onBackPressed() {
        super.onBackPressed()
        gotoProfile()
    }
}