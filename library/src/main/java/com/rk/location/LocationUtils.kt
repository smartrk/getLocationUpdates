package com.rk.location

import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.permissionx.guolindev.PermissionX
import com.rk.location.dialog.AlertDialog


/**
 * Created by Rahul Kansara (Rk) on 30-Mar-22,  rahul.kansara10@gmail.com
 */
class LocationUtils(private val activity: AppCompatActivity, private val continueUpdates : Boolean=false) {
    private var resolutionForResult: ActivityResultLauncher<IntentSenderRequest>? = null
    private var locationCallBack: LocationCallBack? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(activity)
    private var mainLocationCallback: LocationCallback? = null

    fun removeLocationUpdates() {
        mainLocationCallback?.let { fusedLocationProviderClient.removeLocationUpdates(it) }
    }


    init {
        resolutionForResult =
            activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
                when (activityResult.resultCode) {
                    AppCompatActivity.RESULT_OK -> {
                        locationCallBack.let {
                            it?.let { it1 ->
                                getCurrentLocation(
                                    it1
                                )
                            }
                        }
                    }
                    AppCompatActivity.RESULT_CANCELED -> {
                        dialogDialog(
                            "Please turn on gps to continue.",
                            "Turn On",
                            "Cancel",
                        ) { isPositive ->
                            if (isPositive) {
                                locationCallBack?.let {
                                    getCurrentLocation(
                                        it
                                    )
                                }
                            } else {
//                                    activity.onBackPressed()
                                locationCallBack?.gpsRequestDeniedCallBack()
                            }
                        }
                    }
                    else -> {
//                    makeToast("else")
                    }
                }
            }
    }


    fun getCurrentLocation(
        locationCallBac: LocationCallBack
    ) {
        locationCallBack = locationCallBac

        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            getPermissions(
                arrayListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ) {
                getCurrentLocation(locationCallBac)
            }
        } else {
            if (checkGPSStatus()) {
                /* val locationResult: Task<Location> =
                     fusedLocationProviderClient.lastLocation
                 locationResult.addOnCompleteListener {
                     if (it.isSuccessful) {
                         // Set the map's camera position to the current location of the device.
                         val lastKnownLocation = it.result
                         lastKnownLocation?.let {
                             locationCallBac.callBack(it)
                         }
                     }
                 }*/
                val locationRequest = LocationRequest.create()
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                locationRequest.interval = 20 * 1000
                mainLocationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        for (location in locationResult.locations) {
                            if (location != null) {
                                locationCallBac.callBack(location)
                            }
                        }
                    }
                }
                if (continueUpdates) {
                    fusedLocationProviderClient.requestLocationUpdates(
                        locationRequest,
                        mainLocationCallback!!, Looper.getMainLooper()
                    )
                } else {
                    fusedLocationProviderClient.lastLocation
                        .addOnSuccessListener(activity) { location ->
                            if (location != null) {
                                locationCallBac.callBack(location)
                            } else {
                                fusedLocationProviderClient.requestLocationUpdates(
                                    locationRequest,
                                    mainLocationCallback!!, Looper.getMainLooper()
                                )
                            }
                        }
                }


                /* val cancellationTokenSource = CancellationTokenSource()

                 LocationServices.getFusedLocationProviderClient(activity).getCurrentLocation(
                     LocationRequest.PRIORITY_HIGH_ACCURACY,
                     cancellationTokenSource.token
                 )
                     .addOnSuccessListener { location: Location? ->
                         location?.let {
                             locationCallBack.callBack(it)
                         }
                     }.addOnFailureListener {
                     }.addOnCanceledListener {
                     }
 */
            } else {
                val locationRequest = LocationRequest.create()

                val builder =
                    LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

                val settingsClient = LocationServices.getSettingsClient(activity)
                val task = settingsClient.checkLocationSettings(builder.build())

                task.addOnSuccessListener(activity) {
                    getCurrentLocation(locationCallBac)
                }


                task.addOnFailureListener(activity) { e ->
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
        PermissionX.init(activity)
            .permissions(array)
            .request { allGranted, _, _ ->
                if (allGranted) {
                    permissionGranted()
                } else {
                    locationCallBack?.permissionRequestDeniedCallBack()
                }
            }
    }


    private fun checkGPSStatus(): Boolean {
        val manager =
            activity.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun dialogDialog(
        message: String,
        positiveText: String,
        negativeText: String,
        callback: (isPositive: Boolean) -> Unit
    ) {
        try {
            AlertDialog.newInstance(message, positiveText, negativeText, false, callback)
                .show(activity.supportFragmentManager, AlertDialog::class.java.simpleName)
        } catch (e: Exception) {

        }
    }

}