package com.rk.locationutils

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.rk.location.LocationCallBack
import com.rk.location.LocationUtils

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        LocationUtils.init(this)

        findViewById<Button>(R.id.btn).setOnClickListener {
            LocationUtils.getCurrentLocation(object : LocationCallBack {
                override fun callBack(location: Location) {
                    findViewById<TextView>(R.id.locationTv).text =
                        "Lattitude :  ${location.latitude}\nLongitude : ${location.longitude}"
                }

                override fun gpsRequestDeniedCallBack() {

                }

                override fun permissionRequestDeniedCallBack() {
                }
            })
        }

    }


    fun makeToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}