package com.bookqueen.bookqueen.Activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bookqueen.bookqueen.R
import com.bookqueen.bookqueen.fragments.Contactus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class SaveProfile : AppCompatActivity() {
    lateinit var fullname: EditText
    lateinit var email: EditText
    lateinit var saveProfile: Button
    lateinit var auth: FirebaseAuth
    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var spinner_list_college: Spinner
    lateinit var addcollegemessage: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_profile)

        fullname = findViewById(R.id.sname)
        email = findViewById(R.id.semail)
        saveProfile = findViewById(R.id.btnsaveprofile)
        addcollegemessage = findViewById(R.id.erroraddcollege)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("Users")


        spinner_list_college = findViewById(R.id.spinner_list_college)
//        val colleges = resources.getStringArray(R.array.Colleges)

        database.getReference("Colleges")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val colleges: MutableList<String?> = ArrayList()
                    for (areaSnapshot in dataSnapshot.children) {
                        val consultaName = areaSnapshot.getValue(String::class.java)
                        colleges.add(consultaName)
                    }

                    val adapter = ArrayAdapter(
                        this@SaveProfile,
                        android.R.layout.simple_spinner_item,
                        colleges
                    )

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner_list_college.adapter = adapter
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        //var selectedText=colleges.first()
        var text: String? = null

        spinner_list_college.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                text = spinner_list_college.getItemAtPosition(position).toString()
                //selectedText = colleges[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }



        saveProfile.setOnClickListener {
            when {
                TextUtils.isEmpty(fullname.text.toString()) -> {
                    fullname.error = getString(R.string.entername)
                    return@setOnClickListener
                }
                TextUtils.isEmpty(email.text.toString()) -> {
                    email.error = getString(R.string.enteremail)
                    return@setOnClickListener
                }
                else -> {
                    saveProfile.isClickable = false
                    saveuserprofile(
                        fullname.text.toString(),
                        email.text.toString(),
                        text.toString()
                    )
                }
            }
        }
        addcollegemessage.setOnClickListener {
            val intent = Intent(this@SaveProfile, Contactus::class.java)
            startActivity(intent)
        }
    }

    private fun saveuserprofile(name: String, email: String, college: String) {
        databaseReference.child(auth.currentUser!!.uid).child("Fullname").setValue(name)
        databaseReference.child(auth.currentUser!!.uid).child("Email").setValue(email)
        databaseReference.child(auth.currentUser!!.uid).child("College").setValue(college)
        databaseReference.child(auth.currentUser!!.uid).child("Phone")
            .setValue(auth.currentUser!!.phoneNumber)
        Toast.makeText(this, getString(R.string.profilesave), Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()

    }

}