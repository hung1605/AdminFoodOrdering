// PendingOrderActivity.kt
package com.example.adminfoodordering

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminfoodordering.adapter.PendingOrderAdapter
import com.example.adminfoodordering.databinding.ActivityPendingOrderBinding
import com.example.adminfoodordering.model.OrderDetails // Đảm bảo model OrderDetails của bạn có thể Parcelable/Serializable nếu cần
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
    private lateinit var adapter: PendingOrderAdapter // Thêm tham chiếu đến adapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        // Lấy tất cả đơn hàng chưa được chấp nhận hoặc đã chấp nhận nhưng chưa gửi
        databaseOrderDetails = database.reference.child("OrderDetails")

        setupRecyclerView() // Khởi tạo adapter và RecyclerView trước
        getOrderDetails()

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        binding.pendingOrderRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PendingOrderAdapter(this, listOfOrderItem, this) // Truyền listOfOrderItem
        binding.pendingOrderRecyclerView.adapter = adapter
    }

    private fun getOrderDetails() {
        databaseOrderDetails.addValueEventListener(object : ValueEventListener { // Sử dụng addValueEventListener để lắng nghe thay đổi
            override fun onDataChange(snapshot: DataSnapshot) {
                listOfOrderItem.clear() // Xóa danh sách cũ trước khi thêm mới
                for (orderDetailsSnapshot in snapshot.children) {
                    val orderDetails = orderDetailsSnapshot.getValue(OrderDetails::class.java)
                    orderDetails?.let {
                        // Chỉ thêm vào danh sách nếu đơn hàng chưa được xử lý hoàn toàn
                        // (Ví dụ: chưa được đánh dấu là đã gửi đi - bạn có thể thêm 1 trường 'isDispatched' vào OrderDetails)
                        // Hoặc đơn giản là hiển thị tất cả từ "OrderDetails" node
                        listOfOrderItem.add(it)
                    }
                }
                // Đảo ngược danh sách để đơn mới nhất lên đầu (tùy chọn)
                // listOfOrderItem.reverse()
                adapter.notifyDataSetChanged() // Thông báo cho adapter dữ liệu đã thay đổi
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PendingOrderActivity, "Lỗi tải dữ liệu: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("PendingOrderActivity", "Firebase Database Error: ${error.message}")
            }
        })
    }

    // Hàm addDataToListForRecyclerView không còn cần thiết nếu adapter dùng trực tiếp listOfOrderItem

    override fun onItemClickListener(position: Int) {
        if (position < 0 || position >= listOfOrderItem.size) return
        val intent = Intent(this, OrderDetailsActivity::class.java)
        val userOrderDetails = listOfOrderItem[position]
        intent.putExtra("userOrderDetails", userOrderDetails) // Đảm bảo OrderDetails là Serializable hoặc Parcelable
        startActivity(intent)
    }

    override fun onItemAcceptClickListener(position: Int) { // Đổi tên cho rõ ràng
        if (position < 0 || position >= listOfOrderItem.size) return

        val orderToUpdate = listOfOrderItem[position]
        val childItemPushKey = orderToUpdate.itemPushKey
        val userIdClickedItem = orderToUpdate.userUid

        if (childItemPushKey.isNullOrEmpty() || userIdClickedItem.isNullOrEmpty()) {
            Toast.makeText(this, "Lỗi: Thiếu thông tin đơn hàng.", Toast.LENGTH_SHORT).show()
            return
        }

        // Tạo một map để cập nhật nhiều vị trí
        val updates = hashMapOf<String, Any>(
            "OrderDetails/$childItemPushKey/orderAccepted" to true,
            "users/$userIdClickedItem/BuyHistory/$childItemPushKey/orderAccepted" to true
        )

        database.reference.updateChildren(updates)
            .addOnSuccessListener {
                // Cập nhật thành công trên Firebase, giờ cập nhật local data và UI
                orderToUpdate.orderAccepted = true
                adapter.notifyItemChanged(position) // Thông báo cho adapter vẽ lại item này
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

        // Đảm bảo orderAccepted là true trước khi chuyển
        // Mặc dù logic chấp nhận đã làm điều này, nhưng kiểm tra lại không thừa
        if (!orderToDispatch.orderAccepted) {
            Toast.makeText(this, "Vui lòng chấp nhận đơn hàng trước khi gửi!", Toast.LENGTH_SHORT).show()
            return
        }
        // orderToDispatch.orderAccepted = true // Đảm bảo trường này là true khi ghi vào CompletedOrder

        val dispatchItemOrderReference = database.reference.child("CompletedOrder").child(dispatchItemPushKey)

        dispatchItemOrderReference.setValue(orderToDispatch) // Ghi toàn bộ đối tượng đã cập nhật
            .addOnSuccessListener {
                deleteThisItemFromOrderDetails(dispatchItemPushKey, position)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi chuyển đơn hàng: ${it.message}", Toast.LENGTH_LONG).show()
                Log.e("PendingOrderActivity", "Error dispatching order: ${it.message}")
            }
    }

    private fun deleteThisItemFromOrderDetails(dispatchItemPushKey: String, position: Int) {
        val orderDetailsItemReference = database.reference.child("OrderDetails").child(dispatchItemPushKey)
        orderDetailsItemReference.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Đơn hàng được gửi đi", Toast.LENGTH_SHORT).show()
                // Xóa khỏi danh sách local và cập nhật adapter
                // Kiểm tra lại vị trí vì danh sách có thể đã thay đổi nếu dùng addValueEventListener
                // Cách an toàn hơn là tìm lại item bằng pushKey hoặc đảm bảo position vẫn đúng.
                // Với addValueEventListener, danh sách sẽ tự cập nhật, không cần xóa thủ công ở đây
                // nếu bạn chỉ muốn nó biến mất do không còn trong "OrderDetails"
                // Tuy nhiên, nếu bạn muốn hiệu ứng xóa ngay lập tức:
                // listOfOrderItem.removeAt(position) // Cẩn thận nếu dùng addValueEventListener
                // adapter.notifyItemRemoved(position)
                // adapter.notifyItemRangeChanged(position, listOfOrderItem.size)
                // Với addValueEventListener, chỉ cần đợi nó tự refresh là đủ.
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi xóa đơn hàng cũ: ${it.message}", Toast.LENGTH_LONG).show()
                Log.e("PendingOrderActivity", "Error deleting from OrderDetails: ${it.message}")
            }
    }
}