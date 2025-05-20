package com.example.adminfoodordering.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat // Để dùng màu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminfoodordering.R // Đảm bảo R của admin app
import com.example.adminfoodordering.databinding.PendingOrderItemBinding
import com.example.foodordering.Model.OrderDetails

class PendingOrderAdapter(
    private val context: Context,
    private val orderItems: List<OrderDetails>,
    private val itemClicked: OnItemClicked
) : RecyclerView.Adapter<PendingOrderAdapter.PendingOrderViewHolder>() {

    interface OnItemClicked {
        fun onItemAcceptClickListener(position: Int)
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
        holder.bind(currentOrderItem, position)
    }

    override fun getItemCount(): Int = orderItems.size

    inner class PendingOrderViewHolder(private val binding: PendingOrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(orderItem: OrderDetails, position: Int) {
            binding.apply {
                customerName.text = orderItem.userName ?: "N/A"
                pendingOrderQuantity.text = orderItem.totalPrice ?: "0đ" // Giả sử đây là tổng tiền

                val imageUrl = orderItem.foodImages?.firstOrNull { it.isNotEmpty() }
                if (imageUrl != null) {
                    Glide.with(context).load(Uri.parse(imageUrl)).into(orderFoodImage)
                } else {
                    // orderFoodImage.setImageResource(R.drawable.default_image) // Ảnh mặc định
                }

                // Quyết định văn bản và trạng thái của nút
                if (!orderItem.orderAccepted) {
                    orderAcceptButton.text = "Chấp nhận"
                    orderAcceptButton.isEnabled = true
                    // orderAcceptButton.setBackgroundColor(ContextCompat.getColor(context, R.color.green_color)) // Ví dụ màu
                } else if (orderItem.orderAccepted && !orderItem.orderDispatched) {
                    orderAcceptButton.text = "Gửi"
                    orderAcceptButton.isEnabled = true
                    // orderAcceptButton.setBackgroundColor(ContextCompat.getColor(context, R.color.blue_color)) // Ví dụ màu
                } else { // orderAccepted && orderDispatched
                    orderAcceptButton.text = "Đã gửi"
                    orderAcceptButton.isEnabled = false // Vô hiệu hóa sau khi đã gửi
                    // orderAcceptButton.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_color)) // Ví dụ màu
                }

                orderAcceptButton.setOnClickListener {
                    val currentPosition = adapterPosition
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        val clickedOrderItem = orderItems[currentPosition]
                        if (!clickedOrderItem.orderAccepted) {
                            itemClicked.onItemAcceptClickListener(currentPosition)
                        } else if (clickedOrderItem.orderAccepted && !clickedOrderItem.orderDispatched) {
                            itemClicked.onItemDispatchClickListener(currentPosition)
                        }
                        // Không làm gì nếu đã gửi
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
    }
}
