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
import java.text.SimpleDateFormat
import java.util.*


class Addbooksets : AppCompatActivity() {
    lateinit var spinner_list_setdept: Spinner
    lateinit var spinner_list_setsem: Spinner
    lateinit var spinner_list_booksetyear: Spinner
    lateinit var auth: FirebaseAuth
    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var firebasestorage: StorageReference
    lateinit var imgaddbookset: ImageView
    private var selectedimage: Uri? = null
    lateinit var progressBarset: ProgressBar
    lateinit var addbooksets: Button
    lateinit var bookname1: EditText
    lateinit var bookname2: EditText
    lateinit var bookname3: EditText
    lateinit var bookname4: EditText
    lateinit var bookname5: EditText
    lateinit var book1publication: EditText
    lateinit var book2publication: EditText
    lateinit var book3publication: EditText
    lateinit var book4publication: EditText
    lateinit var book5publication: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addbooksets)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("BookSets")
        firebasestorage =
            FirebaseStorage.getInstance().getReference("images" + UUID.randomUUID().toString())
        imgaddbookset = findViewById(R.id.imgaddbookset)
        progressBarset = findViewById(R.id.addbookprogress)
        progressBarset.visibility = View.GONE
        addbooksets = findViewById(R.id.btnaddbookset)

        bookname1 = findViewById(R.id.setedtbookname1)
        bookname2 = findViewById(R.id.setedtbookname2)
        bookname3 = findViewById(R.id.setedtbookname3)
        bookname4 = findViewById(R.id.setedtbookname4)
        bookname5 = findViewById(R.id.setedtbookname5)
        book1publication = findViewById(R.id.setedtbookpublication1)
        book2publication = findViewById(R.id.setedtbookpublication2)
        book3publication = findViewById(R.id.setedtbookpublication3)
        book4publication = findViewById(R.id.setedtbookpublication4)
        book5publication = findViewById(R.id.setedtbookpublication5)

        loadimage()
        spinner_list_setdept = findViewById(R.id.spinner_list_setdept)
        val dept = resources.getStringArray(R.array.departments)
        var selectedsetdept = dept.first()
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            dept
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_list_setdept.adapter = adapter

        spinner_list_setdept.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedsetdept = dept[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        spinner_list_booksetyear = findViewById(R.id.spinner_list_setyear)
        val year = resources.getStringArray(R.array.BookYear)
        var selectedsetyear = year.first()
        var booksetadapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, year)
        booksetadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_list_booksetyear.adapter = booksetadapter

        spinner_list_booksetyear.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedsetyear = year[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }
        spinner_list_setsem = findViewById(R.id.spinner_list_setsem)
        val semister = resources.getStringArray(R.array.Semister)
        var selectedsem = semister.first()
        var semadapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, semister)
        semadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_list_setsem.adapter = semadapter

        spinner_list_setsem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedsem = semister[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }


        addbooksets.setOnClickListener {
            when {
                imgaddbookset.drawable.constantState == ContextCompat.getDrawable(this,R.drawable.ic_action_addimage)?.constantState -> {
                    Toast.makeText(this, getString(R.string.selectimage), Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                TextUtils.isEmpty(bookname1.text.toString()) && TextUtils.isEmpty(book1publication.text.toString()) -> {
                    bookname1.error = getString(R.string.enterbooknameerror)
                    book1publication.error = getString(R.string.enterbookpublicerror)
                    return@setOnClickListener
                }
                TextUtils.isEmpty(bookname2.text.toString()) && TextUtils.isEmpty(
                    book2publication.text.toString()
                ) -> {
                    bookname2.error =getString(R.string.enterbooknameerror)
                    book2publication.error = getString(R.string.enterbookpublicerror)
                    return@setOnClickListener
                }
                TextUtils.isEmpty(bookname3.text.toString()) && TextUtils.isEmpty(
                    book3publication.text.toString()
                ) -> {
                    bookname3.error = getString(R.string.enterbooknameerror)
                    book3publication.error = getString(R.string.enterbookpublicerror)
                    return@setOnClickListener
                }
                bookname4.text.toString()
                    .isNotEmpty() && TextUtils.isEmpty(book4publication.text.toString()) -> {
                    book4publication.error = getString(R.string.enterbookpublicerror)
                    return@setOnClickListener
                }
                book4publication.text.toString()
                    .isNotEmpty() && TextUtils.isEmpty(bookname4.text.toString()) -> {
                    bookname4.error =getString(R.string.enterbooknameerror)
                    return@setOnClickListener
                }
                bookname5.text.toString()
                    .isNotEmpty() && TextUtils.isEmpty(book5publication.text.toString()) -> {
                    book5publication.error = getString(R.string.enterbookpublicerror)
                    return@setOnClickListener
                }
                book5publication.text.toString()
                    .isNotEmpty() && TextUtils.isEmpty(bookname5.text.toString()) -> {
                    bookname5.error = getString(R.string.enterbooknameerror)
                    return@setOnClickListener
                }
                else -> {
                    progressBarset.visibility = View.VISIBLE
                    addbooksets.visibility = View.GONE
                    addbookset(
                        bookname1.text.toString(), book1publication.text.toString(),
                        bookname2.text.toString(), book2publication.text.toString(),
                        bookname3.text.toString(), book3publication.text.toString(),
                        bookname4.text.toString(), book4publication.text.toString(),
                        bookname5.text.toString(), book5publication.text.toString(),
                        selectedsetdept, selectedsetyear, selectedsem
                    )
                }
            }
        }
    }

    private val REQIEST_CODE = 123
    private fun loadimage() {
        imgaddbookset.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQIEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQIEST_CODE) {
            selectedimage = data!!.data!!
            imgaddbookset.setImageURI(selectedimage)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun addbookset(
        book1: String,
        publication1: String,
        book2: String,
        publication2: String,
        book3: String,
        publication3: String,
        book4: String,
        publication4: String,
        book5: String,
        publication5: String, selectedtext: String,
        selectedyear: String,
        selectedsem: String
    ) {

        val uploadimage = firebasestorage.putFile(selectedimage!!)
        uploadimage.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            firebasestorage.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val currentuserdb = databaseReference.push()
                val sdf1 = SimpleDateFormat("dd/MM/yyyy")
                val calendar = Calendar.getInstance()
                val formatdate = sdf1.format(calendar.time)
                currentuserdb.child("Date").setValue(formatdate.toString())
                currentuserdb.child("Book1").setValue("$book1($publication1)")
                currentuserdb.child("Book2").setValue("$book2($publication2)")
                currentuserdb.child("Book3").setValue("$book3($publication3)")
                if (book4 != "") {
                    currentuserdb.child("Book4").setValue("$book4($publication4)")
                } else {
                    currentuserdb.child("Book4").setValue("-")
                }
                if (book5 != "") {
                    currentuserdb.child("Book5").setValue("$book5($publication5)")
                } else {
                    currentuserdb.child("Book5").setValue("-")
                }
                currentuserdb.child("BooksetID").setValue(currentuserdb.key)
                currentuserdb.child("Department").setValue(selectedtext)
                currentuserdb.child("BookYear").setValue(selectedyear)
                currentuserdb.child("Semister").setValue(selectedsem)
                currentuserdb.child("UserUID").setValue(auth.currentUser!!.uid)
                currentuserdb.child("BookImage").setValue(downloadUri.toString())

                progressBarset.visibility = View.GONE
                addbooksets.visibility = View.VISIBLE
                Toast.makeText(this, getString(R.string.booksetadded), Toast.LENGTH_LONG).show()
                imgaddbookset.setImageDrawable(null)
                bookname1.setText("")
                bookname2.setText("")
                bookname3.setText("")
                bookname4.setText("")
                bookname5.setText("")
                book1publication.setText("")
                book2publication.setText("")
                book3publication.setText("")
                book4publication.setText("")
                book5publication.setText("")

            } else {
                Toast.makeText(this, getString(R.string.unabletoloadretry), Toast.LENGTH_SHORT).show()
                progressBarset.visibility = View.GONE
                addbooksets.visibility = View.VISIBLE
            }

        }
    }
}