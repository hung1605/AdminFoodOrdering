package com.example.adminfoodordering

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.adminfoodordering.databinding.ActivityAddItemBinding
import com.example.adminfoodordering.model.AllMenu
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AddItemActivity : AppCompatActivity() {

    private lateinit var foodName: String
    private lateinit var foodPrice: String
    private lateinit var foodDescription: String
    private lateinit var foodIngredient: String
    private var foodImageUri: Uri? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase


    private val binding: ActivityAddItemBinding by lazy {
        ActivityAddItemBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.addItemButton.setOnClickListener {
            foodName = binding.enterFoodName.text.toString().trim()
            foodPrice = binding.enterFoodPrice.text.toString().trim() + " VNĐ"
            foodDescription = binding.description.text.toString().trim()
            foodIngredient = binding.ingredient.text.toString().trim()

            if (foodName.isNotBlank() and foodPrice.isNotBlank() and foodDescription.isNotBlank() and foodDescription.isNotBlank()) {
                uploadData()
                Toast.makeText(this, "Thêm món thành oông", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Hãy điền đầy đủ các trường", Toast.LENGTH_SHORT).show()
            }
        }
        binding.selectImage.setOnClickListener {
            pickImage.launch("image/*")
        }
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun uploadData() {
        val menuRef = database.getReference("menu")
        val newItemKey = menuRef.push().key
        if (foodImageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("menu_images/${newItemKey}.jpg")
            val uploadTask = imageRef.putFile(foodImageUri!!)

            uploadTask.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val newItem = AllMenu(
                        newItemKey,
                        foodName = foodName,
                        foodPrice = foodPrice,
                        foodDescription = foodDescription,
                        foodImage = downloadUrl.toString(),
                        foodIngredient = foodIngredient
                    )
                    newItemKey?.let { key ->
                        menuRef.child(key).setValue(newItem).addOnSuccessListener {
                            Toast.makeText(this, "Them thanh cong", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Them that bai", Toast.LENGTH_SHORT).show()
                        }

                    }
                }
            }
        } else {
            Toast.makeText(this, "Chua chon anh", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            Log.d("anhtrendienthoai", "dfajlksjflsa" + uri.toString())
            binding.selectedImage.setImageURI(uri)
            foodImageUri = uri
        }
    }
}