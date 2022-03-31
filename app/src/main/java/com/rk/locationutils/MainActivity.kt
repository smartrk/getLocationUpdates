package com.rk.locationutils

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.rk.location.LocationCallBack
import com.rk.location.LocationUtils

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        LocationUtils.getCurrentLocation(this, object : LocationCallBack {
            override fun callBack(location: Location) {
                makeToast(location.latitude.toString())
            }
        }
        )
    }


    fun makeToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}