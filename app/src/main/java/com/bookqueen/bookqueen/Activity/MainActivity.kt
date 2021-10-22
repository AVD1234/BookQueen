package com.bookqueen.bookqueen.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.bookqueen.bookqueen.R
import com.bookqueen.bookqueen.fragments.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar.*


class MainActivity : AppCompatActivity() {
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var navigationview: NavigationView
    private lateinit var firebaseAuth: FirebaseAuth

    var previousMenuItem:MenuItem?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseAuth= FirebaseAuth.getInstance()

        toolbar()

        toggle = ActionBarDrawerToggle(this, drawerlayout, R.string.open, R.string.close)
        drawerlayout.addDrawerListener(toggle)
        toggle.syncState()

        openhome()
        val headerview=navigationview.getHeaderView(0)
        val userphone=headerview.findViewById<TextView>(R.id.userphone)
        userphone.text=firebaseAuth.currentUser!!.phoneNumber.toString()

        naviview.setNavigationItemSelectedListener {

            if(previousMenuItem!=null){
                previousMenuItem?.isChecked=false
            }
            it.isCheckable=true
            it.isChecked=true
            previousMenuItem=it
            when (it.itemId) {
                R.id.home -> {
                    openhome()

                    drawerlayout.closeDrawers()
                }
                R.id.myitems -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.framelayout, Myitems())
                        .commit()
                    supportActionBar?.title = getString(R.string.myitems)
                    drawerlayout.closeDrawers()
                }
                R.id.myprofile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.framelayout, Myprofile())
                        .commit()
                    supportActionBar?.title = getString(R.string.myprofile)
                    drawerlayout.closeDrawers()
                }
                R.id.About_App -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.framelayout, Aboutus())
                        .commit()
                    supportActionBar?.title = getString(R.string.aboutus)
                    drawerlayout.closeDrawers()
                }
                R.id.contact_us -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.framelayout, Contactus())
                        .commit()
                    supportActionBar?.title = getString(R.string.contactus)
                    drawerlayout.closeDrawers()
                }
                R.id.share_app -> {
                    val intent=Intent("android.intent.action.SEND")
                    intent.type = "text/plain"
                    intent.putExtra("android.intent.extra.SUBJECT", "BookQueen")
                    intent.putExtra("android.intent.extra.TEXT", "https://play.google.com/store/apps/details?id=$packageName")
                    startActivity(Intent.createChooser(intent, "Share with..."))
                }
                R.id.logout -> {
                    firebaseAuth.signOut()
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    Toast.makeText(this,getString(R.string.signout),Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            true
        }

    }

    private fun openhome() {
        val fragment = Home()
        val transition = supportFragmentManager.beginTransaction()
        transition.replace(R.id.framelayout, fragment)
            .commit()
        navigationview = findViewById(R.id.naviview)
        navigationview.setCheckedItem(R.id.home)
        supportActionBar?.title = getString(R.string.BookQueen)
    }


    fun toolbar() {
        var toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.BookQueen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            drawerlayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        when (supportFragmentManager.findFragmentById(R.id.framelayout)) {
            !is Home -> openhome()

            else -> super.onBackPressed()
        }
    }

}