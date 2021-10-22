package com.bookqueen.bookqueen.adapters


import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.bookqueen.bookqueen.R
import com.bookqueen.bookqueen.constants.Mycollege
import com.bookqueen.bookqueen.models.Booksmodel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso


class bookadapter(
    private var booklist: ArrayList<Booksmodel>,
    private val itemClickListener: OnBookItemClicklistner
) : RecyclerView.Adapter<bookadapter.bookholder>() {

    var searchText: String = ""


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): bookholder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.singlebook, parent, false)
        return bookholder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterlist(filterlist: ArrayList<Booksmodel>, searchText: String) {
        booklist = filterlist
        this.searchText = searchText
        notifyDataSetChanged()

    }

    override fun onBindViewHolder(holder: bookholder, position: Int) {
        val view = booklist[position]
//        holder.bookname.text=view.Bookname
//        holder.bookpublication.text=view.Bookpublication
//        holder.bookdept.text=view.BookDept
//        Picasso.get().load(view.Bookimage).into(holder.bookimage)
        holder.dind(view, itemClickListener)


    }

    override fun getItemCount(): Int {
        return booklist.size
    }

    inner class bookholder(view: View) : RecyclerView.ViewHolder(view) {
        val bookname: TextView = view.findViewById(R.id.recbooknametxt)
        val bookpublication = view.findViewById<TextView>(R.id.recbookpubtxt)
        val bookdept = view.findViewById<TextView>(R.id.recbookdepttxt)
        val bookimage = view.findViewById<ImageView>(R.id.recbookimg)
        val bookview = view.findViewById<CardView>(R.id.bookcardView)

        fun dind(book: Booksmodel, clicklistner: OnBookItemClicklistner) {
            if (searchText.isNotBlank()) {
                val highlightedText = book.BookName!!.replace(
                    searchText,
                    "<font color='red'>$searchText</font>",
                    true
                )
                bookname.text =
                    HtmlCompat.fromHtml(highlightedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
            } else {
                bookname.text = book.BookName
            }
            //bookname.text=book.BookName
            bookpublication.text = book.BookPublication
            bookdept.text = book.Department
            Picasso.get().load(book.BookImage).into(bookimage)
            itemView.setOnClickListener {
                clicklistner.onBookItemclick(book)
            }
        }

        fun bind(book: Booksmodel, clicklistner: OnBookItemClicklistner) {

            val database = FirebaseDatabase.getInstance()
            val auth = FirebaseAuth.getInstance()
            Mycollege.college(object : Mycollege.Mycallback {
                override fun onCallback(value: String) {
                    Log.d("Mycoolege", value)
                    database.getReference("Users").child(book.UserUID.toString())
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            @SuppressLint("NotifyDataSetChanged")
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.value != null) {
                                    val usercollege = snapshot.child("College").value.toString()

                                    if (usercollege == value) {

                                        if (searchText.isNotBlank()) {
                                            val highlightedText = book.BookName!!.replace(
                                                searchText,
                                                "<font color='red'>$searchText</font>",
                                                true
                                            )
                                            bookname.text =
                                                HtmlCompat.fromHtml(
                                                    highlightedText,
                                                    HtmlCompat.FROM_HTML_MODE_LEGACY
                                                )
                                        } else {
                                            bookname.text = book.BookName
                                        }
                                        //bookname.text=book.BookName
                                        bookpublication.text = book.BookPublication
                                        bookdept.text = book.Department
                                        Picasso.get().load(book.BookImage)
                                            .error(R.drawable.ic_action_book)
                                            .into(bookimage)
                                    } else {
                                        booklist.remove(book)
                                        notifyDataSetChanged()
                                        //bookview.visibility = View.GONE
//                                                //bookview.layoutParams = ViewGroup.LayoutParams(0, 0);
//                                                val layoutParams: ViewGroup.LayoutParams =
//                                                    itemView.layoutParams
//                                                layoutParams.width = 0
//                                                layoutParams.height = 0
//                                                itemView.layoutParams = layoutParams
//                                                val p = itemView.layoutParams as ViewGroup.MarginLayoutParams
//                                                p.setMargins(0, 0, 0, 0)
                                    }

                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                }
            })


            itemView.setOnClickListener {
                clicklistner.onBookItemclick(book)
            }
        }

    }

    interface OnBookItemClicklistner {
        fun onBookItemclick(books: Booksmodel)
    }

}