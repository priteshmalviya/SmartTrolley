package com.example.smarttrolly.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smarttrolly.R

class ShoppingHistoryAdapter (private val dataSet: ArrayList<String>, private val listener  : ForGettingDate) :
    RecyclerView.Adapter<ShoppingHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView
        init {
            name = view.findViewById(R.id.Time)
        }
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.segment_in_shopping_history, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.name.text = dataSet[position]
        viewHolder.itemView.setOnClickListener {
            listener.ShowItemData(dataSet[position])
        }
    }



    override fun getItemCount() = dataSet.size

}

interface ForGettingDate {
    fun ShowItemData(s: String)
}