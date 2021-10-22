package com.bookqueen.bookqueen.constants

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object Mycollege {

    fun college(mycallback: Mycallback){
        val databaseReference=FirebaseDatabase.getInstance()
        val auth=FirebaseAuth.getInstance()
    databaseReference.getReference("Users").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(object :ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()){
                val college=snapshot.child("College").value.toString()
                mycallback.onCallback(college)
            }
        }

        override fun onCancelled(error: DatabaseError) {

        }
    })
    }

     interface Mycallback{
        fun onCallback(value:String)
    }
}