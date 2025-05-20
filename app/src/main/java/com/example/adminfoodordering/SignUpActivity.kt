package com.example.adminfoodordering

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.adminfoodordering.databinding.ActivitySignUpBinding
import com.example.adminfoodordering.model.UserModel
import com.example.foodordering.adminapp.Service.MyAdminFirebaseMessagingService
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.google.firebase.messaging.FirebaseMessaging

class SignUpActivity : AppCompatActivity() {

    private lateinit var userName: String
    private lateinit var nameOfRestaurant: String
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private val binding: ActivitySignUpBinding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        database = Firebase
            .database("https://foodordering-b4531-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .reference


        val locationList = arrayListOf("Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "Cần Thơ", "Hải Phòng")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, locationList)
        val autoCompleteTextView = binding.listOfLocation
        autoCompleteTextView.setAdapter(adapter)

        binding.signUpButton.setOnClickListener {

            userName = binding.editTextName.text.toString().trim()
            nameOfRestaurant = binding.editTextNameRestaurant.text.toString().trim()
            email = binding.editTextEmailAddress.text.toString().trim()
            password = binding.editTextPassword.text.toString().trim()

            if (userName.isBlank() or
                nameOfRestaurant.isBlank() or
                email.isBlank() or
                password.isBlank()
            ) {
                Toast.makeText(this, "Hãy điền đầy đủ các trường", Toast.LENGTH_SHORT).show()
            } else {
                createAccount(email, password)
            }
        }
        binding.login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Tài khoản đã được tạo thành công", Toast.LENGTH_SHORT).show()
                saveUserData()
            } else {
                Toast.makeText(this, "Tạo tài khoản thất bại", Toast.LENGTH_SHORT).show()
                Log.d("Account", "tao tai khoan that bai", task.exception)
            }
        }
    }

    private fun saveUserData() {
        Log.d("Account", "save user duoc goi")
        userName = binding.editTextName.text.toString().trim()
        nameOfRestaurant = binding.editTextNameRestaurant.text.toString().trim()
        email = binding.editTextEmailAddress.text.toString().trim()
        password = binding.editTextPassword.text.toString().trim()
        val user = UserModel(userName, nameOfRestaurant, email, password)
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        Log.d("Account", "lay id thanh cong")
        database.child("admin").child(userId).setValue(user)
            .addOnSuccessListener {
                Log.d("Account", "Lưu dữ liệu người dùng thành công!")
                Toast.makeText(this, "Lưu thông tin thành công!", Toast.LENGTH_LONG).show()
                onAdminLoginSuccess()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Log.e("Account", "Lỗi khi lưu dữ liệu người dùng: ${e.message}", e)
                Toast.makeText(this, "Lỗi khi lưu dữ liệu: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun onAdminLoginSuccess() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("AdminLogin", "Fetching FCM token failed for admin", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("AdminLogin", "Current FCM Token for admin: $token")
            MyAdminFirebaseMessagingService.sendAdminFCMTokenToDatabase(token)
        }
    }
}