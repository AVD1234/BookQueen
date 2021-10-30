package com.bookqueen.bookqueen.Activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bookqueen.bookqueen.R
import com.bookqueen.bookqueen.adapters.eventadapter
import com.bookqueen.bookqueen.constants.Mycollege
import com.bookqueen.bookqueen.models.Eventsmodel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class Events : AppCompatActivity(), eventadapter.oneventitemclicklistner {
    lateinit var eventfloatingbutton: FloatingActionButton
    lateinit var databaseReference: DatabaseReference
    lateinit var auth: FirebaseAuth
    lateinit var eventprogressbar: ProgressBar
    lateinit var eventswiperefresh: SwipeRefreshLayout
    lateinit var eventrecyclerview: RecyclerView
    lateinit var eventadapter: eventadapter
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var database: FirebaseDatabase
    lateinit var noevents: TextView

    var eventlist = arrayListOf<Eventsmodel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)
        toolbar()

        firebaseStorage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()
        Mycollege.college(object :Mycollege.Mycallback{
            override fun onCallback(value: String) {
                databaseReference = database.getReference("Events").child(value)
                loadevents()
            }
        })
        auth = FirebaseAuth.getInstance()
        noevents = findViewById(R.id.noevents)
        eventswiperefresh = findViewById(R.id.eventswipelayout)
        eventprogressbar = findViewById(R.id.eventprogressbar)
        eventrecyclerview = findViewById(R.id.eventrecyclerview)
        eventrecyclerview.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        linearLayoutManager.reverseLayout = true
        eventrecyclerview.layoutManager = linearLayoutManager

        eventlist.clear()

        eventfloatingbutton = findViewById(R.id.eventfloatingbutton)
        eventfloatingbutton.setOnClickListener {
            startActivity(Intent(this, AddEvents::class.java))
        }
        eventswiperefresh.setOnRefreshListener {
            eventlist.clear()
            eventswiperefresh.isRefreshing = false
            loadevents()
        }
    }

    private fun loadevents() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SimpleDateFormat", "NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                eventlist.clear()
                for (item in snapshot.children) {
                    val eventimage = item.child("EventImage").value.toString()
                    val eventdate = item.child("EventDate").value.toString()
                    val eventid = item.child("EventID").value.toString()
                    val eventuseruid = item.child("UserUID").value.toString()

                    val sdf = SimpleDateFormat("dd/MM/yyyy")
                    val strDate: Date? = sdf.parse(eventdate)
                    val calendar = Calendar.getInstance()
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    var month = calendar.get(Calendar.MONTH)
                    month += 1
                    val year = calendar.get(Calendar.YEAR)
                    val today: Date? = sdf.parse("$day/$month/$year")

                    redeleteevent(eventdate, eventimage, eventid)

                    if (strDate!! < today) {
                        eventlist.add(
                            Eventsmodel(
                                eventid, eventuseruid,
                                eventimage,
                                getString(R.string.completed),
                                strDate.toString(),
                                R.color.colorPrimary
                            )
                        )
                        eventadapter = eventadapter(eventlist, this@Events)
                        eventadapter.notifyDataSetChanged()

                    } else {
                        eventlist.add(
                            Eventsmodel(
                                eventid,
                                eventuseruid,
                                eventimage,
                                getString(R.string.incomplete),
                                strDate.toString(),
                                R.color.red
                            )
                        )
                    }
                }
                eventadapter = eventadapter(eventlist, this@Events)
                eventadapter.notifyDataSetChanged()
                if (eventadapter.itemCount == 0) {
                    eventrecyclerview.visibility = View.GONE
                    eventprogressbar.visibility = View.GONE
                    noevents.text = getString(R.string.no_events_available)
                    noevents.visibility = View.VISIBLE
                } else {
                    eventrecyclerview.adapter = eventadapter
                    eventprogressbar.visibility = View.GONE
                    noevents.visibility = View.GONE
                    eventrecyclerview.visibility = View.VISIBLE
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@Events,
                    getString(R.string.unabletoloadretry),
                    Toast.LENGTH_SHORT
                ).show()
                eventprogressbar.visibility = View.GONE
            }
        })
    }

    private fun toolbar() {
        val toolbar: Toolbar = findViewById(R.id.eventtoolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Events"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun oneventitemclick(event: Eventsmodel) {
        val builder = AlertDialog.Builder(this)
            .setCancelable(false)
        val customlayout = LayoutInflater.from(this).inflate(R.layout.eventimagedialog, null)
        builder.setView(customlayout)
        val alertDialog = builder.create()
        val dialogeventimage =
            customlayout.findViewById<ImageView>(R.id.dialogeventimage)
        val dialogclose = customlayout.findViewById<Button>(R.id.button_close)
        Picasso.get().load(event.EventImage).into(dialogeventimage)
        dialogclose.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    override fun oneventitemlongclick(event: Eventsmodel) {
        if (event.eventuserid == auth.currentUser!!.uid) {
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
                .setMessage(getString(R.string.dialogdeleteevent))
                .setTitle(ssbuilder)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes)) { DialogInterface, which ->
                    deleteevent(event.EventImage, event.eventid)
                }
                .setNegativeButton(getString(R.string.no)) { DialogInterface, which ->
                    DialogInterface.dismiss()
                }
                .show()
        }

    }

    private fun deleteevent(eventimage: String, eventid: String) {
        firebaseStorage.getReferenceFromUrl(eventimage).delete().addOnSuccessListener {
            databaseReference.child(eventid).removeValue().addOnSuccessListener {
                Toast.makeText(this, getString(R.string.eventdeleted), Toast.LENGTH_SHORT)
                    .show()
            }.addOnFailureListener {
                Toast.makeText(this, getString(R.string.unabletodeleteevent), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun redeleteevent(eventdate: String, eventimage: String, eventid: String) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -6)
        val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val sdf1 = SimpleDateFormat("dd/MM/yyyy")
        val redate = sdf1.format(sdf.parse(cal.time.toString())!!)
        if (eventdate == redate) {
            deleteevent(eventimage, eventid)
        }
    }
}