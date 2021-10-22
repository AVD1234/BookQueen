package com.bookqueen.bookqueen.Activity

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bookqueen.bookqueen.R
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_bookset_details.*

class BooksetDetails : AppCompatActivity() {
    lateinit var book1: TextView
    lateinit var book2: TextView
    lateinit var book3: TextView
    lateinit var book4: TextView
    lateinit var book5: TextView
    lateinit var semister: TextView
    lateinit var booksetdept: TextView
    lateinit var booksetyear: TextView
    lateinit var bookseterrormsg: TextView
    lateinit var btnbooksetsold: Button
    lateinit var booksetimage: ImageView
    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var firebaseStorage: StorageReference
    lateinit var auth: FirebaseAuth
    lateinit var setdetailprogressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookset_details)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("Users")

        book1 = findViewById(R.id.detbook1)
        book1.text = intent.getStringExtra("book1").toString()
        book2 = findViewById(R.id.detbook2)
        book2.text = intent.getStringExtra("book2").toString()
        book3 = findViewById(R.id.detbook3)
        book3.text = intent.getStringExtra("book3").toString()
        book4 = findViewById(R.id.detbook4)
        book4.text = intent.getStringExtra("book4").toString()
        book5 = findViewById(R.id.detbook5)
        book5.text = intent.getStringExtra("book5").toString()
        booksetimage = findViewById(R.id.imgdetbookset)
        Picasso.get().load(intent.getStringExtra("booksetimage").toString()).into(booksetimage)

        booksetimage.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(this)
                .setCancelable(false)
            val customlayout = LayoutInflater.from(this).inflate(R.layout.eventimagedialog, null)
            builder.setView(customlayout)
            val alertDialog = builder.create()
            val dialogeventimage =
                customlayout.findViewById<ImageView>(R.id.dialogeventimage)
            val dialogclose = customlayout.findViewById<Button>(R.id.button_close)
            Picasso.get().load(intent.getStringExtra("booksetimage").toString())
                .into(dialogeventimage)
            dialogclose.setOnClickListener {
                alertDialog.dismiss()
            }
            alertDialog.show()
        }

        firebaseStorage = FirebaseStorage.getInstance()
            .getReferenceFromUrl(intent.getStringExtra("booksetimage").toString())

        booksetyear = findViewById(R.id.booksetyear)
        booksetyear.text = intent.getStringExtra("booksetyear").toString()
        booksetdept = findViewById(R.id.booksetdepartment)
        booksetdept.text = intent.getStringExtra("booksetdept").toString()
        semister = findViewById(R.id.semister)
        semister.text = intent.getStringExtra("semister").toString()


        btnbooksetsold = findViewById(R.id.btnsetsold)
        bookseterrormsg = findViewById(R.id.bookseterormsg)
        setdetailprogressBar = findViewById(R.id.booksetdetprogress)
        if (auth.currentUser!!.uid == intent.getStringExtra("booksetuseruid").toString()) {
            btnbooksetsold.visibility = View.VISIBLE
            bookseterrormsg.visibility = View.VISIBLE
            btnbooksetsold.setOnClickListener {
                val title = getString(R.string.alerttitle)
                val foregroundcolorspan = ForegroundColorSpan(Color.RED)
                val ssbuilder = SpannableStringBuilder(title)
                ssbuilder.setSpan(
                    foregroundcolorspan,
                    0,
                    title.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                AlertDialog.Builder(this)
                    .setMessage(getString(R.string.dialogdeletebookset))
                    .setTitle(ssbuilder)
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.yes)) { DialogInterface, which ->
                        booksetdetprogress.visibility = View.VISIBLE
                        Deletebookset()
                    }
                    .setNegativeButton(getString(R.string.no)) { DialogInterface, which ->
                        DialogInterface.dismiss()
                    }
                    .show()
            }
        }
        ownerDetail()
    }

    private fun ownerDetail() {
        val ownername = findViewById<TextView>(R.id.booksetcontactname)
        val ownerphone = findViewById<TextView>(R.id.booksetcontactphone)
        databaseReference.child(intent.getStringExtra("booksetuseruid").toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value != null) {
                        ownername.text = snapshot.child("Fullname").value.toString()
                        ownerphone.text = snapshot.child("Phone").value.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@BooksetDetails,
                        getString(R.string.unabletoloadowner),
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun Deletebookset() {
        btnsetsold.visibility = View.GONE
        firebaseStorage.delete().addOnSuccessListener {
            database.getReference("BookSets").child(intent.getStringExtra("booksetid").toString())
                .removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.booksetdeleted), Toast.LENGTH_SHORT)
                        .show()
                    booksetdetprogress.visibility = View.GONE
                }
        }.addOnFailureListener {
            OnFailureListener { p0 ->
                Toast.makeText(this, p0.message.toString(), Toast.LENGTH_SHORT)
                    .show()
                booksetdetprogress.visibility = View.GONE
                btnbooksetsold.visibility = View.VISIBLE
            }
        }
    }

}