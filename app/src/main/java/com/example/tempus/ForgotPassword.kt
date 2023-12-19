package com.example.tempus

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ForgotPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
    }
    fun resetPassword()
    { val username = findViewById<EditText>(R.id.forgot_username)
        val name = username?.text.toString().replace("\\s".toRegex(), "")
        val ref = FirebaseDatabase.getInstance().getReference("UserDetails")
        val query = ref.orderByChild("displayname").equalTo(name)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val field1 = dataSnapshot.child("field1").getValue(String::class.java)
                if(field1 == name)
                {

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error here
            }
        })

    }
}