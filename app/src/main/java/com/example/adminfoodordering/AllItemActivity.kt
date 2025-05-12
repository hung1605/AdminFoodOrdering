package com.example.adminfoodordering

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuAdapter
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminfoodordering.adapter.AddItemAdapter
import com.example.adminfoodordering.databinding.ActivityAllItemBinding

class AllItemActivity : AppCompatActivity() {
    private val binding : ActivityAllItemBinding by lazy { ActivityAllItemBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val menuFoodName = listOf(
            "Pizza",
            "Burger",
            "Salad",
            "Pizza",
            "Burger",
            "Salad",
            "Pizza",
            "Burger",
            "Salad",
            "Pizza",
            "Burger",
            "Salad"
        )
        val menuItemPrice = listOf(
            "10.000đ",
            "10.000đ",
            "15.000đ",
            "10.000đ",
            "10.000đ",
            "15.000đ",
            "10.000đ",
            "10.000đ",
            "15.000đ",
            "10.000đ",
            "10.000đ",
            "15.000đ"
        )
        val menuImage = listOf(
            R.drawable.menu1,
            R.drawable.menu2,
            R.drawable.menu3,
            R.drawable.menu1,
            R.drawable.menu2,
            R.drawable.menu3,
            R.drawable.menu1,
            R.drawable.menu2,
            R.drawable.menu3,
            R.drawable.menu1,
            R.drawable.menu2,
            R.drawable.menu3
        )

        val adapter = AddItemAdapter(
            ArrayList(menuFoodName),
            ArrayList(menuItemPrice),
            ArrayList(menuImage)
        )
        binding.menuRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.menuRecyclerView.adapter = adapter

        binding.backButton.setOnClickListener {
            finish()
        }
    }
}