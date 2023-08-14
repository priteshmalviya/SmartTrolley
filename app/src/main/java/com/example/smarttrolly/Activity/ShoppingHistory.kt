package com.example.smarttrolly.Activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smarttrolly.Adapter.ChechOutAdapter
import com.example.smarttrolly.Adapter.ForGettingDate
import com.example.smarttrolly.Adapter.ShoppingHistoryAdapter
import com.example.smarttrolly.R
import com.example.smarttrolly.modles.User
import com.google.firebase.database.*

class ShoppingHistory : AppCompatActivity(),ForGettingDate {


    private lateinit var Username : String
    private lateinit var trolleyId : String
    private lateinit var mDbRef : DatabaseReference
    private lateinit var adapter: ShoppingHistoryAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_history)


            Username=intent.getStringExtra("UserName").toString()
            trolleyId=intent.getStringExtra("TrolleyId").toString()
            mDbRef = FirebaseDatabase.getInstance().reference

        val rcv=findViewById<RecyclerView>(R.id.recyclerview)
        var ProductList=ArrayList<String>()

        mDbRef.child("Shopping").child(Username.substring(0,Username.indexOf('.'))).addValueEventListener(object : ValueEventListener {
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
                Toast.makeText(this@ShoppingHistory, e.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        val userImage=findViewById<ImageView>(R.id.ProfileImage)

        mDbRef.child("Users").child(Username.substring(0,Username.indexOf('.'))).get().addOnSuccessListener {
            val UserData= it.getValue(User::class.java)!!
            Glide.with(this).load(UserData.imageUrl).circleCrop().into(userImage)
        }.addOnFailureListener{
            Toast.makeText(this, "Failed To Add Product...", Toast.LENGTH_SHORT).show()
        }

        userImage.setOnClickListener {
            val intent= Intent(this, UserProfileActivity::class.java)//MainActivity::class.java)
            intent.putExtra("TrolleyId",trolleyId)
            intent.putExtra("UserName",Username)
            finish()
            startActivity(intent)
        }

        adapter = ShoppingHistoryAdapter(ProductList,this)
        rcv.layoutManager = LinearLayoutManager(this)
        rcv.adapter = adapter


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

    override fun onBackPressed() {
        super.onBackPressed()
        val intent= Intent(this, UserProfileActivity::class.java)
        intent.putExtra("TrolleyId",trolleyId)
        intent.putExtra("UserName",Username)
        finish()
        startActivity(intent)
    }
}