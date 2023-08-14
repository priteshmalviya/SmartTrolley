package com.example.smarttrolly.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smarttrolly.Adapter.OnButtonClicked
import com.example.smarttrolly.Adapter.TrolleyAdapter
import com.example.smarttrolly.R
import com.example.smarttrolly.modles.Product
import com.example.smarttrolly.modles.User
import com.google.firebase.database.*
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader

class Cart : AppCompatActivity(), OnButtonClicked {


    private lateinit var mDbRef : DatabaseReference
    private lateinit var adapter: TrolleyAdapter
    private lateinit var trolleyId : String
    private lateinit var Username : String
    private lateinit var layout: RelativeLayout
    private lateinit var UserData : User
    private var BillingPrice = 0.0f
    private var more=true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        trolleyId=intent.getStringExtra("TrolleyId").toString()
        Username=intent.getStringExtra("UserName").toString()


        //Toast.makeText(this, trolleyId, Toast.LENGTH_SHORT).show()
        //Toast.makeText(this, Username, Toast.LENGTH_SHORT).show()

        mDbRef = FirebaseDatabase.getInstance().reference

        val ProductList=ArrayList<Product>()
        //val nodeProductList = ArrayList<NodeProduct>()
        val rcv=findViewById<RecyclerView>(R.id.recyclerview)
        layout = findViewById(R.id.Layout)

        findViewById<Button>(R.id.SearchBtn).setOnClickListener {
            val intent= Intent(this, SearchProduct::class.java)
            intent.putExtra("TrolleyId",trolleyId)
            intent.putExtra("UserName",Username)
            finish()
            startActivity(intent)
        }

        val userImage=findViewById<ImageView>(R.id.ProfileImage)

        mDbRef.child("Users").child(Username.substring(0,Username.indexOf('.'))).get().addOnSuccessListener {
            UserData= it.getValue(User::class.java)!!
            Glide.with(this).load(UserData.imageUrl).circleCrop().into(userImage)
        }.addOnFailureListener{
            Toast.makeText(this, "Failed To Add Product...", Toast.LENGTH_SHORT).show()
        }

