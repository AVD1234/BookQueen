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
import com.bookqueen.bookqueen.Activity.Addbooks
import com.bookqueen.bookqueen.Activity.BookDetails
import com.bookqueen.bookqueen.Activity.Events
import com.bookqueen.bookqueen.ConnectionManager.ConnectionManager
import com.bookqueen.bookqueen.R
import com.bookqueen.bookqueen.adapters.bookadapter
import com.bookqueen.bookqueen.constants.Mycollege
import com.bookqueen.bookqueen.models.Booksmodel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_addbooks.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class Books : Fragment(), bookadapter.OnBookItemClicklistner {

    lateinit var addbooks: FloatingActionButton
    lateinit var bookrecyclerView: RecyclerView
    lateinit var bookswiperefresh: SwipeRefreshLayout
    lateinit var bookadapter: bookadapter
    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var auth: FirebaseAuth
    var booklist = arrayListOf<Booksmodel>()
    lateinit var bookprogressbar: ProgressBar
    lateinit var nobookfound: TextView

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_books, container, false)

        setHasOptionsMenu(true)

        addbooks = view.findViewById(R.id.booksFAB)
        bookswiperefresh = view.findViewById(R.id.bookswiperefresh)
        bookrecyclerView = view.findViewById(R.id.bookrecyclerview)
        bookrecyclerView.setHasFixedSize(true)
        bookprogressbar = view.findViewById(R.id.bookprogressbar)
        bookprogressbar.visibility = View.VISIBLE
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        Mycollege.college(object : Mycollege.Mycallback {
            override fun onCallback(value: String) {
                Log.d("mycollege", value)
                databaseReference = database.getReference("Books").child(value)
                showbooks()

            }
        })
        nobookfound = view.findViewById(R.id.nobooksavailable)

        booklist.clear()

        bookrecyclerView.layoutManager = LinearLayoutManager(context)

        bookswiperefresh.setOnRefreshListener {
            booklist.clear()
            bookswiperefresh.isRefreshing = false
            showbooks()
        }


        addbooks.setOnClickListener {
            startActivity(Intent(context, Addbooks::class.java))

        }


        return view
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun showbooks() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    booklist.clear()
                    for (item in snapshot.children) {
                        val bookid = item.child("BookId").value.toString()
                        val bookname = item.child("BookName").value.toString()
                        val bookpublication = item.child("BookPublication").value.toString()
                        val bookdept = item.child("Department").value.toString()
                        val bookimage = item.child("BookImage").value.toString()
                        val bookyear = item.child("BookYear").value.toString()
                        val userid = item.child("UserUID").value.toString()
                        val date = item.child("Date").value.toString()
                        RedeleteBook(date, bookimage, bookid)
                        booklist.add(
                            Booksmodel(
                                bookid,
                                bookimage,
                                bookname,
                                bookdept,
                                bookpublication,
                                bookyear,
                                userid,
                            )
                        )

                    }
                    bookadapter =
                        bookadapter(
                            booklist,
                            this@Books
                        )
                    bookadapter.notifyDataSetChanged()
                    Log.d("Booksize", (bookadapter.itemCount.toString()))
                    if (bookadapter.itemCount == 0) {
                        bookprogressbar.visibility =
                            View.GONE
                        nobookfound.text = getString(R.string.nobookavailable)
                        nobookfound.visibility = View.VISIBLE
                        bookrecyclerView.visibility = View.GONE
                    } else {
                        bookrecyclerView.adapter =
                            bookadapter
                        bookprogressbar.visibility =
                            View.GONE
                        bookrecyclerView.visibility = View.VISIBLE
                        nobookfound.visibility = View.GONE

                    }

                } catch (e: Exception) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    bookprogressbar.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "please check connection", Toast.LENGTH_SHORT).show()
                bookprogressbar.visibility = View.GONE
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.booksmenu, menu)
        val searchItem = menu.findItem(R.id.search)

        val searchView: SearchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {

                filter(newText)
                return false
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.events -> {
                startevents()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    @SuppressLint("SetTextI18n")
    private fun filter(text: String) {
        val filteredlist = ArrayList<Booksmodel>()


        for (item in booklist) {
            if (item.BookName!!.lowercase(Locale.getDefault())
                    .contains(text.lowercase(Locale.getDefault()))
            ) {
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()) {
            bookrecyclerView.visibility = View.GONE
            nobookfound.visibility = View.VISIBLE
            nobookfound.text = "\" $text \" books not found."
            //Toast.makeText(context, getString(R.string.nodatafound), Toast.LENGTH_SHORT).show()
        } else {
            bookrecyclerView.visibility = View.VISIBLE
            nobookfound.visibility = View.GONE
            bookadapter.filterlist(filteredlist, text)
        }
    }

    override fun onBookItemclick(books: Booksmodel) {
        val intent = Intent(requireActivity().applicationContext, BookDetails::class.java)

        intent.putExtra("Bookid", books.BookId)
        intent.putExtra("Bookname", books.BookName)
        intent.putExtra("Bookpubl", books.BookPublication)
        intent.putExtra("BookImage", books.BookImage)
        intent.putExtra("Bookdept", books.Department)
        intent.putExtra("Bookyear", books.BookYear)
        intent.putExtra("UserUID", books.UserUID)
        startActivity(intent)
    }

    private fun startevents() {
        startActivity(Intent(context, Events::class.java))
    }


    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!ConnectionManager().isconnected(requireContext())) {
            val snackbar = Snackbar.make(view, getString(R.string.nointernet), Snackbar.LENGTH_LONG)
            snackbar.setAction(getString(R.string.settings)) {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
            snackbar.show()
            bookprogressbar.visibility = View.GONE

        }

    }

    @SuppressLint("SimpleDateFormat")
    private fun RedeleteBook(bookdate: String, bookimage: String, bookid: String) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -6)
        val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val sdf1 = SimpleDateFormat("dd/MM/yyyy")
        val redate = sdf1.format(sdf.parse(cal.time.toString())!!)
        if (bookdate == redate) {
            val firebaseStorage =
                FirebaseStorage.getInstance().getReferenceFromUrl(bookimage)
            firebaseStorage.delete().addOnSuccessListener {
                databaseReference.child(bookid)
                    .removeValue().addOnSuccessListener {
                    }
            }
        }
    }
}