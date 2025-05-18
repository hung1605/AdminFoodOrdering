package com.example.adminfoodordering

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminfoodordering.adapter.MenuItemAdapter
import com.example.adminfoodordering.databinding.ActivityAllItemBinding
import com.example.adminfoodordering.model.AllMenu
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class AllItemActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private var menuItems: ArrayList<AllMenu> = ArrayList()

    private val binding: ActivityAllItemBinding by lazy {
        ActivityAllItemBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        databaseReference = FirebaseDatabase.getInstance().reference
        retrieveMenuItem()

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun retrieveMenuItem() {
        database = FirebaseDatabase.getInstance()
        val foodRef: DatabaseReference = database.reference.child("menu")

        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                menuItems.clear()

                for (foodSnapshot in snapshot.children) {
                    val menuItemData = foodSnapshot.getValue(AllMenu::class.java)
                    if (menuItemData != null) {
                        menuItemData.key = foodSnapshot.key
                    }
                    menuItemData?.let {
                        menuItems.add(it)
                        Log.d("menuItem", "menuItem: " + menuItemData.toString())
                    }
                }
                setAdapter()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError", "Error: ${error.message}")
            }
        })
    }

    private fun setAdapter() {
        val adapter =
            MenuItemAdapter(this@AllItemActivity, menuItems, database) { position ->
                deleteMenuItems(position)
            }
        binding.menuRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.menuRecyclerView.adapter = adapter
    }

    private fun deleteMenuItems(position: Int) {

        val menuItemToDelete = menuItems[position]
        val menuItemKey = menuItemToDelete.key
        val foodImageUrl = menuItemToDelete.foodImage

        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(foodImageUrl.toString())
        Log.d("xoaMenuItem", "Đang thử xóa ảnh từ URL Storage: $foodImageUrl")
        storageRef.delete().addOnSuccessListener {
            Log.d("xoaMenuItem", "Xóa ảnh thành công từ URL Storage: $foodImageUrl")
            // Tiếp tục xóa dữ liệu trên Realtime Database
            deleteItemFromRealtimeDatabase(
                menuItemKey.toString(),
                position,
                menuItemToDelete.foodName ?: "Unknown Item"
            )
        }.addOnFailureListener { exception ->
            Log.e("xoaMenuItem", "Xóa ảnh thất bại từ URL Storage: $foodImageUrl", exception)
            Toast.makeText(this, "Lỗi xóa ảnh: ${exception.message}", Toast.LENGTH_LONG).show()
            // Quyết định: Vẫn xóa item khỏi DB ngay cả khi xóa ảnh thất bại
            deleteItemFromRealtimeDatabase(
                menuItemKey.toString(),
                position,
                menuItemToDelete.foodName ?: "Unknown Item"
            )
        }

    }

    // Hàm deleteItemFromRealtimeDatabase giữ nguyên như trước
    private fun deleteItemFromRealtimeDatabase(itemKey: String, position: Int, itemName: String) {
        val foodMenuReference = database.reference.child("menu").child(itemKey)

        Log.d("xoaMenuItem", "Đang gọi hàm xóa Realtime Database cho key: $itemKey")
        foodMenuReference.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("xoaMenuItem", "Xóa thành công trên Realtime Database cho key: $itemKey")
                if (position < menuItems.size && menuItems[position].key == itemKey) {
                    menuItems.removeAt(position)
                    binding.menuRecyclerView.adapter?.notifyItemRemoved(position)
                    binding.menuRecyclerView.adapter?.notifyItemRangeChanged(
                        position,
                        menuItems.size - position
                    )
                    Toast.makeText(this, "Đã xóa '${itemName}'", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w(
                        "xoaMenuItem",
                        "Mục tại vị trí $position đã thay đổi hoặc không còn tồn tại trong danh sách local."
                    )
                }
            } else {
                Log.e(
                    "xoaMenuItem",
                    "Xóa thất bại trên Realtime Database cho key: $itemKey",
                    task.exception
                )
                Toast.makeText(
                    this,
                    "Xóa '${itemName}' thất bại trên Database: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}