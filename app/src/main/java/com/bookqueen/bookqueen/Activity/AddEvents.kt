package com.bookqueen.bookqueen.Activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bookqueen.bookqueen.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_add_events.*
import java.util.*

class AddEvents : AppCompatActivity() {
    lateinit var datepicker: TextView
    lateinit var eventimage: ImageView
    lateinit var addevent: Button
    lateinit var addeventProgressBar: ProgressBar
    lateinit var auth: FirebaseAuth
    lateinit var firebaseStorage: StorageReference
    lateinit var databaseReference: DatabaseReference
    private var selectedimage: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_events)
        datepicker = findViewById(R.id.eventdate)
        eventimage = findViewById(R.id.addeventimage)
        addevent = findViewById(R.id.btnaddevent)
        addeventProgressBar = findViewById(R.id.addeventprogessbar)


        auth = FirebaseAuth.getInstance()
        firebaseStorage =
            FirebaseStorage.getInstance().getReference("EventImages" + UUID.randomUUID().toString())
        databaseReference = FirebaseDatabase.getInstance().getReference("Events")

        val calender = Calendar.getInstance()
        datepicker.setOnClickListener {
            val day = calender.get(Calendar.DAY_OF_MONTH)
            val month = calender.get(Calendar.MONTH)
            val year = calender.get(Calendar.YEAR)
            val datePickerDialog =
                DatePickerDialog(this@AddEvents, object : DatePickerDialog.OnDateSetListener {
                    @SuppressLint("SetTextI18n")
                    override fun onDateSet(
                        view: DatePicker?,
                        year: Int,
                        month: Int,
                        dayOfMonth: Int
                    ) {
                        val newmonth = month + 1
                        datepicker.text = "$dayOfMonth/$newmonth/$year"
                    }
                }, year, month, day)
            datePickerDialog.show()

        }
        addevent.setOnClickListener {
            when {
                eventimage.drawable.constantState == ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_action_addimage
                )?.constantState -> {
                    Toast.makeText(this, getString(R.string.selectimage), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                datepicker.text == "Select Date.." -> {
                    Toast.makeText(this, getString(R.string.selectdate), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    addeventProgressBar.visibility = View.VISIBLE
                    btnaddevent.visibility = View.GONE
                    addevents()
                }
            }
        }
        loadimage()
    }

    val REQUEST_CODE = 123
    private fun loadimage() {
        eventimage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            selectedimage = data!!.data!!
            eventimage.setImageURI(selectedimage)
        }
    }

    private fun addevents() {
        val uploadTask = firebaseStorage.putFile(selectedimage!!)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            firebaseStorage.downloadUrl
        }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    val currentuserdb = databaseReference.push()
                    val date = datepicker.text.toString()
                    currentuserdb.child("EventImage").setValue(downloadUri.toString())
                    currentuserdb.child("EventDate").setValue(date)
                    currentuserdb.child("EventID").setValue(currentuserdb.key.toString())
                    currentuserdb.child("UserUID").setValue(auth.currentUser!!.uid)
                    addeventProgressBar.visibility = View.GONE
                    btnaddevent.visibility = View.VISIBLE
                    Toast.makeText(this, getString(R.string.eventadded), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.unabletoloadretry), Toast.LENGTH_SHORT).show()
                }
            }
    }
}