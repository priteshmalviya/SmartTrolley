package com.example.smarttrolly.Activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smarttrolly.Adapter.ShoppingHistoryAdapter
import com.example.smarttrolly.Adapter.ShoppingListAdapter
import com.example.smarttrolly.R
import com.example.smarttrolly.modles.Product
import com.example.smarttrolly.modles.User
import com.google.firebase.database.*

class ShoppingList : AppCompatActivity() {


    private lateinit var Username : String
    private lateinit var trolleyId : String
    private lateinit var mDbRef : DatabaseReference
    private lateinit var adapter: ShoppingListAdapter
    private lateinit var time : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)


        Username=intent.getStringExtra("UserName").toString()
        trolleyId=intent.getStringExtra("TrolleyId").toString()
        time=intent.getStringExtra("Time").toString()

        mDbRef = FirebaseDatabase.getInstance().reference

        val rcv=findViewById<RecyclerView>(R.id.recyclerview)
        var ProductList=ArrayList<Product>()

        mDbRef.child("Shopping").child(Username.substring(0,Username.indexOf('.'))).child(time).addValueEventListener(object :
            ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                ProductList.clear()
                for (postSnapshot in snapshot.children) {
                    val product=postSnapshot.getValue(Product::class.java)
                    ProductList.add(product!!)
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(e: DatabaseError){
                Toast.makeText(this@ShoppingList, e.toString(), Toast.LENGTH_SHORT).show()
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

        adapter = ShoppingListAdapter(ProductList)
        rcv.layoutManager = LinearLayoutManager(this)
        rcv.adapter = adapter
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent= Intent(this, UserProfileActivity::class.java)
        finish()
        intent.putExtra("UserName",Username)
        intent.putExtra("TrolleyId",trolleyId)
        startActivity(intent)
    }
}