        mDbRef.child("Tolleys").child(trolleyId).addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value.toString()!=Username){
                    val intent= Intent(this@Cart, MainActivity::class.java)
                    finish()
                    intent.putExtra("UserName",Username)
                    startActivity(intent)
                }
            }
            override fun onCancelled(e: DatabaseError){
                Toast.makeText(this@Cart, e.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        userImage.setOnClickListener {
            val intent= Intent(this, UserProfileActivity::class.java)
            finish()
            intent.putExtra("TrolleyId",trolleyId)
            intent.putExtra("UserName",Username)
            startActivity(intent)
        }

        findViewById<Button>(R.id.CheckOutBtn).setOnClickListener {
            if(BillingPrice>0.0f){
                val intent= Intent(this, CheckOutActivity::class.java)
                finish()
                intent.putExtra("TrolleyId",trolleyId)
                intent.putExtra("UserName",Username)
                startActivity(intent)
            }else{
                Toast.makeText(this, "Please Add Something To Cart", Toast.LENGTH_SHORT).show()
            }
        }

        mDbRef.child("cart").child(trolleyId).addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                ProductList.clear()
                BillingPrice = 0.0f
                for (postSnapshot in snapshot.children) {
                    val product = postSnapshot.getValue(Product::class.java)
                    ProductList.add(product!!)
                    //val discountedp :Float = product.price.toFloat()-((product.price.toFloat()/100)*product.discount.toFloat())
                    val totalp : Int = (product.discountedprice*product.quantity)
                    BillingPrice += totalp
                }
                if(ProductList.size>0){
                    findViewById<ImageView>(R.id.EmptyCartImage).isVisible=false
                    rcv.isVisible=true
                }else{
                    findViewById<ImageView>(R.id.EmptyCartImage).isVisible=true
                    rcv.isVisible=false
                }
                adapter.notifyDataSetChanged()
                ShowBillingPrice()
            }
            override fun onCancelled(e: DatabaseError){
                Toast.makeText(this@Cart, e.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        mDbRef.child("NodeMcu-cart").child(trolleyId).addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                //nodeProductList.clear()
                if(snapshot.value==null){
                    findViewById<ScrollView>(R.id.ScrollLayout).isVisible=false
                }
                for (postSnapshot in snapshot.children) {
                    val ProductId=postSnapshot.getValue(String::class.java)
                    //nodeProductList.add(nodeProduct)
                    //Toast.makeText(this@Cart, ProductId, Toast.LENGTH_SHORT).show()

                    mDbRef.child("products").child(ProductId!!).child("data").get().addOnSuccessListener {
                        if(it.value!=null) {
                            val Product = it.getValue(Product::class.java)!!
                            if (Product != null) {
                                Product.quantity = 1
                                UpdateUI(Product)
                            }
                        }else{
                            findViewById<ScrollView>(R.id.ScrollLayout).isVisible=false
                        }
                    }.addOnFailureListener{
                    }
                }
                //uploadData(nodeProductList)
            }
            override fun onCancelled(e: DatabaseError){
                Toast.makeText(this@Cart, e.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        adapter = TrolleyAdapter(ProductList,this,this)
        rcv.layoutManager = LinearLayoutManager(this)
        rcv.adapter = adapter

    }


    private fun UpdateUI(Product: Product) {
        //Toast.makeText(this@Cart, "Product id "+productId, Toast.LENGTH_SHORT).show()

        val discountedp :Float = Product.price.toFloat() - ((Product.price.toFloat()/100) * Product.discount.toFloat())
        Glide.with(this).load(Product.normalImage).into(layout.findViewById(R.id.ProductImage))
        layout.findViewById<TextView>(R.id.ProductName).text=Product.name
        layout.findViewById<TextView>(R.id.ProductPrice).text="Price : Rs."+Product.price+"/-"
        layout.findViewById<TextView>(R.id.Discount).text="Discount : "+Product.discount+"%"
        layout.findViewById<TextView>(R.id.DicountedPrice).text ="Discounte Price : "+discountedp+"/-"
        layout.findViewById<TextView>(R.id.ExpiryDate).text="Expiry Date : "+Product.edate
        layout.findViewById<TextView>(R.id.PakagingDate).text = "Packed On : "+Product.mdate
        if(Product.description.length>80){
            layout.findViewById<TextView>(R.id.ProductDescription).text="Description : "+Product.description.substring(0,80)+"..."
            layout.findViewById<TextView>(R.id.ProductDescription).setOnClickListener {
                showMoreOrLess(Product.description)
            }
        }else{
            layout.findViewById<TextView>(R.id.ProductDescription).text="Description : "+Product.description
        }


        findViewById<ScrollView>(R.id.ScrollLayout).isVisible=true

        layout.findViewById<Button>(R.id.CancleBtn).setOnClickListener {
            findViewById<ScrollView>(R.id.ScrollLayout).isVisible=false
            mDbRef.child("NodeMcu-cart").child(trolleyId).removeValue()
        }

        layout.findViewById<Button>(R.id.AddBtn).setOnClickListener {
            findViewById<ScrollView>(R.id.ScrollLayout).isVisible=false
            uploadData(Product)
            mDbRef.child("NodeMcu-cart").child(trolleyId).removeValue()
        }
    }

    private fun showMoreOrLess(description: String) {
        if(more){
            layout.findViewById<TextView>(R.id.ProductDescription).text="Description : "+description
        }else{
            layout.findViewById<TextView>(R.id.ProductDescription).text="Description : "+description.substring(0,50)+"..."
        }
        more=!more
    }


    private fun uploadData(pd: Product) {
        mDbRef.child("cart").child(trolleyId).child(pd.id).child("quantity").get().addOnSuccessListener {
            if (it.value==null){
                //Toast.makeText(this, "Not in cart", Toast.LENGTH_SHORT).show()
                mDbRef.child("cart").child(trolleyId).child(pd.id).setValue(pd)
            }else{
                //Toast.makeText(this, "in cart", Toast.LENGTH_SHORT).show()
                quatityCange(true,pd.id)
            }
            Toast.makeText(this, "The Product Is Added.", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener{
            Toast.makeText(this, "Failed To Add Product...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun quatityCange(flag: Boolean, id: String) {
        mDbRef.child("cart").child(trolleyId).child(id).child("quantity").get().addOnSuccessListener {
            var qt=it.value.toString().toInt()
            if (flag) {
                qt++;
                //Toast.makeText(this, "Plus : "+it.value.toString(), Toast.LENGTH_SHORT).show()
                mDbRef.child("cart").child(trolleyId).child(id).child("quantity").setValue(qt)
                //mDbRef.child("NodeMcu-cart").child(trolleyId).child(id).setValue(qt)
            }else{
                //Toast.makeText(this, "Minus : "+id+" "+trolleyId, Toast.LENGTH_SHORT).show()
                //Toast.makeText(this, "Minus : "+it.value.toString(), Toast.LENGTH_SHORT).show()
                if(qt>1) {
                    qt--
                    mDbRef.child("cart").child(trolleyId).child(id).child("quantity").setValue(qt)
                    //mDbRef.child("NodeMcu-cart").child(trolleyId).child(id).setValue(qt)
                }
            }
        }.addOnFailureListener{
            Toast.makeText(this, "Failed To Update...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun removeProduct(id: String) {
        mDbRef.child("cart").child(trolleyId).child(id).removeValue()
    }

    private fun ShowBillingPrice() {
        findViewById<TextView>(R.id.CheckoutPrice).text="Total Price To Pay : Rs. "+BillingPrice.toString()+" /-"
    }

    override fun onStart() {
        super.onStart()
        val fis : FileInputStream
        try {
            fis=openFileInput("Trolley.txt")
            val isr= InputStreamReader(fis)
            val br= BufferedReader(isr)
            val sb=StringBuilder()
            sb.append(br.readLine())
            //Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show()
        }catch (e : Exception){
            finish()
            Toast.makeText(this, "got it"+e.toString(), Toast.LENGTH_SHORT).show()
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