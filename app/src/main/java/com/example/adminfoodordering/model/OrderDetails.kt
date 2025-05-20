package com.example.foodordering.Model // Hoặc package của app Admin: com.example.adminfoodordering.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable
import java.util.ArrayList // Cần thiết cho Parcelable lists

class OrderDetails() : Serializable, Parcelable { // Implement cả Serializable và Parcelable
    var userUid: String? = null
    var userName: String? = null
    var foodNames: MutableList<String>? = null
    var foodQuantities: MutableList<Int>? = null
    var foodPrices: MutableList<String>? = null
    var foodImages: MutableList<String>? = null
    var address: String? = null
    var totalPrice: String? = null
    var phoneNumber: String? = null
    var orderAccepted: Boolean = false
    var paymentReceived: Boolean = false
    var itemPushKey: String? = null
    var currentTime: Long = 0
    var orderDispatched: Boolean = false // <-- TRƯỜNG MỚI

    // Constructor đầy đủ
    constructor(
        userUid: String?,
        userName: String?,
        foodNames: MutableList<String>?,
        foodQuantities: MutableList<Int>?,
        foodPrices: MutableList<String>?,
        foodImages: MutableList<String>?,
        address: String?,
        totalPrice: String?,
        phoneNumber: String?,
        orderAccepted: Boolean,
        paymentReceived: Boolean,
        itemPushKey: String?,
        currentTime: Long,
        orderDispatched: Boolean
    ) : this() {
        this.userUid = userUid
        this.userName = userName
        this.foodNames = foodNames
        this.foodQuantities = foodQuantities
        this.foodPrices = foodPrices
        this.foodImages = foodImages
        this.address = address
        this.totalPrice = totalPrice
        this.phoneNumber = phoneNumber
        this.orderAccepted = orderAccepted
        this.paymentReceived = paymentReceived
        this.itemPushKey = itemPushKey
        this.currentTime = currentTime
        this.orderDispatched = orderDispatched
    }

    // Constructor cho Parcelable
    constructor(parcel: Parcel) : this() {
        userUid = parcel.readString()
        userName = parcel.readString()

        // Đọc các list String từ Parcel
        parcel.createStringArrayList()?.let { foodNames = it }
        parcel.createStringArrayList()?.let { foodPrices = it }
        parcel.createStringArrayList()?.let { foodImages = it }

        // Đọc MutableList<Int> (foodQuantities)
        val foodQuantitiesSize = parcel.readInt()
        if (foodQuantitiesSize != -1) {
            foodQuantities = ArrayList<Int>(foodQuantitiesSize)
            for (i in 0 until foodQuantitiesSize) {
                foodQuantities?.add(parcel.readInt())
            }
        } else {
            foodQuantities = null
        }

        address = parcel.readString()
        totalPrice = parcel.readString()
        phoneNumber = parcel.readString()
        orderAccepted = parcel.readByte() != 0.toByte()
        paymentReceived = parcel.readByte() != 0.toByte()
        itemPushKey = parcel.readString()
        currentTime = parcel.readLong()
        orderDispatched = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userUid)
        parcel.writeString(userName)

        // Ghi các list String vào Parcel
        parcel.writeStringList(foodNames)
        parcel.writeStringList(foodPrices)
        parcel.writeStringList(foodImages)

        // Ghi MutableList<Int> (foodQuantities)
        if (foodQuantities != null) {
            parcel.writeInt(foodQuantities!!.size)
            for (quantity in foodQuantities!!) {
                parcel.writeInt(quantity)
            }
        } else {
            parcel.writeInt(-1) // Đánh dấu là null
        }

        parcel.writeString(address)
        parcel.writeString(totalPrice)
        parcel.writeString(phoneNumber)
        parcel.writeByte(if (orderAccepted) 1 else 0)
        parcel.writeByte(if (paymentReceived) 1 else 0)
        parcel.writeString(itemPushKey)
        parcel.writeLong(currentTime)
        parcel.writeByte(if (orderDispatched) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OrderDetails> {
        override fun createFromParcel(parcel: Parcel): OrderDetails {
            return OrderDetails(parcel)
        }

        override fun newArray(size: Int): Array<OrderDetails?> {
            return arrayOfNulls(size)
        }
    }
}
