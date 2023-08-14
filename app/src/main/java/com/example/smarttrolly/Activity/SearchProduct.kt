package com.example.smarttrolly.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smarttrolly.Adapter.SearchAdapter
import com.example.smarttrolly.R
import com.example.smarttrolly.modles.Product
import com.google.firebase.database.*

class SearchProduct : AppCompatActivity() {

    private lateinit var trolleyId : String
    private lateinit var mDbRef : DatabaseReference
    private lateinit var adapter: SearchAdapter
    private lateinit var Username : String
    val ProductList=ArrayList<Product>()
    val ProductListMatched=ArrayList<Product>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_product)


        trolleyId=intent.getStringExtra("TrolleyId").toString()
        Username=intent.getStringExtra("UserName").toString()

        val rcv=findViewById<RecyclerView>(R.id.recyclerview)
        mDbRef = FirebaseDatabase.getInstance().reference

        val closMap=findViewById<TextView>(R.id.ClosMap)

        findViewById<Button>(R.id.OpenMapBtn).setOnClickListener {
            closMap.isVisible=true
            findViewById<ImageView>(R.id.map).isVisible=true
        }

        closMap.setOnClickListener {
            closMap.isVisible=false
            findViewById<ImageView>(R.id.map).isVisible=false
        }

        mDbRef.child("products").addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                ProductList.clear()
                for (postSnapshot in snapshot.children) {
                    val product = snapshot.child(postSnapshot.key+"/data").getValue(Product::class.java)
                    ProductList.add(product!!)
                    ProductListMatched.add(product!!)
                }
                if(ProductList.size>0){
                    findViewById<ImageView>(R.id.ProductNotFoundImage).isVisible=false
                    rcv.isVisible=true
                }else{
                    findViewById<ImageView>(R.id.ProductNotFoundImage).isVisible=true
                    rcv.isVisible=false
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(e: DatabaseError){
                Toast.makeText(this@SearchProduct, e.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        val searchBox =findViewById<EditText>(R.id.SearchBox)

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                ProductListMatched.clear()
                for(p in ProductList){
                    if (p.name.toLowerCase().contains(p0.toString().toLowerCase()) || p.id.contains(p0.toString().toLowerCase())){
                        ProductListMatched.add(p)
                    }
                }
                if(ProductListMatched.size>0){
                    findViewById<ImageView>(R.id.ProductNotFoundImage).isVisible=false
                    rcv.isVisible=true
                }else{
                    findViewById<ImageView>(R.id.ProductNotFoundImage).isVisible=true
                    rcv.isVisible=false
                }
                adapter.notifyDataSetChanged()
            }
        })

        findViewById<Button>(R.id.GotoCartBtn).setOnClickListener {
            val intent= Intent(this, Cart::class.java)//MainActivity::class.java)
            intent.putExtra("TrolleyId",trolleyId)
            intent.putExtra("UserName",Username)
            finish()
            startActivity(intent)
        }

        adapter = SearchAdapter(ProductListMatched)
        rcv.layoutManager = LinearLayoutManager(this)
        rcv.adapter = adapter

    }


    override fun onBackPressed() {
        val intent= Intent(this, Cart::class.java)//MainActivity::class.java)
        intent.putExtra("TrolleyId",trolleyId)
        intent.putExtra("UserName",Username)
        finish()
        startActivity(intent)
    }

}
