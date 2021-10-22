package com.bookqueen.bookqueen.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.bookqueen.bookqueen.R
import com.bookqueen.bookqueen.constants.Mycollege
import com.bookqueen.bookqueen.models.Booksetmodel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class booksetadapter(
    var Booksetlist: ArrayList<Booksetmodel>,
    var onItemClickListener: OnItemBooksetCliclListner
) : RecyclerView.Adapter<booksetadapter.booksetviwholder>() {

    var searchText = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): booksetviwholder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.singlebook, parent, false)
        return booksetviwholder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filteredlist(filteredlist: ArrayList<Booksetmodel>, searchText: String) {
        Booksetlist = filteredlist
        this.searchText = searchText
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: booksetviwholder, position: Int) {
        val view = Booksetlist[position]
        holder.bind(view, onItemClickListener)
    }

    override fun getItemCount(): Int {
        return Booksetlist.size
    }

    inner class booksetviwholder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        val booksetyear: TextView = itemview.findViewById(R.id.recbooknametxt)
        val semister = itemview.findViewById<TextView>(R.id.recbookpubtxt)
        val bookdept = itemview.findViewById<TextView>(R.id.recbookdepttxt)
        val booksetimage = itemview.findViewById<ImageView>(R.id.recbookimg)

        fun bind(bookset: Booksetmodel, onClickListener: OnItemBooksetCliclListner) {
            val database = FirebaseDatabase.getInstance()
            Mycollege.college(object : Mycollege.Mycallback {
                override fun onCallback(value: String) {
                    database.getReference("Users").child(bookset.UserUID.toString())
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            @SuppressLint("NotifyDataSetChanged")
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.value != null) {
                                    val usercollege = snapshot.child("College").value.toString()
                                    if (usercollege == value) {
                                        if (searchText.isNotEmpty()) {
                                            val highlightedtext = bookset.BookYear!!.replace(
                                                searchText,
                                                "<font color='red'>$searchText</font>",
                                                true
                                            )
                                            booksetyear.text =
                                                HtmlCompat.fromHtml(
                                                    highlightedtext,
                                                    HtmlCompat.FROM_HTML_MODE_LEGACY
                                                )
                                        } else {
                                            booksetyear.text = bookset.BookYear
                                        }
                                        //booksetyear.text = bookset.BookYear
                                        if (searchText.isNotEmpty()) {
                                            val highlightedtext = bookset.Booksetsemister!!.replace(
                                                searchText,
                                                "<font color='red'>$searchText</font>",
                                                true
                                            )
                                            semister.text =
                                                HtmlCompat.fromHtml(
                                                    highlightedtext,
                                                    HtmlCompat.FROM_HTML_MODE_LEGACY
                                                )
                                        } else {
                                            semister.text = bookset.Booksetsemister
                                        }
                                        //semister.text = bookset.Booksetsemister
                                        bookdept.text = bookset.Department
                                        Picasso.get().load(bookset.BooksetImage).into(booksetimage)

                                    } else {
                                        Booksetlist.remove(bookset)
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
                onClickListener.OnBooksetclicked(bookset)
            }
        }
    }

    interface OnItemBooksetCliclListner {
        fun OnBooksetclicked(Bookset: Booksetmodel)
    }

}