package com.rk.location

import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.tasks.CancellationTokenSource
import com.permissionx.guolindev.PermissionX
import com.rk.location.dialog.AlertDialog

/**
 * Created by Rahul Kansara (Rk) on 30-Mar-22,  rahul.kansara10@gmail.com
 */
object LocationUtils {

    var mainLocationCallBack: LocationCallBack? = null
    var activity: AppCompatActivity? = null

    private var resolutionForResult: ActivityResultLauncher<IntentSenderRequest>? = null

    fun init(activity: AppCompatActivity) {
        this.activity = activity
        if (resolutionForResult == null) {
            resolutionForResult =
                activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
                    when (activityResult.resultCode) {
                        AppCompatActivity.RESULT_OK -> {
                            mainLocationCallBack?.let { getCurrentLocation(it) }
                        }
                        AppCompatActivity.RESULT_CANCELED -> {
                            dialogDialog(
                                "Please turn on gps to continue.",
                                "Turn On",
                                "Cancel",
                            ) { isPositive ->
                                if (isPositive) {
                                    mainLocationCallBack?.let { getCurrentLocation(it) }
                                } else {
//                                    activity.onBackPressed()
                                    mainLocationCallBack?.gpsRequestDeniedCallBack()
                                }
                            }
                        }
                        else -> {
//                    makeToast("else")
                        }
                    }
                }
        }

    }

    fun getCurrentLocation(locationCallBack: LocationCallBack) {
        mainLocationCallBack = locationCallBack


            if (ActivityCompat.checkSelfPermission(
                    activity!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    activity!!,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                getPermissions(
                    arrayListOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) {
                    getCurrentLocation(locationCallBack)
                }
            } else {
                if (checkGPSStatus()) {

                    val cancellationTokenSource = CancellationTokenSource()

                    LocationServices.getFusedLocationProviderClient(activity!!).getCurrentLocation(
                        LocationRequest.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.token
                    ).addOnSuccessListener { location: Location? ->
                        location?.let {
                            locationCallBack.callBack(it)
                        }
                    }.addOnFailureListener {
                    }.addOnCanceledListener {
                    }

                } else {
                    val locationRequest = LocationRequest.create()

                    val builder =
                        LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

                    val settingsClient = LocationServices.getSettingsClient(activity!!)
                    val task = settingsClient.checkLocationSettings(builder.build())

                    task.addOnSuccessListener(activity!!) {
                        getCurrentLocation(locationCallBack)
                    }

                    task.addOnFailureListener(activity!!) { e ->
                        if (e is ResolvableApiException) {
                            try {
//                                e.startResolutionForResult(activity, 234)
                                val intentSenderRequest =
                                    IntentSenderRequest.Builder(e.resolution).build()
                                resolutionForResult?.launch(intentSenderRequest)
                            } catch (e1: IntentSender.SendIntentException) {
                                e1.printStackTrace()
                            }
                        }
                    }
                }

            }

    }


    private fun getPermissions(array: List<String>, permissionGranted: () -> Unit) {
        PermissionX.init(activity!!)
            .permissions(array)
            .request { allGranted, _, deniedList ->
                if (allGranted) {
                    permissionGranted()
                } else {
                    mainLocationCallBack?.permissionRequestDeniedCallBack()
                }
            }
    }


    private fun checkGPSStatus(): Boolean {
        val manager =
            activity!!.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun dialogDialog(
        message: String,
        positiveText: String,
        negativeText: String,
        callback: (isPositive: Boolean) -> Unit
    ) {
        try {
            AlertDialog.newInstance(message, positiveText, negativeText, false, callback)
                .show(activity!!.supportFragmentManager, AlertDialog::class.java.simpleName)
        } catch (e: Exception) {

        }
    }

}