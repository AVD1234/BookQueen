package com.bookqueen.bookqueen.Activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bookqueen.bookqueen.R
import com.bookqueen.bookqueen.constants.Mycollege
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*

class Addtools : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var firebaseStorage: StorageReference
    lateinit var btnaddtools: Button
    lateinit var progressBartool: ProgressBar
    lateinit var imgaddtool: ImageView
    var selectedimage: Uri? = null
    lateinit var edttoolname: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addtools)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        Mycollege.college(object : Mycollege.Mycallback {
            override fun onCallback(value: String) {
                Log.d("mycollege", value)
                databaseReference = database.getReference("Tools").child(value)
            }
        })
        databaseReference = database.getReference("Tools")
        firebaseStorage =
            FirebaseStorage.getInstance().getReference("images" + UUID.randomUUID().toString())
        imgaddtool = findViewById(R.id.imgaddtool)
        progressBartool = findViewById(R.id.addbookprogresstool)
        progressBartool.visibility = View.GONE
        btnaddtools = findViewById(R.id.btnadtool)
        edttoolname = findViewById(R.id.edttoolname)

        loadimage()

        btnaddtools.setOnClickListener {
            when {
                imgaddtool.drawable.constantState == ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_action_addimage
                )?.constantState -> {
                    Toast.makeText(this, getString(R.string.selectimage), Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                TextUtils.isEmpty(edttoolname.text.toString()) -> {
                    edttoolname.error = "Enter Toolname"
                    return@setOnClickListener
                }
                else -> {
                    progressBartool.visibility = View.VISIBLE
                    btnaddtools.visibility = View.GONE
                    addtools(edttoolname.text.toString())
                }
            }
        }

    }


    private val REQIEST_CODE = 123
    private fun loadimage() {
        imgaddtool.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQIEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQIEST_CODE) {
            selectedimage = data!!.data!!
            imgaddtool.setImageURI(selectedimage)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun addtools(toolname: String) {
        val uplaodimage = firebaseStorage.putFile(selectedimage!!)
        uplaodimage.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            firebaseStorage.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val currentuserdb = databaseReference.push()
                val sdf1 = SimpleDateFormat("dd/MM/yyyy")
                val calendar = Calendar.getInstance()
                val formatdate = sdf1.format(calendar.time)
                currentuserdb.child("Date").setValue(formatdate.toString())
                currentuserdb.child("ToolID").setValue(currentuserdb.key)
                currentuserdb.child("Toolname").setValue(toolname)
                currentuserdb.child("UserUID").setValue(auth.currentUser!!.uid)
                currentuserdb.child("ToolImage").setValue(downloadUri.toString())

                progressBartool.visibility = View.GONE
                btnaddtools.visibility = View.VISIBLE
                Toast.makeText(this, getString(R.string.tooladded), Toast.LENGTH_SHORT).show()
                edttoolname.setText("")
                imgaddtool.setImageResource(R.drawable.ic_action_addimage)

            } else {
                Toast.makeText(this, getString(R.string.unabletoloadretry), Toast.LENGTH_LONG)
                    .show()
                progressBartool.visibility = View.GONE
                btnaddtools.visibility = View.VISIBLE
            }
        }
    }
}