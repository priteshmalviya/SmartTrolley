package com.example.smarttrolly.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.smarttrolly.R
import com.example.smarttrolly.modles.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader

class LogInActyvity : AppCompatActivity() {


    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSinginClient: GoogleSignInClient
    private lateinit var mDbRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in_actyvity)

        mDbRef = FirebaseDatabase.getInstance().reference

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSinginClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()

        findViewById<Button>(R.id.sininBtn).setOnClickListener {
            findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
            //findViewById<Button>(R.id.sininBtn).visibility = View.GONE
            sinInGoogle()
        }

        findViewById<Button>(R.id.LogInBtn).setOnClickListener {
            //Toast.makeText(this, "Log In Clicked", Toast.LENGTH_SHORT).show()
            findViewById<ProgressBar>(R.id.progressBar).isVisible=true
            loginwithus()
        }

        findViewById<Button>(R.id.SinUpBtn).setOnClickListener {
            val intent= Intent(this, SinUpActivity::class.java)//MainActivity::class.java)
            finish()
            startActivity(intent)
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
    }

    private fun loginwithus() {
        val email=findViewById<EditText>(R.id.Email).text.toString()
        val password =findViewById<EditText>(R.id.Password).text.toString()

        if (email!="" && password!=""){
            if(email.indexOf('.')==-1 || email.indexOf('@')==-1){
                findViewById<EditText>(R.id.Email).text.clear()
                findViewById<EditText>(R.id.Password).text.clear()
                findViewById<ProgressBar>(R.id.progressBar).isVisible=false
                Toast.makeText(this, "Plese Enter Valid Email Address", Toast.LENGTH_SHORT).show()
                return
            }
            val path=email.substring(0,email.indexOf('.'))
            mDbRef.child("Users").child(path).get().addOnSuccessListener {
                if (it.value==null){
                    Toast.makeText(this, "User Not Exist", Toast.LENGTH_SHORT).show()
                }else{
                    val user=it.getValue(User::class.java)
                    if (user?.password==password){
                        divert(email)
                    }else {
                        Toast.makeText(this, "Password Is Incorrect", Toast.LENGTH_SHORT).show()
                    }
                }
                findViewById<EditText>(R.id.Email).text.clear()
                findViewById<EditText>(R.id.Password).text.clear()
                findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
            }.addOnFailureListener{
                Toast.makeText(this, "Failed To Add Product...", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this, "Email Or Password Can Not Be Empty", Toast.LENGTH_SHORT).show()
            findViewById<ProgressBar>(R.id.progressBar).isVisible=false
        }
    }

    private fun sinInGoogle() {
        val sinInIntent=googleSinginClient.signInIntent
        launcher.launch(sinInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result->
        if(result.resultCode== RESULT_OK){
            val task=GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResult(task)
        }
    }

    private fun handleResult(task: Task<GoogleSignInAccount>) {
        if(task.isSuccessful){
            val account: GoogleSignInAccount? = task.result
            if(account!=null){
                updateUI(account)
            }
        }else{
            Toast.makeText(this,task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential= GoogleAuthProvider.getCredential(account.idToken,null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if(it.isSuccessful){
                addUserToDataBase(firebaseAuth.currentUser?.email,firebaseAuth.currentUser?.displayName)
                divert(firebaseAuth.currentUser?.email.toString())
            }else{
                findViewById<ProgressBar>(R.id.progressBar).visibility= View.GONE
                findViewById<Button>(R.id.sininBtn).visibility= View.VISIBLE
                Toast.makeText(this,it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addUserToDataBase(email: String?, name: String?) {
        val path=email?.substring(0,email.indexOf('.'))
        mDbRef.child("Users").child(path!!).get().addOnSuccessListener {
            if (it.value==null){
                mDbRef.child("Users").child(path).setValue(User(name!!,email))
            }
        }.addOnFailureListener{
            Toast.makeText(this, "Failed To Add Product...", Toast.LENGTH_SHORT).show()
        }
    }


    private fun divert(username : String) {
        saveDataToStorage(username)
        val intent= Intent(this, MainActivity::class.java)
        finish()
        intent.putExtra("UserName",username)
        startActivity(intent)
    }

    private fun saveDataToStorage(username: String) {
        var fos : FileOutputStream?
        try {
            fos=openFileOutput("UserName.txt", MODE_PRIVATE)
            fos.write(username.toByteArray())
            //Toast.makeText(this, filesDir.toString(), Toast.LENGTH_SHORT).show()
        }catch (e : Exception){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        var fis : FileInputStream
        try {
            fis=openFileInput("UserName.txt")
            val isr=InputStreamReader(fis)
            val br=BufferedReader(isr)
            val sb=StringBuilder()
            sb.append(br.readLine())
            divert(sb.toString())
        }catch (e : Exception){
            //Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
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


