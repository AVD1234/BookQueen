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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bookqueen.bookqueen.Activity.Addtools
import com.bookqueen.bookqueen.ConnectionManager.ConnectionManager
import com.bookqueen.bookqueen.R
import com.bookqueen.bookqueen.adapters.tooladapter
import com.bookqueen.bookqueen.constants.Mycollege
import com.bookqueen.bookqueen.models.Toolmodel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.bookqueen.bookqueen.Activity.ToolDetails as ToolDetails1


class Tools : Fragment(), tooladapter.OnToolItemClickListner {

    lateinit var addtools: FloatingActionButton
    lateinit var database: FirebaseDatabase
    lateinit var auth: FirebaseAuth
    lateinit var databaseReference: DatabaseReference
    var toolslist = arrayListOf<Toolmodel>()
    lateinit var toolswiperefresh: SwipeRefreshLayout
    lateinit var toolrecyclerviiew: RecyclerView
    lateinit var toolprogressBar: ProgressBar
    lateinit var tooadapter: tooladapter
    lateinit var notoolsfound: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_tools, container, false)
        setHasOptionsMenu(true)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        Mycollege.college(object : Mycollege.Mycallback {
            override fun onCallback(value: String) {
                Log.d("mycollege", value)
                databaseReference = FirebaseDatabase.getInstance().getReference("Tools").child(value)
                loaddata()
            }
        })

        toolprogressBar = view.findViewById(R.id.toolprogressbar)
        toolswiperefresh = view.findViewById(R.id.toolswiperefresh)
        toolrecyclerviiew = view.findViewById(R.id.toolrecyclerview)
        notoolsfound = view.findViewById(R.id.notoolsavailable)
        toolrecyclerviiew.setHasFixedSize(true)
        toolslist.clear()

        toolrecyclerviiew.layoutManager = GridLayoutManager(context, 2)

        addtools = view.findViewById(R.id.ToolsFAB)
        addtools.setOnClickListener {
            startActivity(Intent(context, Addtools::class.java))
        }

        toolswiperefresh.setOnRefreshListener {
            toolslist.clear()
            toolswiperefresh.isRefreshing = false
            loaddata()
        }

        return view
    }

    private fun loaddata() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {

                try {
                    toolslist.clear()
                    for (item in snapshot.children) {
                        val toolname = item.child("Toolname").value.toString()
                        val toolimage = item.child("ToolImage").value.toString()
                        val useruid = item.child("UserUID").value.toString()
                        val toolid = item.child("ToolID").value.toString()
                        val date = item.child("Date").value.toString()

                        Redeletetool(date, toolimage, toolid)
                        toolslist.add(
                            Toolmodel(
                                toolid,
                                toolname,
                                toolimage,
                                useruid,
                                date
                            )
                        )
                    }
                    tooadapter =
                        tooladapter(toolslist, this@Tools)
                    tooadapter.notifyDataSetChanged()
                    if (tooadapter.itemCount == 0) {

                        toolrecyclerviiew.visibility = View.GONE
                        toolprogressBar.visibility = View.GONE
                        notoolsfound.text = getString(R.string.no_tools_available)
                        notoolsfound.visibility = View.VISIBLE
                    } else {
                        toolrecyclerviiew.adapter = tooadapter
                        toolprogressBar.visibility = View.GONE
                        toolrecyclerviiew.visibility = View.VISIBLE
                        notoolsfound.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, e.message.toString(), Toast.LENGTH_LONG).show()
                    toolprogressBar.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message.toString(), Toast.LENGTH_LONG).show()
                toolprogressBar.visibility = View.GONE

            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.fragmentmenu, menu)
        val searchItem = menu.findItem(R.id.search)

        val searchView: SearchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText.toString())
                return false
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun filter(text: String) {
        val filteredlist = ArrayList<Toolmodel>()


        for (item in toolslist) {
            if (item.Toolname!!.lowercase(Locale.getDefault())
                    .contains(text.lowercase(Locale.getDefault()))
            ) {
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()) {
            toolrecyclerviiew.visibility = View.GONE
            notoolsfound.visibility = View.VISIBLE
            notoolsfound.text = "\" $text \" no tools found"
            //Toast.makeText(context, getString(R.string.nodatafound), Toast.LENGTH_SHORT).show()
        } else {
            toolrecyclerviiew.visibility = View.VISIBLE
            notoolsfound.visibility = View.GONE
            tooadapter.filterlist(filteredlist, text)
        }

    }


    override fun OntoolItemclick(Tool: Toolmodel) {
        val intent = Intent(context, ToolDetails1::class.java)
        intent.putExtra("toolname", Tool.Toolname)
        intent.putExtra("toolimage", Tool.ToolImage)
        intent.putExtra("useruid", Tool.userUID)
        intent.putExtra("toolid", Tool.Toolid)
        intent.putExtra("toolDate", Tool.Date)
        startActivity(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!ConnectionManager().isconnected(requireContext())) {
            val snackbar = Snackbar.make(view, getString(R.string.nointernet), Snackbar.LENGTH_LONG)
            snackbar.setAction(getString(R.string.settings)) {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
            snackbar.show()
            toolprogressBar.visibility = View.GONE

        }
        super.onViewCreated(view, savedInstanceState)
    }

    @SuppressLint("SimpleDateFormat")
    private fun Redeletetool(tooldate: String, toolimage: String, toolid: String) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -6)
        val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val sdf1 = SimpleDateFormat("dd/MM/yyyy")
        val redate = sdf1.format(sdf.parse(cal.time.toString())!!)
        if (tooldate == redate) {
            val firebaseStorage =
                FirebaseStorage.getInstance().getReferenceFromUrl(toolimage)
            firebaseStorage.delete().addOnSuccessListener {
                databaseReference.child(toolid)
                    .removeValue().addOnSuccessListener {
                    }
            }
        }
    }
}