package com.example.smarttrolly.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smarttrolly.R
import com.example.smarttrolly.modles.Product

class ChechOutAdapter (private val dataSet: ArrayList<Product>, private val listener  : ForPriceUpdate) :
    RecyclerView.Adapter<ChechOutAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView
        val totalPrice : TextView
        val edate : TextView
        val price : TextView

        init {
            name = view.findViewById(R.id.pname)
            totalPrice=view.findViewById(R.id.TotalPrice)
            edate=view.findViewById(R.id.Edate)
            price=view.findViewById(R.id.Price)
        }
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.segment_of_checkout, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (dataSet[position].name.length > 30) {
            viewHolder.name.text = dataSet[position].name.substring(0, 19) + "..."
        } else {
            viewHolder.name.text = dataSet[position].name
        }
        val TotalPrice : Int = (dataSet[position].discountedprice*dataSet[position].quantity)
        viewHolder.totalPrice.text="Total Price : "+TotalPrice.toString()
        viewHolder.edate.text = "Expiry : " + dataSet[position].edate
        viewHolder.price.text = "Price Per Unit : "+dataSet[position].price
        //listener.ShowBillingPrice(TotalPrice)
    }


    override fun getItemCount() = dataSet.size

}

interface ForPriceUpdate {
    //fun ShowBillingPrice(ProductTotalPrice : Float)
}
