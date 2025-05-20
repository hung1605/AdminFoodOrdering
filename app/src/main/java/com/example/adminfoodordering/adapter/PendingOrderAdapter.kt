package com.example.adminfoodordering.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminfoodordering.databinding.PendingOrderItemBinding
import com.example.adminfoodordering.model.OrderDetails // Import OrderDetails

class PendingOrderAdapter(
    private val context: Context,
    private val orderItems: List<OrderDetails>, // Thay đổi thành List<OrderDetails>
    private val itemClicked: OnItemClicked
) : RecyclerView.Adapter<PendingOrderAdapter.PendingOrderViewHolder>() {

    interface OnItemClicked {
        fun onItemAcceptClickListener(position: Int) // Đổi tên cho rõ ràng
        fun onItemClickListener(position: Int)
        fun onItemDispatchClickListener(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingOrderViewHolder {
        val binding =
            PendingOrderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PendingOrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PendingOrderViewHolder, position: Int) {
        val currentOrderItem = orderItems[position]
        holder.bind(currentOrderItem, position) // Truyền cả đối tượng OrderDetails
    }

    override fun getItemCount(): Int = orderItems.size

    inner class PendingOrderViewHolder(private val binding: PendingOrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Không cần biến isAccepted ở đây nữa

        fun bind(orderItem: OrderDetails, position: Int) { // Nhận OrderDetails
            binding.apply {
                customerName.text = orderItem.userName ?: "N/A"
                // Giả sử quantity ở đây bạn muốn hiển thị tổng tiền
                pendingOrderQuantity.text = orderItem.totalPrice ?: "0đ"

                // Lấy ảnh đầu tiên từ danh sách ảnh (nếu có)
                val imageUrl = orderItem.foodImages?.firstOrNull { it.isNotEmpty() }
                if (imageUrl != null) {
                    val uri = Uri.parse(imageUrl)
                    Glide.with(context).load(uri).into(orderFoodImage)
                } else {
                    // Đặt ảnh placeholder nếu không có ảnh
                    // orderFoodImage.setImageResource(R.drawable.placeholder_image)
                }

                // Quyết định văn bản và hành động của nút dựa trên orderItem.orderAccepted
                if (orderItem.orderAccepted) {
                    orderAcceptButton.text = "Gửi"
                } else {
                    orderAcceptButton.text = "Chấp nhận"
                }

                orderAcceptButton.setOnClickListener {
                    val currentPosition = adapterPosition // Luôn dùng adapterPosition trong listener
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        val clickedOrderItem = orderItems[currentPosition] // Lấy lại item mới nhất
                        if (!clickedOrderItem.orderAccepted) {
                            // Nút đang là "Chấp nhận", gọi hành động chấp nhận
                            itemClicked.onItemAcceptClickListener(currentPosition)
                            // Không thay đổi text ở đây, Activity sẽ cập nhật data và notify adapter
                        } else {
                            // Nút đang là "Gửi", gọi hành động gửi đi
                            itemClicked.onItemDispatchClickListener(currentPosition)
                            // Việc xóa item khỏi list và cập nhật UI sẽ do Activity xử lý
                        }
                    }
                }

                itemView.setOnClickListener {
                    val currentPosition = adapterPosition
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        itemClicked.onItemClickListener(currentPosition)
                    }
                }
            }
        }
        // Hàm showToast không còn cần thiết ở đây nếu Activity xử lý Toast
    }
}