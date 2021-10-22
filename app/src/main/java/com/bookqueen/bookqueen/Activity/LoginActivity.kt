package com.bookqueen.bookqueen.Activity


import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.bookqueen.bookqueen.ConnectionManager.ConnectionManager
import com.bookqueen.bookqueen.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {


    lateinit var edtphonenumber: EditText
    private lateinit var sendotp: Button
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        edtphonenumber = findViewById(R.id.edtphone)
        sendotp = findViewById(R.id.btnsendotp)


        auth = FirebaseAuth.getInstance()


        if (auth.currentUser != null) {
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }

        sendotp.setOnClickListener {
            if (TextUtils.isEmpty(edtphonenumber.text.toString())) {
                edtphonenumber.error = getString(R.string.enterphone)
                return@setOnClickListener
            } else if (edtphonenumber.length() != 10) {
                edtphonenumber.error = getString(R.string.invalidphone)
                return@setOnClickListener
            } else if (!ConnectionManager().isconnected(this)) {
                val snackbar = Snackbar.make(it, getString(R.string.nointernet), Snackbar.LENGTH_LONG)
                snackbar.setAction(getString(R.string.settings)) {
                    startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                }
                snackbar.show()
            } else {
                val intent = Intent(this@LoginActivity, OtpVerfiy::class.java)
                intent.putExtra("mobilenumber", edtphonenumber.text.toString())
                startActivity(intent)
                finish()
            }
        }

    }
}


