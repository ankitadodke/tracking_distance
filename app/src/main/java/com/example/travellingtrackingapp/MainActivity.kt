package com.example.travellingtrackingapp
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var lastLocation: Location? = null
    private var totalDistanceInMeters = 0.0

    private lateinit var distanceTextView: TextView
    private lateinit var startTrackingButton: Button
    private lateinit var stopTrackingButton: Button

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        distanceTextView = findViewById(R.id.distanceTextView)
        startTrackingButton = findViewById(R.id.startTrackingButton)
        stopTrackingButton = findViewById(R.id.stopTrackingButton)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        startTrackingButton.setOnClickListener {
            requestLocationPermission()
        }

        stopTrackingButton.setOnClickListener {
            stopLocationTracking()
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            startLocationTracking()
        }
    }

    private fun startLocationTracking() {
        val locationRequest = LocationRequest.create().apply {
            interval = 5000 // 5 seconds
            fastestInterval = 2000 // 2 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return

                for (location in locationResult.locations) {
                    lastLocation?.let {
                        totalDistanceInMeters += it.distanceTo(location)
                    }
                    lastLocation = location
                }

                val totalDistanceInKm = totalDistanceInMeters / 1000
                updateUIDistance(totalDistanceInKm)
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Handle permission request
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun stopLocationTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun updateUIDistance(distanceInKm: Double) {
        distanceTextView.text = "Distance Traveled: %.2f km".format(distanceInKm)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationTracking()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationTracking()
    }
}
