package com.bookqueen.bookqueen.Activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bookqueen.bookqueen.R
import com.bookqueen.bookqueen.constants.Mycollege
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class ToolDetails : AppCompatActivity() {
    lateinit var toolname: TextView
    lateinit var toolerrormsg: TextView
    lateinit var btntoolsold: Button
    lateinit var toolimage: ImageView
    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var databaseReference1: DatabaseReference
    lateinit var firebaseStorage: StorageReference
    lateinit var auth: FirebaseAuth
    lateinit var tooldetprogressBar: ProgressBar

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tool_details)

        toolname = findViewById(R.id.dettoolname)
        toolimage = findViewById(R.id.imgdettool)
        btntoolsold = findViewById(R.id.btntoolsold)
        toolerrormsg = findViewById(R.id.toolerormsg)
        tooldetprogressBar = findViewById(R.id.tooldetprogress)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("Users")
        Mycollege.college(object : Mycollege.Mycallback {
            override fun onCallback(value: String) {
                databaseReference1 = database.getReference("Tools").child(value)
            }
        })
        firebaseStorage = FirebaseStorage.getInstance()
            .getReferenceFromUrl(intent.getStringExtra("toolimage").toString())

        toolname.text = intent.getStringExtra("toolname").toString()
        Picasso.get().load(intent.getStringExtra("toolimage").toString()).into(toolimage)

        toolimage.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(this)
                .setCancelable(false)
            val customlayout = LayoutInflater.from(this).inflate(R.layout.eventimagedialog, null)
            builder.setView(customlayout)
            val alertDialog = builder.create()
            val dialogeventimage =
                customlayout.findViewById<ImageView>(R.id.dialogeventimage)
            val dialogclose = customlayout.findViewById<Button>(R.id.button_close)
            Picasso.get().load(intent.getStringExtra("toolimage").toString())
                .into(dialogeventimage)
            dialogclose.setOnClickListener {
                alertDialog.dismiss()
            }
            alertDialog.show()
        }

        val tooldate = intent.getStringExtra("toolDate").toString()
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -6)
        val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        //val strDate: Date? = sdf.parse(cal.time.toString())
        val sdf1 = SimpleDateFormat("dd/MM/yyyy")
        val redate = sdf1.format(sdf.parse(cal.time.toString())!!)
        if (tooldate == redate) {
            deletetool()
        }

        if (auth.currentUser!!.uid == intent.getStringExtra("useruid").toString()) {
            toolerrormsg.visibility = View.VISIBLE
            btntoolsold.visibility = View.VISIBLE

            btntoolsold.setOnClickListener {
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
                builder.setMessage(getString(R.string.dialogdeletetool)).setTitle(ssbuilder)
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.yes)) { dialogInterface, which ->
                        deletetool()
                        tooldetprogressBar.visibility = View.VISIBLE

                    }.setNegativeButton(getString(R.string.no)) { dialogInterface, which ->
                        dialogInterface.dismiss()
                    }
                    .show()
            }
        }

        ownerdetail()

    }

    private fun ownerdetail() {
        val toolownername = findViewById<TextView>(R.id.toolcontactname)
        val toolownerphone = findViewById<TextView>(R.id.toolcontactphone)
        databaseReference.child(intent.getStringExtra("useruid").toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value != null) {
                        toolownername.text = snapshot.child("Fullname").value.toString()
                        toolownerphone.text = snapshot.child("Phone").value.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ToolDetails,
                        getString(R.string.unabletoloadowner),
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    fun deletetool() {
        btntoolsold.visibility = View.GONE
        firebaseStorage.delete().addOnSuccessListener {
            databaseReference1.child(intent.getStringExtra("toolid").toString())
                .removeValue().addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.tooldeleted), Toast.LENGTH_LONG)
                        .show()
                    tooldetprogressBar.visibility = View.GONE
                }
        }.addOnFailureListener {
            OnFailureListener { p0 ->
                Toast.makeText(this@ToolDetails, p0.message, Toast.LENGTH_LONG)
                    .show()
                tooldetprogressBar.visibility = View.GONE
                btntoolsold.visibility = View.VISIBLE

            }
        }
    }
}