package com.example.smarttrolly.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smarttrolly.Adapter.ForGettingDate
import com.example.smarttrolly.Adapter.ShoppingHistoryAdapter
import com.example.smarttrolly.R
import com.example.smarttrolly.modles.User
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class UserProfileActivity : AppCompatActivity(),ForGettingDate {

    private lateinit var Username : String
    private lateinit var trolleyId : String
    private lateinit var mDbRef : DatabaseReference
    var UserData = User()
    private lateinit var ProfileImage : ImageView
    private lateinit var StorageReference : StorageReference
    private lateinit var Imagepath : Uri
    private lateinit var adapter: ShoppingHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        Username=intent.getStringExtra("UserName").toString()
        trolleyId=intent.getStringExtra("TrolleyId").toString()
        mDbRef = FirebaseDatabase.getInstance().reference
        StorageReference= FirebaseStorage.getInstance().reference

        //Toast.makeText(this, trolleyId, Toast.LENGTH_SHORT).show()


        ProfileImage=findViewById(R.id.ProfileImage)
        val Name=findViewById<TextView>(R.id.UserName)
        val Email =findViewById<TextView>(R.id.UserEmail)
        val Phone =findViewById<TextView>(R.id.UserPhone)
        val updateBtn=findViewById<Button>(R.id.UpdateBtn)
        val logoutBtn=findViewById<Button>(R.id.LogOutBtn)
        val HomeBtn=findViewById<TextView>(R.id.Home)

        ProfileImage.setOnClickListener {
            UploadImage()
        }


        if(trolleyId=="null" || trolleyId==""){
            HomeBtn.text="Home"
            HomeBtn.setOnClickListener {
                val intent= Intent(this, MainActivity::class.java)
                finish()
                intent.putExtra("UserName",Username)
                startActivity(intent)
            }
            logoutBtn.text="LOGOUT"
            logoutBtn.setOnClickListener {
                val file =File(filesDir,"UserName.txt")
                if(file.delete()){
                    val intent = Intent(this,LogInActyvity::class.java)
                    finish()
                    startActivity(intent)
                }else{
                    Toast.makeText(this, "Some Error Accured", Toast.LENGTH_SHORT).show()
                }
            }
        }else{
            HomeBtn.text="Cart"
            HomeBtn.setOnClickListener {
                val intent= Intent(this, Cart::class.java)
                finish()
                intent.putExtra("UserName",Username)
                intent.putExtra("TrolleyId",trolleyId)
                startActivity(intent)
            }

            logoutBtn.text="Leave Trolley"
            logoutBtn.setOnClickListener {
                val file =File(filesDir,"Trolley.txt")
                if(file.delete()){
                    mDbRef.child("cart").child(trolleyId).removeValue()
                    mDbRef.child("Tolleys").child(trolleyId).setValue("")
                    val intent= Intent(this, UserProfileActivity::class.java)
                    finish()
                    intent.putExtra("UserName",Username)
                    intent.putExtra("TrolleyId","")
                    startActivity(intent)
                }else{
                    Toast.makeText(this, "Some Error Accured", Toast.LENGTH_SHORT).show()
                }
            }
        }

        mDbRef.child("Users").child(Username.substring(0,Username.indexOf('.'))).get().addOnSuccessListener {
            UserData=it.getValue(User::class.java)!!
            Name.text=UserData.name
            Email.text=UserData.email
            Glide.with(this).load(UserData.imageUrl).circleCrop().into(ProfileImage)
            Phone.text=UserData.mobileNumber.toString()

            if (UserData.mobileNumber.length==0) {
                updateBtn.isVisible = true;
                updateBtn.setOnClickListener {
                    val intent = Intent(this, UpdateMobileNumber::class.java)
                    finish()
                    intent.putExtra("UserName", Username)
                    intent.putExtra("TrolleyId", trolleyId)
                    startActivity(intent)
                }
            }

        }.addOnFailureListener{
            Toast.makeText(this, "Failed To Add Product...", Toast.LENGTH_SHORT).show()
        }


        val rcv=findViewById<RecyclerView>(R.id.recyclerview)
        var ProductList=ArrayList<String>()

        mDbRef.child("Shopping").child(Username.substring(0,Username.indexOf('.'))).addValueEventListener(object :
            ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                ProductList.clear()
                for (postSnapshot in snapshot.children) {
                    ProductList.add(postSnapshot.key.toString())
                }
                ProductList.reverse()
                if(ProductList.size>0){
                    findViewById<ImageView>(R.id.NoPuchaseHistory).isVisible=false
                    rcv.isVisible=true
                }else{
                    findViewById<ImageView>(R.id.NoPuchaseHistory).isVisible=true
                    rcv.isVisible=false
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(e: DatabaseError){
                Toast.makeText(this@UserProfileActivity, e.toString(), Toast.LENGTH_SHORT).show()
            }
        })


        adapter = ShoppingHistoryAdapter(ProductList,this)
        rcv.layoutManager = LinearLayoutManager(this)
        rcv.adapter = adapter
    }


    private fun UploadImage() {
        intent= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(intent,123)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 123 && resultCode == RESULT_OK) {
            Imagepath = data!!.data!!
            ProfileImage.setImageURI(Imagepath)
            sendImagetoStorage()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun sendImagetoStorage() {
        val imageref = StorageReference.child("Images").child("Students").child("ProfilePhoto").child(Username.lowercase())

        //Image compresesion
        var bitmap: Bitmap? = null
        try {
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Imagepath)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 10, byteArrayOutputStream)
        val data: ByteArray = byteArrayOutputStream.toByteArray()

        ///putting image to storage
        val uploadTask = imageref.putBytes(data)
        uploadTask.addOnSuccessListener {
            imageref.downloadUrl.addOnSuccessListener { uri ->
                updateImage(uri.toString())
            }.addOnFailureListener {
                Toast.makeText(applicationContext,"Faild To Select Image.", Toast.LENGTH_SHORT).show()
            }
            //Toast.makeText(applicationContext, "Data Is Uploaded", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(applicationContext,"Failed To Upload Image",Toast.LENGTH_SHORT).show()
        }
    }

    override fun ShowItemData(s: String) {
        //Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
        val intent= Intent(this, ShoppingList::class.java)
        finish()
        intent.putExtra("UserName",Username)
        intent.putExtra("TrolleyId",trolleyId)
        intent.putExtra("Time",s)
        startActivity(intent)
    }

    private fun updateImage(Url : String) {
        val path=Username.substring(0,Username.indexOf('.')).lowercase()
        mDbRef.child("Users").child(path).child("imageUrl").setValue(Url)
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