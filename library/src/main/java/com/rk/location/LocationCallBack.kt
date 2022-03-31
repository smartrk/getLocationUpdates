package com.rk.location

import android.location.Location

/**
 * Created by Rahul Kansara (Rk) on 29-Mar-22,  rahul.kansara10@gmail.com
 */
interface LocationCallBack {
    fun  callBack(location: Location)
    fun  gpsRequestDeniedCallBack()
    fun  permissionRequestDeniedCallBack()
}