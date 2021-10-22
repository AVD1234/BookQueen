package com.bookqueen.bookqueen.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bookqueen.bookqueen.Activity.SaveProfile
import com.bookqueen.bookqueen.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class Myprofile : Fragment() {

    lateinit var name: TextView
    lateinit var email: TextView
    lateinit var college: TextView
    lateinit var editprofile: TextView
    lateinit var auth: FirebaseAuth
    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var progressBar: ProgressBar


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_myprofile, container, false)

        name = view.findViewById(R.id.myname)
        email = view.findViewById(R.id.email)
        college = view.findViewById(R.id.college)
        editprofile = view.findViewById(R.id.editprofile)
        progressBar = view.findViewById(R.id.myprofileprogressbar)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("Users")

        progressBar.visibility = View.GONE
        loadprofile()
        editprofile.setOnClickListener {
            startActivity(Intent(context, SaveProfile::class.java))
        }

        return view
    }

    private fun loadprofile() {
        progressBar.visibility = View.VISIBLE
        databaseReference.child(auth.currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {


                override fun onDataChange(snapshot: DataSnapshot) {
                    val fullname = snapshot.child("Fullname").value.toString()
                    val useremail = snapshot.child("Email").value.toString()
                    val usercollege = snapshot.child("College").value.toString()
                    name.text = fullname
                    email.text = useremail
                    college.text = usercollege
                    progressBar.visibility = View.GONE

                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

}