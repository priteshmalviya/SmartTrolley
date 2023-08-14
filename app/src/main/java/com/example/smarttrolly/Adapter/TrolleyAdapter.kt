package com.example.smarttrolly.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smarttrolly.R
import com.example.smarttrolly.modles.Product

class TrolleyAdapter (private val dataSet: ArrayList<Product>, private val listener  : OnButtonClicked, private val context:Context) :
    RecyclerView.Adapter<TrolleyAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView
        val edate:TextView
        val price:TextView
        val quantity : TextView
        val plusBtn : Button
        val minusBtn : Button
        val removBtn : Button
        val discountedPrice : TextView
        val totalPrice : TextView
        val discounte : TextView
        val productImge : ImageView

        init {
            name = view.findViewById(R.id.pname)
            edate=view.findViewById(R.id.Edate)
            price=view.findViewById(R.id.Price)
            quantity=view.findViewById(R.id.quantity)
            plusBtn = view.findViewById(R.id.PlusBtn)
            minusBtn = view.findViewById(R.id.MinusBtn)
            removBtn = view.findViewById(R.id.RemoveBtn)
            discountedPrice = view.findViewById(R.id.DiscountedPrice)
            totalPrice = view.findViewById(R.id.TotalPrice)
            discounte = view.findViewById(R.id.Discounte)
            productImge = view.findViewById(R.id.pimg)
        }
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.segment_of_cart, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (dataSet[position].name.length > 21) {
            viewHolder.name.text = dataSet[position].name.substring(0, 19) + "..."
        } else {
            viewHolder.name.text = dataSet[position].name
        }
        viewHolder.edate.text = "Expiry : " + dataSet[position].edate
        viewHolder.price.text = "Price : " + dataSet[position].price
        viewHolder.quantity.text = dataSet[position].quantity.toString()
        viewHolder.discounte.text = "Discount "+dataSet[position].discount+"%"

        viewHolder.plusBtn.setOnClickListener {
            listener.quatityCange(true, dataSet[position].id)
        }
        viewHolder.minusBtn.setOnClickListener {
            listener.quatityCange(false, dataSet[position].id)
        }
        viewHolder.removBtn.setOnClickListener{
            listener.removeProduct(dataSet[position].id)
        }
        val discountedp :Int = dataSet[position].discountedprice//dataSet[position].price.toFloat()-((dataSet[position].price.toFloat()/100)*dataSet[position].discount.toFloat());
        viewHolder.discountedPrice.text = "Discounted Price : "+discountedp.toString()
        val totalp : Int = (discountedp*dataSet[position].quantity.toInt())
        viewHolder.totalPrice.text = "Total Price : "+totalp.toString();
        Glide.with(viewHolder.itemView).load(dataSet[position].compresedImage).into(viewHolder.productImge)
        //listener.ShowBillingPrice(totalp)
    }

    override fun getItemCount() : Int{
        return dataSet.size
    }

}

interface OnButtonClicked {
    fun quatityCange(flag: Boolean, id: String)
    fun removeProduct(id: String)
    //fun ShowBillingPrice(ProductTotalPrice : Float)
}