package com.bookqueen.bookqueen.Activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
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
import kotlinx.android.synthetic.main.activity_addbooks.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Addbooks : AppCompatActivity() {
    lateinit var spinner_list_dept: Spinner
    lateinit var spinner_list_bookyear: Spinner
    lateinit var auth: FirebaseAuth
    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var firebaseStorage: StorageReference
    lateinit var btnaddbooks: Button
    lateinit var edtbookname: EditText
    lateinit var edtbookpublication: EditText
    lateinit var progressBar: ProgressBar
    lateinit var imgaddbook: ImageView
    var selectedimage: Uri? = null

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addbooks)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("Books")
        firebaseStorage =
            FirebaseStorage.getInstance().getReference("images" + UUID.randomUUID().toString())

        btnaddbooks = findViewById(R.id.btnaddbook)
        edtbookname = findViewById(R.id.edtbookname)
        edtbookpublication = findViewById(R.id.edtbookpublication)
        imgaddbook = findViewById(R.id.imgaddbook)
        progressBar = findViewById(R.id.addbookprogress)
        progressBar.visibility = View.GONE

        spinner_list_dept = findViewById(R.id.spinner_list_dept)
        val dept = resources.getStringArray(R.array.departments)
        var selecteddept = dept.first()
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            dept
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_list_dept.adapter = adapter

        spinner_list_dept.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                selecteddept = dept[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        spinner_list_bookyear = findViewById(R.id.spinner_list_year)
        val year = resources.getStringArray(R.array.BookYear)
        var selectedyear = dept.first()
        val bookadapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            year
        )
        bookadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_list_year.adapter = bookadapter

        spinner_list_year.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedyear = year[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        loadimage()


        btnaddbooks.setOnClickListener {
            when {
                imgaddbook.drawable.constantState == ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_action_addimage
                )?.constantState -> {
                    Toast.makeText(this, getString(R.string.selectimage), Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                TextUtils.isEmpty(edtbookname.text.toString()) -> {
                    edtbookname.error = getString(R.string.enterbooknameerror)
                    return@setOnClickListener
                }
                TextUtils.isEmpty(edtbookpublication.text.toString()) -> {
                    edtbookpublication.error = getString(R.string.enterbookpublicerror)
                    return@setOnClickListener
                }
                else -> {
                    progressBar.visibility = View.VISIBLE
                    btnaddbooks.visibility = View.GONE
                    addbooks(
                        edtbookname.text.toString(),
                        edtbookpublication.text.toString(),
                        selecteddept.toString(), selectedyear.toString()
                    )
                }
            }
        }
    }


    private val REQIEST_CODE = 123
    private fun loadimage() {
        imgaddbook.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQIEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQIEST_CODE) {
            selectedimage = data!!.data!!
            imgaddbook.setImageURI(selectedimage)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun addbooks(
        bookname: String,
        bookpublication: String,
        selectedtext: String,
        selectedyear: String
    ) {
        val currentuserdb = databaseReference.push()
        val uploadimage = firebaseStorage.putFile(selectedimage!!)
        uploadimage.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            firebaseStorage.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val sdf1 = SimpleDateFormat("dd/MM/yyyy")
                val calendar = Calendar.getInstance()
                val formatdate = sdf1.format(calendar.time)
                currentuserdb.child("Date").setValue(formatdate.toString())
                currentuserdb.child("BookId").setValue(currentuserdb.key)
                currentuserdb.child("BookImage").setValue(downloadUri.toString())
                currentuserdb.child("BookName").setValue(bookname)
                currentuserdb.child("BookPublication").setValue(bookpublication)
                currentuserdb.child("Department").setValue(selectedtext)
                currentuserdb.child("BookYear").setValue(selectedyear)
                currentuserdb.child("UserUID").setValue(auth.currentUser!!.uid)



                progressBar.visibility = View.GONE
                btnaddbooks.visibility = View.VISIBLE
                Toast.makeText(this, getString(R.string.toastbookadded), Toast.LENGTH_LONG).show()
                edtbookname.setText("")
                edtbookpublication.setText("")
                imgaddbook.setImageDrawable(null)

            } else {
                Toast.makeText(this, getString(R.string.unabletoloadretry), Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
                btnaddbooks.visibility = View.VISIBLE
            }
        }

    }
}