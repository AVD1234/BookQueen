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
import com.bookqueen.bookqueen.models.Toolmodel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class tooladapter(
    var toolslist: ArrayList<Toolmodel>,
    val onItemClickListener: OnToolItemClickListner
) : RecyclerView.Adapter<tooladapter.toolviewholder>() {

    var searchText = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): toolviewholder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.singletool, parent, false)
        return toolviewholder(view)
    }

    override fun onBindViewHolder(holder: toolviewholder, position: Int) {
        val view = toolslist[position]
        holder.bind(view, onItemClickListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterlist(filterlist: ArrayList<Toolmodel>, searchText: String) {

        this.searchText = searchText
        toolslist = filterlist

        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return if (toolslist.size == 0) 0 else toolslist.size
    }

    inner class toolviewholder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        val toolname = itemview.findViewById<TextView>(R.id.toolname)
        val toolimage = itemview.findViewById<ImageView>(R.id.toolimage)

        fun dind(tool: Toolmodel, clicklistner: OnToolItemClickListner) {
            if (searchText.isNotEmpty()) {
                val highlightedtext = tool.Toolname!!.replace(
                    searchText,
                    "<font color='red'>$searchText</font>",
                    true
                )
                toolname.text =
                    HtmlCompat.fromHtml(highlightedtext, HtmlCompat.FROM_HTML_MODE_LEGACY)
            } else {
                toolname.text = tool.Toolname
            }
            //toolname.text=tool.Toolname
            Picasso.get().load(tool.ToolImage).into(toolimage)
            itemView.setOnClickListener {
                clicklistner.OntoolItemclick(tool)
            }
        }

        fun bind(tool: Toolmodel, clicklistner: OnToolItemClickListner) {
            val database = FirebaseDatabase.getInstance()
            Mycollege.college(object : Mycollege.Mycallback {
                override fun onCallback(value: String) {
                    Log.d("mycollege", value)
                    database.getReference("Users").child(tool.userUID.toString())
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            @SuppressLint("NotifyDataSetChanged")
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.value != null) {
                                    val usercollege = snapshot.child("College").value.toString()
                                    if (usercollege == value) {

                                        if (searchText.isNotBlank()) {
                                            val highlightedText = tool.Toolname!!.replace(
                                                searchText,
                                                "<font color='red'>$searchText</font>",
                                                true
                                            )
                                            toolname.text =
                                                HtmlCompat.fromHtml(
                                                    highlightedText,
                                                    HtmlCompat.FROM_HTML_MODE_LEGACY
                                                )
                                        } else {
                                            toolname.text = tool.Toolname
                                        }
                                        //toolname.text=tool.Toolname
                                        Picasso.get().load(tool.ToolImage).into(toolimage)

                                    } else {
                                        toolslist.remove(tool)
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
                clicklistner.OntoolItemclick(tool)
            }
        }
    }

    interface OnToolItemClickListner {
        fun OntoolItemclick(Tool: Toolmodel)
    }

}