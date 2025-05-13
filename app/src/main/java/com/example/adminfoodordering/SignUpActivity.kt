package com.example.adminfoodordering

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.adminfoodordering.databinding.ActivitySignUpBinding
import com.example.adminfoodordering.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

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
        database = Firebase.database.reference


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
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Tạo tài khoản thất bại", Toast.LENGTH_SHORT).show()
                Log.d("Account", "tao tai khoan that bai", task.exception)
            }
        }
    }

    private fun saveUserData() {
        userName = binding.editTextName.text.toString().trim()
        nameOfRestaurant = binding.editTextNameRestaurant.text.toString().trim()
        email = binding.editTextEmailAddress.text.toString().trim()
        password = binding.editTextPassword.text.toString().trim()
        val user = UserModel(userName, nameOfRestaurant, email, password)
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        database.child("user").child(userId).setValue(user)
    }
}