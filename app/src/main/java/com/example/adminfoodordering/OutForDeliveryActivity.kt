package com.example.adminfoodordering

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminfoodordering.adapter.DeliveryAdapter
import com.example.adminfoodordering.databinding.ActivityOutForDeliveryBinding

class OutForDeliveryActivity : AppCompatActivity() {

    private val binding: ActivityOutForDeliveryBinding by lazy {
        ActivityOutForDeliveryBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        val customerNames = arrayListOf("Customer 1", "Customer 2", "Customer 3")
        val moneyStatus = arrayListOf("Đã nhận đuợc hàng", "Chưa nhận được hàng", "Đang chờ nhận hàng")
        val adapter = DeliveryAdapter(customerNames, moneyStatus)
        binding.deliveryRecyclerView.adapter = adapter
        binding.deliveryRecyclerView.layoutManager = LinearLayoutManager(this)
    }
}