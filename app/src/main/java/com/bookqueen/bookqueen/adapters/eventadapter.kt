package com.bookqueen.bookqueen.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bookqueen.bookqueen.R
import com.bookqueen.bookqueen.constants.Mycollege
import com.bookqueen.bookqueen.models.Eventsmodel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class eventadapter(
    val eventlist: ArrayList<Eventsmodel>,
    val onitemclicklistner: oneventitemclicklistner
) :
    RecyclerView.Adapter<eventadapter.eventviewholder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): eventviewholder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.singleevent, parent, false)
        return eventviewholder(view)
    }

    override fun onBindViewHolder(holder: eventviewholder, position: Int) {
        val view = eventlist[position]
        holder.bind(view, onitemclicklistner)

    }

    override fun getItemCount(): Int {
        return if (eventlist.size == 0) 0 else eventlist.size
    }

    inner class eventviewholder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        val eventimage = itemview.findViewById<ImageView>(R.id.eventimg)
        val eventstatus = itemview.findViewById<TextView>(R.id.eventstatus)
        val eventdate = itemview.findViewById<TextView>(R.id.eventdate)
        val statuslayout = itemview.findViewById<LinearLayout>(R.id.statuslayout)

        @SuppressLint("SimpleDateFormat")
        fun dind(events: Eventsmodel, clicklistner: oneventitemclicklistner) {
            eventstatus.text = events.status
            val sdf = SimpleDateFormat(
                "EEE MMM dd HH:mm:ss zzz yyyy",
                Locale.ENGLISH
            )
            val sdf1 = SimpleDateFormat("dd/MM/yyyy")
            val redate = sdf1.format(sdf.parse(events.datetime)!!)
            eventdate.text = redate.toString()
            Picasso.get().load(events.EventImage).into(eventimage)
            statuslayout.setBackgroundColor(events.layoutcolor)
            itemView.setOnClickListener {
                clicklistner.oneventitemclick(events)
            }
            itemView.setOnLongClickListener(object : View.OnLongClickListener {
                override fun onLongClick(v: View?): Boolean {
                    clicklistner.oneventitemlongclick(events)
                    return true
                }
            })
        }


        @SuppressLint("ResourceAsColor", "SimpleDateFormat")
        fun bind(events: Eventsmodel, clicklistner: oneventitemclicklistner) {
            val database = FirebaseDatabase.getInstance()
            Mycollege.college(object : Mycollege.Mycallback {
                override fun onCallback(value: String) {
                    Log.d("mycollege", value)
                    database.getReference("Users").child(events.eventuserid.toString())
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            @SuppressLint("NotifyDataSetChanged")
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.value != null) {
                                    val usercollege = snapshot.child("College").value.toString()
                                    if (usercollege == value) {
                                        eventstatus.text = events.status
                                        val sdf = SimpleDateFormat(
                                            "EEE MMM dd HH:mm:ss zzz yyyy",
                                            Locale.ENGLISH
                                        )
                                        val sdf1 = SimpleDateFormat("dd/MM/yyyy")
                                        val redate = sdf1.format(sdf.parse(events.datetime)!!)
                                        eventdate.text = redate.toString()
                                        Picasso.get().load(events.EventImage).into(eventimage)
                                        statuslayout.setBackgroundColor(events.layoutcolor)

                                    } else {
                                        eventlist.remove(events)
                                        notifyDataSetChanged()
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                }
            })
            itemView.setOnClickListener {
                clicklistner.oneventitemclick(events)
            }
            itemView.setOnLongClickListener(object : View.OnLongClickListener {
                override fun onLongClick(v: View?): Boolean {
                    clicklistner.oneventitemlongclick(events)
                    return true
                }
            })
        }
    }

    interface oneventitemclicklistner {
        fun oneventitemclick(event: Eventsmodel)
        fun oneventitemlongclick(event: Eventsmodel)
    }
}




