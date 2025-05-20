package com.example.adminfoodordering

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminfoodordering.adapter.PendingOrderAdapter
import com.example.adminfoodordering.databinding.ActivityPendingOrderBinding
import com.example.foodordering.Model.OrderDetails
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PendingOrderActivity : AppCompatActivity(), PendingOrderAdapter.OnItemClicked {

    private lateinit var binding: ActivityPendingOrderBinding
    private var listOfOrderItem: ArrayList<OrderDetails> = arrayListOf()
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseOrderDetails: DatabaseReference
    private lateinit var adapter: PendingOrderAdapter
    private var orderDetailsValueEventListener: ValueEventListener? = null // Để gỡ bỏ listener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        databaseOrderDetails = database.reference.child("OrderDetails")

        setupRecyclerView()
        getOrderDetails()

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        binding.pendingOrderRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PendingOrderAdapter(this, listOfOrderItem, this)
        binding.pendingOrderRecyclerView.adapter = adapter
    }

    private fun getOrderDetails() {
        // Gỡ bỏ listener cũ nếu có
        if (orderDetailsValueEventListener != null) {
            databaseOrderDetails.removeEventListener(orderDetailsValueEventListener!!)
        }
        orderDetailsValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listOfOrderItem.clear()
                for (orderDetailsSnapshot in snapshot.children) {
                    val orderDetails = orderDetailsSnapshot.getValue(OrderDetails::class.java)
                    // Chỉ hiển thị các đơn hàng chưa được gửi đi từ "OrderDetails"
                    if (orderDetails != null && !orderDetails.orderDispatched) {
                        listOfOrderItem.add(orderDetails)
                    }
                }
                listOfOrderItem.sortByDescending { it.currentTime } // Sắp xếp mới nhất lên đầu
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PendingOrderActivity, "Lỗi tải dữ liệu: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("PendingOrderActivity", "Firebase Database Error: ${error.message}")
            }
        }
        databaseOrderDetails.addValueEventListener(orderDetailsValueEventListener!!)
    }

    override fun onItemClickListener(position: Int) {
        if (position < 0 || position >= listOfOrderItem.size) return
        val intent = Intent(this, OrderDetailsActivity::class.java)
        val userOrderDetails = listOfOrderItem[position]
        intent.putExtra("userOrderDetails", userOrderDetails as Parcelable)
        startActivity(intent)
    }

    override fun onItemAcceptClickListener(position: Int) {
        if (position < 0 || position >= listOfOrderItem.size) return

        val orderToUpdate = listOfOrderItem[position]
        val childItemPushKey = orderToUpdate.itemPushKey
        val userIdClickedItem = orderToUpdate.userUid

        if (childItemPushKey.isNullOrEmpty() || userIdClickedItem.isNullOrEmpty()) {
            Toast.makeText(this, "Lỗi: Thiếu thông tin đơn hàng.", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = hashMapOf<String, Any?>(
            "OrderDetails/$childItemPushKey/orderAccepted" to true,
            "OrderDetails/$childItemPushKey/orderDispatched" to false, // Đặt orderDispatched là false khi chấp nhận
            "users/$userIdClickedItem/BuyHistory/$childItemPushKey/orderAccepted" to true,
            "users/$userIdClickedItem/BuyHistory/$childItemPushKey/orderDispatched" to false
        )

        database.reference.updateChildren(updates)
            .addOnSuccessListener {
                // Firebase đã cập nhật, addValueEventListener sẽ tự động làm mới list
                // Không cần cập nhật local và notifyItemChanged thủ công ở đây nữa nếu dùng addValueEventListener
                Toast.makeText(this, "Đơn hàng đã được chấp nhận", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi chấp nhận đơn hàng: ${it.message}", Toast.LENGTH_LONG).show()
                Log.e("PendingOrderActivity", "Error accepting order: ${it.message}")
            }
    }

    override fun onItemDispatchClickListener(position: Int) {
        if (position < 0 || position >= listOfOrderItem.size) return

        val orderToDispatch = listOfOrderItem[position]
        val dispatchItemPushKey = orderToDispatch.itemPushKey

        if (dispatchItemPushKey.isNullOrEmpty()) {
            Toast.makeText(this, "Lỗi: Thiếu key đơn hàng để gửi.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!orderToDispatch.orderAccepted) { // Kiểm tra lại
            Toast.makeText(this, "Đơn hàng chưa được chấp nhận.", Toast.LENGTH_SHORT).show()
            return
        }

        // Cập nhật trạng thái orderDispatched trên Firebase TRƯỚC KHI di chuyển
        val updatesForDispatch = hashMapOf<String, Any?>(
            "OrderDetails/$dispatchItemPushKey/orderDispatched" to true,
            "users/${orderToDispatch.userUid}/BuyHistory/$dispatchItemPushKey/orderDispatched" to true
        )

        database.reference.updateChildren(updatesForDispatch)
            .addOnSuccessListener {
                // Sau khi cập nhật thành công, tạo đối tượng để ghi vào CompletedOrder
                val finalOrderToMove = orderToDispatch.apply {
                    this.orderDispatched = true // Đảm bảo đối tượng local cũng được cập nhật
                }

                val dispatchItemOrderReference = database.reference.child("CompletedOrder").child(dispatchItemPushKey)
                dispatchItemOrderReference.setValue(finalOrderToMove)
                    .addOnSuccessListener {
                        deleteThisItemFromOrderDetails(dispatchItemPushKey) // Không cần position nữa
                    }
                    .addOnFailureListener {e ->
                        Toast.makeText(this, "Lỗi chuyển đơn hàng sang Completed: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("PendingOrderActivity", "Error moving to CompletedOrder: ${e.message}")
                        // Cân nhắc rollback orderDispatched nếu bước này thất bại
                        val rollbackUpdates = hashMapOf<String, Any?>(
                            "OrderDetails/$dispatchItemPushKey/orderDispatched" to false,
                            "users/${orderToDispatch.userUid}/BuyHistory/$dispatchItemPushKey/orderDispatched" to false
                        )
                        database.reference.updateChildren(rollbackUpdates)
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi cập nhật trạng thái gửi: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("PendingOrderActivity", "Error setting orderDispatched: ${e.message}")
            }
    }

    private fun deleteThisItemFromOrderDetails(dispatchItemPushKey: String) {
        val orderDetailsItemReference = database.reference.child("OrderDetails").child(dispatchItemPushKey)
        orderDetailsItemReference.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Đơn hàng được gửi đi và xóa khỏi danh sách chờ", Toast.LENGTH_SHORT).show()
                // addValueEventListener sẽ tự động cập nhật RecyclerView
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi xóa đơn hàng cũ: ${it.message}", Toast.LENGTH_LONG).show()
                Log.e("PendingOrderActivity", "Error deleting from OrderDetails: ${it.message}")
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Gỡ bỏ listener khi Activity bị hủy
        if (orderDetailsValueEventListener != null) {
            databaseOrderDetails.removeEventListener(orderDetailsValueEventListener!!)
        }
    }
}
