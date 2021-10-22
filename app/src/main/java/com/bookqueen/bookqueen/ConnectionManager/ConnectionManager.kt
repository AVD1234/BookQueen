package com.bookqueen.bookqueen.ConnectionManager

import android.content.Context
import android.net.ConnectivityManager

class ConnectionManager {
     fun isconnected(context: Context): Boolean {
        val connectionmanager: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkinfo = connectionmanager.activeNetworkInfo
        return networkinfo != null && networkinfo.isConnectedOrConnecting
    }
}