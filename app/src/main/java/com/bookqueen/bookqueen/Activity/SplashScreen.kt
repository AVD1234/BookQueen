package com.bookqueen.bookqueen.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.bookqueen.bookqueen.R

class SplashScreen : AppCompatActivity() {

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val image=findViewById<ImageView>(R.id.bookimageView)
        val animation=AnimationUtils.loadAnimation(this@SplashScreen, R.anim.zoomin)
        image.startAnimation(animation)

        Handler().postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }

}