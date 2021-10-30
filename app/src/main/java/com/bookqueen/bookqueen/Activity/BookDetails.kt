package com.bookqueen.bookqueen.Activity

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bookqueen.bookqueen.R
import com.bookqueen.bookqueen.constants.Mycollege
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class BookDetails : AppCompatActivity() {
    lateinit var bookname: TextView
    lateinit var bookpubl: TextView
    lateinit var bookdept: TextView
    lateinit var bookyear: TextView
    lateinit var oname: TextView
    lateinit var ocontact: TextView
    lateinit var errormsg: TextView
    lateinit var btnsold: Button
    lateinit var bookimage: ImageView
    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var databaseReference1: DatabaseReference
    lateinit var firebaseStorage: StorageReference
    lateinit var auth: FirebaseAuth
    lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_details)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("Users")
        //databaseReference1 = database.getReference("Books")
        Mycollege.college(object : Mycollege.Mycallback {
            override fun onCallback(value: String) {
                Log.d("mycollege", value)
                databaseReference1 = database.getReference("Books").child(value)
            }
        })

        bookname = findViewById(R.id.detbookname)
        bookpubl = findViewById(R.id.detbookpublication)
        bookdept = findViewById(R.id.department)
        bookyear = findViewById(R.id.bookyear)
        bookimage = findViewById(R.id.imgdetbook)
        oname = findViewById(R.id.contactname)
        ocontact = findViewById(R.id.contactphone)
        errormsg = findViewById(R.id.erormsg)
        btnsold = findViewById(R.id.btnsold)
        progressBar = findViewById(R.id.bookdetprogress)


        val bookid = intent.getStringExtra("Bookid")
        val name = intent.getStringExtra("Bookname")
        val publ = intent.getStringExtra("Bookpubl")
        val image = intent.getStringExtra("BookImage")
        val dept = intent.getStringExtra("Bookdept")
        val year = intent.getStringExtra("Bookyear")
        val useruid = intent.getStringExtra("UserUID").toString()


        firebaseStorage =
            FirebaseStorage.getInstance().getReferenceFromUrl(image!!)

        Picasso.get().load(image).into(bookimage)

        bookimage.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(this)
                .setCancelable(false)
            val customlayout = LayoutInflater.from(this).inflate(R.layout.eventimagedialog, null)
            builder.setView(customlayout)
            val alertDialog = builder.create()
            val dialogeventimage =
                customlayout.findViewById<ImageView>(R.id.dialogeventimage)
            val dialogclose = customlayout.findViewById<Button>(R.id.button_close)
            Picasso.get().load(image).into(dialogeventimage)
            dialogclose.setOnClickListener {
                alertDialog.dismiss()
            }
            alertDialog.show()
        }

        bookname.text = name
        bookpubl.text = publ
        bookdept.text = dept
        bookyear.text = year

        if (auth.currentUser!!.uid == useruid) {
            errormsg.visibility = View.VISIBLE
            btnsold.visibility = View.VISIBLE

            btnsold.setOnClickListener {
                val title = getString(R.string.alerttitle)
                val foregroundcolorspan = ForegroundColorSpan(Color.RED)
                val ssbuilder = SpannableStringBuilder(title)
                ssbuilder.setSpan(
                    foregroundcolorspan,
                    0,
                    title.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                val builder = AlertDialog.Builder(this)
                builder.setMessage(getString(R.string.dialogdeletebook)).setTitle(ssbuilder)
                    .setCancelable(false)
                builder.setPositiveButton(getString(R.string.yes)) { dialogInterface, which ->
                    progressBar.visibility = View.VISIBLE
                    deletebook(bookid!!)
                }
                builder.setNegativeButton(getString(R.string.no)) { dialogInterface, which ->
                    dialogInterface.dismiss()
                }
                    .show()
            }

        }
        databaseReference.child(useruid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value != null) {
                        val personname = snapshot.child("Fullname").value.toString()
                        val personcontact = snapshot.child("Phone").value.toString()
                        oname.text = personname
                        ocontact.text = personcontact
                    }

                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@BookDetails, error.message, Toast.LENGTH_LONG).show()
                }
            })
    }
    private fun deletebook(bookid:String){
        btnsold.visibility=View.GONE
        firebaseStorage.delete().addOnSuccessListener {
            databaseReference1.child(bookid).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this@BookDetails, getString(R.string.bookdeleted), Toast.LENGTH_LONG)
                        .show()
                    progressBar.visibility = View.GONE
                }
        }.addOnFailureListener {
            OnFailureListener { p0 ->
                Toast.makeText(
                    this@BookDetails,
                    p0.message.toString(),
                    Toast.LENGTH_LONG
                ).show()
                progressBar.visibility = View.GONE
                btnsold.visibility=View.VISIBLE
            }
        }
    }
}