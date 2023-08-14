package com.example.smarttrolly.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smarttrolly.R
import com.example.smarttrolly.modles.Product

class SearchAdapter(private val dataSet: ArrayList<Product>) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView
        val price:TextView
        val discounte : TextView
        val location : TextView
        val discountedPrice : TextView
        val edate : TextView
        val productImge : ImageView

        init {
            name = view.findViewById(R.id.pname)
            discounte = view.findViewById(R.id.Discounte)
            price=view.findViewById(R.id.Price)
            location=view.findViewById(R.id.Location)
            discountedPrice=view.findViewById(R.id.DiscountedPrice)
            edate=view.findViewById(R.id.Edate)
            productImge = view.findViewById(R.id.pimg)
        }
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.segment_of_search, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (dataSet[position].name.length > 21) {
            viewHolder.name.text = dataSet[position].name.substring(0, 19) + "..."
        } else {
            viewHolder.name.text = dataSet[position].name
        }
        viewHolder.price.text = "Price : " + dataSet[position].price
        viewHolder.discounte.text = "Discount "+dataSet[position].discount+"%"
        //val discountedp :Float = dataSet[position].price.toFloat()-((dataSet[position].price.toFloat()/100)*dataSet[position].discount.toFloat());
        viewHolder.discountedPrice.text = "Discounted Price : "+dataSet[position].discountedprice.toString()
        viewHolder.edate.text = "Expiry : " + dataSet[position].edate
        Glide.with(viewHolder.itemView).load(dataSet[position].compresedImage).into(viewHolder.productImge)
        viewHolder.location.text = dataSet[position].location
    }

    override fun getItemCount() = dataSet.size
}
