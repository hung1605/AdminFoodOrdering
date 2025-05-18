package com.example.adminfoodordering

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.adminfoodordering.databinding.ActivityAdminProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminProfileActivity : AppCompatActivity() {

    private val binding: ActivityAdminProfileBinding by lazy {
        ActivityAdminProfileBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var adminReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        adminReference = database.getReference("admin")

        binding.backButton.setOnClickListener { finish() }

        binding.name.isEnabled = false
        binding.address.isEnabled = false
        binding.email.isEnabled = false
        binding.phone.isEnabled = false
        binding.password.isEnabled = false
        binding.saveButton.isEnabled = false

        var isEnable = false

        binding.saveButton.setOnClickListener {
            updateUserData()
            isEnable = !isEnable
            binding.name.isEnabled = isEnable
            binding.address.isEnabled = isEnable
            binding.email.isEnabled = isEnable
            binding.phone.isEnabled = isEnable
            binding.password.isEnabled = isEnable
        }


        binding.editButton.setOnClickListener {
            isEnable = !isEnable
            binding.name.isEnabled = isEnable
            binding.address.isEnabled = isEnable
            binding.email.isEnabled = isEnable
            binding.phone.isEnabled = isEnable
            binding.password.isEnabled = isEnable

            if (isEnable) {
                binding.name.requestFocus()
            }
            binding.saveButton.isEnabled = isEnable
        }

        retrieveAdminData()
    }

    private fun updateUserData() {
        val updateName = binding.name.text.toString()
        val updateAddress = binding.address.text.toString()
        val updateEmail = binding.email.text.toString()
        val updatePhone = binding.phone.text.toString()
        val updatePassword = binding.password.text.toString()

        val currentUserUid = auth.currentUser?.uid

        if (currentUserUid != null) {
            val userReference = database.getReference("admin/$currentUserUid")
            userReference.child("name").setValue(updateName)
            userReference.child("address").setValue(updateAddress)
            userReference.child("email").setValue(updateEmail)
            userReference.child("phone").setValue(updatePhone)
            userReference.child("password").setValue(updatePassword)

            auth.currentUser?.updateEmail(updateEmail)
            auth.currentUser?.updatePassword(updatePassword)
        }
        Toast.makeText(this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show()
    }

    private fun retrieveAdminData() {
        val currentUserUid = auth.currentUser?.uid

        if (currentUserUid != null) {
            val adminReference = database.getReference("admin/$currentUserUid")
            adminReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val ownerName = snapshot.child("name").value
                        val ownerEmail = snapshot.child("email").value
                        val ownerPassword = snapshot.child("password").value
                        val ownerAddress = snapshot.child("address").value
                        val ownerPhone = snapshot.child("phone").value
                        setDataToView(ownerName, ownerEmail, ownerPassword, ownerAddress, ownerPhone)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    private fun setDataToView(
        ownerName: Any?,
        ownerEmail: Any?,
        ownerPassword: Any?,
        ownerAddress: Any?,
        ownerPhone: Any?
    ) {
        binding.name.setText(ownerName.toString())
        binding.email.setText(ownerEmail.toString())
        binding.password.setText(ownerPassword.toString())
        binding.address.setText(ownerAddress.toString())
        binding.phone.setText(ownerPhone.toString())
    }
}