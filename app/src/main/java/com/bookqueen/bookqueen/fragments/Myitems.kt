package com.bookqueen.bookqueen.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookqueen.bookqueen.Activity.BookDetails
import com.bookqueen.bookqueen.Activity.BooksetDetails
import com.bookqueen.bookqueen.Activity.ToolDetails
import com.bookqueen.bookqueen.R
import com.bookqueen.bookqueen.adapters.bookadapter
import com.bookqueen.bookqueen.adapters.booksetadapter
import com.bookqueen.bookqueen.adapters.tooladapter
import com.bookqueen.bookqueen.constants.Mycollege
import com.bookqueen.bookqueen.models.Booksetmodel
import com.bookqueen.bookqueen.models.Booksmodel
import com.bookqueen.bookqueen.models.Toolmodel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_addbooks.*
import java.util.*


class Myitems : Fragment(), bookadapter.OnBookItemClicklistner, tooladapter.OnToolItemClickListner,
    booksetadapter.OnItemBooksetCliclListner {

    lateinit var itembookrecyclerView: RecyclerView
    lateinit var itembookadapter: bookadapter
    lateinit var itembooksetrecyclerView: RecyclerView
    lateinit var itembooksetadapter: booksetadapter
    lateinit var itemtoolrecyclerView: RecyclerView
    lateinit var itemtooladapter: tooladapter
    lateinit var auth: FirebaseAuth
    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var databaseReference1: DatabaseReference
    lateinit var databaseReference2: DatabaseReference
    var booklist = arrayListOf<Booksmodel>()
    var toolslist = arrayListOf<Toolmodel>()
    var booksetlist = arrayListOf<Booksetmodel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_myitems, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        Mycollege.college(object :Mycollege.Mycallback{
            override fun onCallback(value: String) {
                databaseReference = database.getReference("Books").child(value)
                databaseReference1 = database.getReference("BookSets").child(value)
                databaseReference2 = database.getReference("Tools").child(value)
                showbooks()
                showbooksets()
                showtools()
            }
        })

        itembookrecyclerView = view.findViewById(R.id.itembookrecyclerview)
        itembookrecyclerView.setHasFixedSize(true)
        booklist.clear()
        itembookrecyclerView.layoutManager = LinearLayoutManager(context)

        itembooksetrecyclerView = view.findViewById(R.id.itembooksetrecyclerview)
        itembooksetrecyclerView.setHasFixedSize(true)
        booksetlist.clear()
        itembooksetrecyclerView.layoutManager = LinearLayoutManager(context)

        itemtoolrecyclerView = view.findViewById(R.id.itemtoolrecyclerview)
        itemtoolrecyclerView.setHasFixedSize(true)
        toolslist.clear()
        itemtoolrecyclerView.layoutManager = GridLayoutManager(context, 2)


        return view
    }

    private fun showbooksets() {

        databaseReference1.addValueEventListener(object : ValueEventListener {
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
                        if (userid == auth.currentUser!!.uid) {
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
                        itembooksetadapter = booksetadapter(booksetlist, this@Myitems)
                        itembooksetadapter.notifyDataSetChanged()
                        itembooksetrecyclerView.adapter = itembooksetadapter
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()

            }
        })
    }

    private fun showtools() {
        databaseReference2.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {

                try {
                    toolslist.clear()
                    for (item in snapshot.children) {
                        val toolname = item.child("Toolname").value.toString()
                        val toolimage = item.child("ToolImage").value.toString()
                        val useruid = item.child("UserUID").value.toString()
                        val toolid = item.child("ToolID").value.toString()
                        toolslist.add(Toolmodel(toolid, toolname, toolimage, useruid))
                        if(auth.currentUser!!.uid==useruid){
                            itemtooladapter = tooladapter(toolslist, this@Myitems)
                            itembookadapter.notifyDataSetChanged()
                            itemtoolrecyclerView.adapter = itemtooladapter
                        }

                    }
                } catch (e: Exception) {
                    Toast.makeText(context, e.message.toString(), Toast.LENGTH_LONG).show()

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message.toString(), Toast.LENGTH_LONG).show()


            }
        })
    }

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
                        if (userid == auth.currentUser!!.uid) {
                            booklist.add(
                                Booksmodel(
                                    bookid,
                                    bookimage,
                                    bookname,
                                    bookdept,
                                    bookpublication,
                                    bookyear,
                                    userid
                                )
                            )
                        }
                        itembookadapter = bookadapter(booklist, this@Myitems)
                        itembookadapter.notifyDataSetChanged()
                        itembookrecyclerView.adapter = itembookadapter

                    }
                } catch (e: Exception) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.checkconnection), Toast.LENGTH_SHORT).show()
            }

        })
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

    override fun OntoolItemclick(Tool: Toolmodel) {
        val intent = Intent(context, ToolDetails::class.java)
        intent.putExtra("toolname", Tool.Toolname)
        intent.putExtra("toolimage", Tool.ToolImage)
        intent.putExtra("useruid", Tool.userUID)
        intent.putExtra("toolid", Tool.Toolid)
        startActivity(intent)
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

}