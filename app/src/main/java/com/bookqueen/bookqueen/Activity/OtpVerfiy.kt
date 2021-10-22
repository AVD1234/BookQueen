package com.bookqueen.bookqueen.Activity


import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bookqueen.bookqueen.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.database.*
import java.util.concurrent.TimeUnit


class OtpVerfiy : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var verify: Button
    lateinit var resendotp: TextView
    lateinit var numbertext: TextView
    lateinit var progressBar: ProgressBar
    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var number: String
    lateinit var otp: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verfiy)
        auth = FirebaseAuth.getInstance()
        verify = findViewById(R.id.btnverify)
        resendotp = findViewById(R.id.resendotp)
        progressBar = findViewById(R.id.progressBar)
        numbertext = findViewById(R.id.intnumber)
        otp = findViewById(R.id.edtotp)
        progressBar.visibility = View.GONE
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("Users")

        val mobileNumber = intent.getStringExtra("mobilenumber")
        number = "+91" + mobileNumber.toString().trim()
        numbertext.text = number


        verify.setOnClickListener {
            //val otp = findViewById<EditText>(R.id.edtotp).text.toString().trim()
            if (TextUtils.isEmpty(otp.text.toString())) {
                otp.error = getString(R.string.enterotp)
            } else if (otp.length() > 0 || otp.length() == 4) {
                progressBar.visibility = View.VISIBLE
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    storedVerificationId.toString(), otp.text.toString().trim()
                )
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(this, getString(R.string.invalidotp), Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
        }
        resendotp.setOnClickListener {

            resendcodeforverification(number, resendToken)
            Toast.makeText(this, getString(R.string.otpsent), Toast.LENGTH_SHORT).show()
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(applicationContext, getString(R.string.verificationfailed), Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                storedVerificationId = verificationId
                resendToken = token

            }
        }

        sendVerificationcode(number)

    }

    private fun sendVerificationcode(number: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendcodeforverification(
        phone: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {


        val option = PhoneAuthOptions.newBuilder()
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .setForceResendingToken(token!!)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(option)


    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.signinsuccess), Toast.LENGTH_SHORT).show()
                    databaseReference.orderByChild("Phone").equalTo(number)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.value != null) {
                                    startActivity(
                                        Intent(
                                            applicationContext,
                                            MainActivity::class.java
                                        )
                                    )
                                    progressBar.visibility = View.GONE
                                    finish()
                                } else {

                                    startActivity(
                                        Intent(
                                            applicationContext,
                                            SaveProfile::class.java
                                        )
                                    )
                                    progressBar.visibility = View.GONE
                                    finish()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@OtpVerfiy, error.message, Toast.LENGTH_SHORT)
                                    .show()
                            }

                        })
//                    startActivity(Intent(applicationContext, MainActivity::class.java))
//                    progressBar.visibility = View.GONE
//                    finish()

                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, getString(R.string.invalidotp), Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.GONE
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE

            }
    }

}