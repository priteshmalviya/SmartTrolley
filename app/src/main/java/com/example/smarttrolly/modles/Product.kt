package com.example.smarttrolly.modles

class Product (
    val brand : String = "" ,
    val description : String = "" ,
    val discount: String = "",
    val edate:String ="",
    val id : String = "",
    val mdate : String = "",
    val name:String="",
    val price : String = "",
    var quantity : Int = 0,
    val compresedImage : String ="" ,
    val normalImage : String = "",
    val location : String = "",
    val categories : String = "",
    val discountedprice : Int = 0
)

class DataForProduct(
    val data : Product=Product()
)