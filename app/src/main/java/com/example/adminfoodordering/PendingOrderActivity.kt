package com.example.adminfoodordering

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminfoodordering.adapter.PendingOrderAdapter
import com.example.adminfoodordering.databinding.ActivityPendingOrderBinding

class PendingOrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPendingOrderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        val customerNames = arrayListOf("Customer 1", "Customer 2", "Customer 3")
        val quantity = arrayListOf("8", "5", "6")
        val foodImage = arrayListOf(R.drawable.menu1, R.drawable.menu2, R.drawable.menu3)
        val adapter = PendingOrderAdapter(customerNames, quantity, foodImage, this)
        binding.pendingOrderRecyclerView.adapter = adapter
        binding.pendingOrderRecyclerView.layoutManager = LinearLayoutManager(this)
    }
}