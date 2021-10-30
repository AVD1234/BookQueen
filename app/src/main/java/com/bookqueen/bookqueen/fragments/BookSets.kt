package com.bookqueen.bookqueen.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bookqueen.bookqueen.Activity.Addbooksets
import com.bookqueen.bookqueen.Activity.BooksetDetails
import com.bookqueen.bookqueen.ConnectionManager.ConnectionManager
import com.bookqueen.bookqueen.R
import com.bookqueen.bookqueen.adapters.booksetadapter
import com.bookqueen.bookqueen.constants.Mycollege
import com.bookqueen.bookqueen.models.Booksetmodel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class BookSets : Fragment(), booksetadapter.OnItemBooksetCliclListner {

    lateinit var addbooksets: FloatingActionButton
    lateinit var booksetswiperefresh: SwipeRefreshLayout
    lateinit var booksetrecyclerview: RecyclerView
    lateinit var booksetadapter: booksetadapter
    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var booksetprogressbar: ProgressBar
    lateinit var auth: FirebaseAuth
    var booksetlist = arrayListOf<Booksetmodel>()
    lateinit var nobooksetsfound: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_book_sets, container, false)

        booksetswiperefresh = view.findViewById(R.id.booksetswiperefresh)
        booksetrecyclerview = view.findViewById(R.id.booksetrecyclerview)
        booksetrecyclerview.setHasFixedSize(true)
        booksetlist.clear()
        booksetrecyclerview.layoutManager = LinearLayoutManager(context)
        booksetprogressbar = view.findViewById(R.id.booksetprogressbar)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        Mycollege.college(object : Mycollege.Mycallback {
            override fun onCallback(value: String) {
                Log.d("mycollege", value)
                databaseReference =
                    FirebaseDatabase.getInstance().getReference("BookSets").child(value)
                showbooksets()
            }
        })



        setHasOptionsMenu(true)
        nobooksetsfound = view.findViewById(R.id.nobooksetsavailable)
        addbooksets = view.findViewById(R.id.bookSetsFAB)
        addbooksets.setOnClickListener {
            startActivity(Intent(context, Addbooksets::class.java))
        }
        booksetswiperefresh.setOnRefreshListener {
            booksetlist.clear()
            booksetswiperefresh.isRefreshing = false
            showbooksets()
        }
        return view
    }

    private fun showbooksets() {

        databaseReference.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    booksetlist.clear()
                    for (item in snapshot.children) {
                        val book1 = item.child("Book1").value.toString()
                        val book2 = item.child("Book2").value.toString()
                        val book3 = item.child("Book3").value.toString()
                        val book4 = item.child("Book4").value.toString()
                        val book5 = item.child("Book5").value.toString()
                        val booksetid = item.child("BooksetID").value.toString()
                        val userid = item.child("UserUID").value.toString()
                        val bookimage = item.child("BookImage").value.toString()
                        val booksetyear = item.child("BookYear").value.toString()
                        val booksetdept = item.child("Department").value.toString()
                        val semister = item.child("Semister").value.toString()
                        val date = item.child("Date").value.toString()

                        RedeleteBookset(date, bookimage, booksetid)

                        booksetlist.add(
                            Booksetmodel(
                                booksetid,
                                bookimage,
                                semister,
                                booksetdept,
                                book1,
                                book2,
                                book3,
                                book4,
                                book5,
                                booksetyear,
                                userid
                            )
                        )

                    }
                    booksetadapter = booksetadapter(
                        booksetlist,
                        this@BookSets
                    )
                    booksetadapter.notifyDataSetChanged()
                    if (booksetadapter.itemCount == 0) {
                        booksetprogressbar.visibility =
                            View.GONE
                        nobooksetsfound.text = getString(R.string.nobooksetsavailable)
                        nobooksetsfound.visibility = View.VISIBLE
                        booksetrecyclerview.visibility = View.GONE
                    } else {
                        booksetrecyclerview.adapter =
                            booksetadapter
                        booksetprogressbar.visibility =
                            View.GONE
                        booksetrecyclerview.visibility = View.VISIBLE
                        nobooksetsfound.visibility = View.GONE

                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        e.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    booksetprogressbar.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()
                booksetprogressbar.visibility = View.GONE
            }
        })
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater
    ) {

        inflater.inflate(R.menu.fragmentmenu, menu)
        val menuItem = menu.findItem(R.id.search)

        val searchView = menuItem.actionView as SearchView
        searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText)
                return false
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun filter(newText: String?) {
        val filteredlist = ArrayList<Booksetmodel>()
        for (item in booksetlist) {
            if (item.BookYear!!.lowercase(Locale.getDefault())
                    .contains(newText!!.lowercase(Locale.getDefault())) || item.Department!!.lowercase(
                    Locale.getDefault()
                )
                    .contains(newText.lowercase(Locale.getDefault()))
            ) {
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()) {
            booksetrecyclerview.visibility = View.GONE
            nobooksetsfound.visibility = View.VISIBLE
            nobooksetsfound.text = "\" $newText \" no booksets found"
            //  Toast.makeText(context, getString(R.string.nodatafound), Toast.LENGTH_SHORT).show()
        } else {
            booksetrecyclerview.visibility = View.VISIBLE
            nobooksetsfound.visibility = View.GONE
            booksetadapter.filteredlist(filteredlist, newText!!)
        }

    }

    override fun OnBooksetclicked(Bookset: Booksetmodel) {

        val intent = Intent(context, BooksetDetails::class.java)
        intent.putExtra("book1", Bookset.Book1)
        intent.putExtra("book2", Bookset.Book2)
        intent.putExtra("book3", Bookset.Book3)
        intent.putExtra("book4", Bookset.Book4)
        intent.putExtra("book5", Bookset.Book5)
        intent.putExtra("booksetimage", Bookset.BooksetImage)
        intent.putExtra("booksetdept", Bookset.Department)
        intent.putExtra("booksetyear", Bookset.BookYear)
        intent.putExtra("booksetid", Bookset.BooksetId)
        intent.putExtra("booksetuseruid", Bookset.UserUID)
        intent.putExtra("semister", Bookset.Booksetsemister)
        startActivity(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!ConnectionManager().isconnected(requireContext())) {
            val snackbar = Snackbar.make(view, getString(R.string.nointernet), Snackbar.LENGTH_LONG)
            snackbar.setAction(getString(R.string.settings)) {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
            snackbar.show()
            booksetprogressbar.visibility = View.GONE

        }
        super.onViewCreated(view, savedInstanceState)
    }

    @SuppressLint("SimpleDateFormat")
    private fun RedeleteBookset(booksetdate: String, booksetimage: String, booksetid: String) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -6)
        val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val sdf1 = SimpleDateFormat("dd/MM/yyyy")
        val redate = sdf1.format(sdf.parse(cal.time.toString())!!)
        if (booksetdate == redate) {
            val firebaseStorage =
                FirebaseStorage.getInstance().getReferenceFromUrl(booksetimage)
            firebaseStorage.delete().addOnSuccessListener {
                databaseReference.child(booksetid)
                    .removeValue().addOnSuccessListener {
                    }
            }
        }
    }
}