package com.task.test.activities

import android.annotation.SuppressLint
import android.graphics.Rect
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.task.test.R
import com.task.test.model.LocationWithName
import com.task.test.model.direction.Direction
import com.task.test.util.Constants.GOOGLE_MAPS_API_KEY
import com.task.test.util.Constants.KEY_FROM
import com.task.test.util.Constants.KEY_TO
import com.task.test.util.Constants.MODE_WALKING
import com.task.test.util.MapUtils
import com.task.test.viewmodels.ResultViewModel


class ResultActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var from: LocationWithName
    private lateinit var to: LocationWithName
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewModel: ResultViewModel

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        viewModel = ViewModelProvider(this).get(ResultViewModel::class.java)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        from = intent.getParcelableExtra(KEY_FROM) as LocationWithName
        to = intent.getParcelableExtra(KEY_TO) as LocationWithName
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        viewModel.getDirections(
            "${from.position.latitude},${from.position.longitude}",
            "${to.position.latitude},${to.position.longitude}",
            false,
            MODE_WALKING,
            GOOGLE_MAPS_API_KEY
        )

        viewModel.getErrors()?.observe(this, Observer {
            if (it != null) {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                viewModel.clearErrors()
            }
        })

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                viewModel.getDirections().observe(this, Observer { data ->
                    updateMapContents(
                        from.position, to.position,
                        LatLng(location!!.latitude, location.longitude), data
                    )
                })
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    private fun updateMapContents(
        from: LatLng,
        to: LatLng,
        userLocation: LatLng,
        respObj: Direction
    ) {
        val lineoption = PolylineOptions()
        val builder = LatLngBounds.Builder()
        builder.include(from)
        builder.include(to)
        builder.include(userLocation)
        mMap.clear()
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 0))
        val item = respObj.routes[0]
        val result = ArrayList<List<LatLng>>()
        val points = arrayListOf<LatLng>()
        val path = ArrayList<LatLng>()
        for (element in item.legs[0].steps) {
            path.addAll(MapUtils.decodePolyline(element.polyline.points))
        }
        result.add(path)
        item.legs[0].steps.size
        for (i in item.legs[0].steps.indices) {
            val obj = item.legs[0].steps[i]
            points.add(LatLng(obj.startLocation.lat, obj.startLocation.lng))
            points.add(LatLng(obj.endLocation.lat, obj.endLocation.lng))
        }
        points.add(userLocation)
        for (i in result.indices) {
            lineoption.addAll(result[i])
            lineoption.width(7f)
            lineoption.color(getColor(R.color.black))
            lineoption.geodesic(false)
        }
        mMap.addPolyline(lineoption)
        mMap.addMarker(MarkerOptions().position(from))
        mMap.addMarker(MarkerOptions().position(to))
        val cameraUpdate = MapUtils.computeCameraView(
            points, mMap, Rect(100, 50, 50, 100),
            400, 250
        )
        mMap.moveCamera(cameraUpdate)
    }

}