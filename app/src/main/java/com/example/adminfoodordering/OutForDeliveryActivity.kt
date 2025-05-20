package com.example.adminfoodordering

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminfoodordering.adapter.DeliveryAdapter
import com.example.adminfoodordering.databinding.ActivityOutForDeliveryBinding
import com.example.foodordering.Model.OrderDetails

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OutForDeliveryActivity : AppCompatActivity() {

    private val binding: ActivityOutForDeliveryBinding by lazy {
        ActivityOutForDeliveryBinding.inflate(layoutInflater)
    }

    private lateinit var database:FirebaseDatabase
    private var listOfCompleteOrderList:ArrayList<OrderDetails> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }
        //Lấy và hiển thị danh sách đơn hàng đã giao
        retrieveCompletedOrders()


//        val customerNames = arrayListOf("Customer 1", "Customer 2", "Customer 3")
//        val moneyStatus = arrayListOf("Đã nhận đuợc hàng", "Chưa nhận được hàng", "Đang chờ nhận hàng")

    }

    private fun retrieveCompletedOrders() {
        database =  FirebaseDatabase.getInstance()
        val completedOrderRef = database.reference.child("CompletedOrder")
            .orderByChild("currentTime")
        completedOrderRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listOfCompleteOrderList.clear()
                for (orderSnapshot in snapshot.children) {
                    val completeOrder = orderSnapshot.getValue(OrderDetails::class.java)
                    completeOrder?.let {
                        listOfCompleteOrderList.add(it)
                    }
                }
                //hiển thị đơn hàng mới nhất
                listOfCompleteOrderList.reverse()
                setDataIntoRecyclerView()

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun setDataIntoRecyclerView() {
        //Khỏi tạo danh sách chứa tên khách hàng và trạng thái thanh toán
        val customerNames = mutableListOf<String>()
        val moneyStatus = mutableListOf<Boolean>()

        for(order in listOfCompleteOrderList){
            order.userName?.let {
                customerNames.add(it)
            }
            moneyStatus.add(order.paymentReceived)
        }

        val adapter = DeliveryAdapter(customerNames, moneyStatus)
        binding.deliveryRecyclerView.adapter = adapter
        binding.deliveryRecyclerView.layoutManager = LinearLayoutManager(this)
    }
}