package com.example.smarttrolly.Activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smarttrolly.Adapter.ChechOutAdapter
import com.example.smarttrolly.Adapter.ForPriceUpdate
import com.example.smarttrolly.Adapter.TrolleyAdapter
import com.example.smarttrolly.R
import com.example.smarttrolly.modles.Product
import com.example.smarttrolly.modles.User
import com.google.firebase.database.*
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Attributes
import kotlin.collections.ArrayList

class CheckOutActivity : AppCompatActivity(),ForPriceUpdate,PaymentResultWithDataListener {

    private lateinit var mDbRef : DatabaseReference
    private lateinit var adapter: ChechOutAdapter
    private lateinit var trolleyId : String
    private lateinit var Username : String
    private lateinit var UserData : User
    private var BillingPrice = 0.0f
    var ProductList=ArrayList<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out)

        trolleyId=intent.getStringExtra("TrolleyId").toString()
        Username=intent.getStringExtra("UserName").toString()

        Checkout.preload(this)

        mDbRef = FirebaseDatabase.getInstance().reference

        ProductList=ArrayList()
        val rcv=findViewById<RecyclerView>(R.id.recyclerview)

        findViewById<Button>(R.id.PayButton).setOnClickListener {
            //Toast.makeText(this, "Pay Button", Toast.LENGTH_SHORT).show()
            makePayment()
        }

        findViewById<Button>(R.id.cartBtn).setOnClickListener {
            val intent= Intent(this, Cart::class.java)
            intent.putExtra("TrolleyId",trolleyId)
            intent.putExtra("UserName",Username)
            finish()
            startActivity(intent)
        }

        mDbRef.child("cart").child(trolleyId).addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                ProductList.clear()
                BillingPrice = 0.0f
                for (postSnapshot in snapshot.children) {
                    val product = postSnapshot.getValue(Product::class.java)
                    ProductList.add(product!!)
                    val TotalPrice = product.discountedprice*product.quantity
                    BillingPrice+=TotalPrice
                }
                adapter.notifyDataSetChanged()
                ShowBillingPrice()
            }
            override fun onCancelled(e: DatabaseError){
                Toast.makeText(this@CheckOutActivity, e.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        mDbRef.child("Users").child(Username.substring(0,Username.indexOf('.'))).get().addOnSuccessListener {
            UserData=it.getValue(User::class.java)!!
        }.addOnFailureListener{
            Toast.makeText(this, "Failed To Add Product...", Toast.LENGTH_SHORT).show()
        }

        adapter = ChechOutAdapter(ProductList,this)
        rcv.layoutManager = LinearLayoutManager(this)
        rcv.adapter = adapter

    }

    private fun ShowBillingPrice() {
        //BillingPrice+=ProductTotalPrice
        findViewById<TextView>(R.id.CheckoutPrice).text="Total Price To Pay : Rs. "+BillingPrice.toString()+" /-"
    }


    private fun makePayment(){
        val checkout = Checkout();
        checkout.setKeyID("rzp_test_nIuqqABXzUYdMW")
        try {
            val option=JSONObject()
            option.put("name","Smart Trolley")
            option.put("description","testing the pAYMENT")
            option.put("amount",BillingPrice*100)
            option.put("currency","INR")
            option.put("theme.color","#3399cc")
            //option.put("image","")

            val retryObj = JSONObject()
            retryObj.put("enabled",true)
            retryObj.put("max_count",4)
            option.put("retry",retryObj)

            val prefill = JSONObject()
            prefill.put("email",Username)
            prefill.put("contact",UserData.mobileNumber)

            option.put("prefill",prefill)

            checkout.open(this,option)

        }catch (e:java.lang.Exception){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {
        Toast.makeText(this, "Payment is successfull", Toast.LENGTH_SHORT).show()
        //Toast.makeText(this, p1!!.orderId.toString(), Toast.LENGTH_SHORT).show()
        updatedata(p1)
    }

    private fun updatedata(p1: PaymentData?) {
        //Toast.makeText(this, p1?.data.toString(), Toast.LENGTH_SHORT).show()
        val sdf = SimpleDateFormat("dd-MM-yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())
        for (p in ProductList){
            mDbRef.child("Shopping").child(Username.substring(0,Username.indexOf('.'))).child(currentDate.toString()).child(p.id).setValue(p)
            mDbRef.child("Admin").child(trolleyId).child(p.id).setValue(p)
        }
        mDbRef.child("cart").child(trolleyId).removeValue()
        mDbRef.child("Tolleys").child(trolleyId).setValue("")
        val intent= Intent(this, ShoppingHistory::class.java)
        finish()
        intent.putExtra("UserName",Username)
        startActivity(intent)
        //mDbRef.child("Payments").child(Username.substring(0,Username.indexOf('.'))).setValue(p1?.paymentId.toString())
    }

    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
        Toast.makeText(this, "Payment is Failed", Toast.LENGTH_SHORT).show()
        //findViewById<TextView>(R.id.CheckoutPrice).text=p2?.data.toString()
    }

    override fun onBackPressed() {
        val intent= Intent(this, Cart::class.java)
        intent.putExtra("TrolleyId",trolleyId)
        intent.putExtra("UserName",Username)
        finish()
        startActivity(intent)
    }

}