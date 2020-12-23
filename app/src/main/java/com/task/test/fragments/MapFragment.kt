package com.task.test.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.task.test.R
import com.task.test.content
import com.task.test.model.EpoxyHistoryModel
import com.task.test.model.LocationWithName
import com.task.test.util.Constants.EMPTY
import com.task.test.util.Constants.KEY_IS_FROM
import com.task.test.util.Constants.LOCATION_REQUEST_CODE
import com.task.test.util.Constants.MAX_RESULT
import com.task.test.viewmodels.MapFragmentViewModel
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.sheet_history.view.*


class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private var mMap: GoogleMap? = null
    private var mapFragment: SupportMapFragment? = null
    private var listOfAddress: MutableList<Address> = mutableListOf<Address>()
    private var listOfHistoryAddress = mutableListOf<EpoxyHistoryModel>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var dialog: BottomSheetDialog
    lateinit var dataPasser: OnDataPass
    private var isFrom = false
    private lateinit var currentPosition: LatLng

    private lateinit var viewModel: MapFragmentViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataPasser = context as OnDataPass
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(activity!!.applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel = ViewModelProvider(this).get(MapFragmentViewModel::class.java)

        viewModel.getSearchAddressesLiveData().observe(this.viewLifecycleOwner, Observer {
            listOfAddress = it as MutableList<Address>
            val item = listOfAddress[0]
            val addressStr = getStringFromAddress(item)
            listOfHistoryAddress.add(EpoxyHistoryModel(addressStr, item.latitude, item.longitude))
            showAutoCompleteList(listAddressToArrayAdapterString(listOfAddress))
            currentPosition =
                LatLng(item.latitude, item.longitude)
            addNewMarker(currentPosition, true)
            dataPasser.onDataPass(LocationWithName("", currentPosition, isFrom))
        })

        viewModel.getMapClickAddressesLiveData().observe(this.viewLifecycleOwner, Observer {
            listOfAddress = it as MutableList<Address>
            showAutoCompleteList(listAddressToArrayAdapterString(listOfAddress))
            val item = listOfAddress[0]
            val addressStr = getStringFromAddress(item)
            listOfHistoryAddress.add(EpoxyHistoryModel(addressStr, item.latitude, item.longitude))
            searchAutoCompleteTextView.setText(addressStr, false)
            dataPasser.onDataPass(LocationWithName(addressStr, currentPosition, isFrom))
        })

        viewModel.getErrors()?.observe(this.viewLifecycleOwner, Observer {
            if (it != null) {
                Toast.makeText(activity?.baseContext, it.localizedMessage, Toast.LENGTH_SHORT)
                    .show()
                viewModel.clearErrors()
            }
        })

        if (ActivityCompat.checkSelfPermission(
                activity!!.baseContext, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                activity!!.baseContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mapFragment?.getMapAsync(this)
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_REQUEST_CODE
            )
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                mMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(location!!.latitude, location.longitude), 15f
                    )
                )
            }

        myLocations.setOnClickListener { openBottomSheetDialog(listOfHistoryAddress) }
        arguments?.let { isFrom = it.getBoolean(KEY_IS_FROM) }

        searchRouteClick()
        autoCompleteTextItemClick()
    }

    private fun searchRouteClick() {
        search.setOnClickListener {
            viewModel.getLatLngFromAddress(
                searchAutoCompleteTextView.text.trim().toString(),
                MAX_RESULT
            )
        }
    }

    private fun autoCompleteTextItemClick() {
        searchAutoCompleteTextView.onItemClickListener =
            OnItemClickListener { _, _, position, _ ->
                currentPosition = LatLng(
                    listOfAddress[position].latitude,
                    listOfAddress[position].longitude
                )

                val item = listOfAddress[0]
                val addressStr = getStringFromAddress(item)
                listOfHistoryAddress.add(
                    EpoxyHistoryModel(
                        addressStr,
                        item.latitude,
                        item.longitude
                    )
                )
                searchAutoCompleteTextView.setText(addressStr, false)
                dataPasser.onDataPass(LocationWithName(addressStr, currentPosition, isFrom))
                addNewMarker(currentPosition, true)
            }
    }

    override fun onMapClick(position: LatLng) {
        currentPosition = position
        addNewMarker(currentPosition)
        searchAutoCompleteTextView.setText(EMPTY, false)
        viewModel.getAddressFromLatLng(currentPosition, MAX_RESULT)
    }

    private fun getStringFromAddress(address: Address): String {
        val knownName: String? = address.featureName ?: EMPTY
        val city: String? = address.locality ?: EMPTY
        val street: String? = address.thoroughfare ?: EMPTY
        return "$street $knownName $city"
    }

    private fun showAutoCompleteList(cities: Array<String>) {
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(
            activity!!.applicationContext,
            android.R.layout.simple_list_item_1,
            cities
        )
        searchAutoCompleteTextView.setAdapter(arrayAdapter)
        searchAutoCompleteTextView.showDropDown()
    }

    private fun addNewMarker(p0: LatLng, resize: Boolean = false) {
        val markerOptions = MarkerOptions()
        markerOptions.position(p0)
        markerOptions.title(p0.latitude.toString() + " : " + p0.longitude)
        mMap?.clear()
        if (resize) {
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(p0, 15f))
        }
        mMap?.addMarker(markerOptions)
    }

    private fun listAddressToArrayAdapterString(addresses: MutableList<Address>): Array<String> {
        val array = mutableListOf<String>()
        for (address in addresses) {
            array.add(getStringFromAddress(address))
        }
        return array.toTypedArray()
    }

    private fun openBottomSheetDialog(epoxyHistoryModels: MutableList<EpoxyHistoryModel>) {
        val view: View = layoutInflater.inflate(R.layout.sheet_history, null)
        dialog = BottomSheetDialog(context!!)
        dialog.setContentView(view)

        val closeImageView = view.findViewById<View>(R.id.closeImageView) as ImageView

        view.recyclerView.withModels {
            epoxyHistoryModels.forEachIndexed { position, model ->
                content {
                    id(position)
                    simpleModel(model)
                    onClickContent { _ ->
                        Toast.makeText(activity, model.name, Toast.LENGTH_SHORT).show()
                        val item = epoxyHistoryModels[position]
                        currentPosition = LatLng(item.lat, item.lon)
                        addNewMarker(currentPosition, true)
                        searchAutoCompleteTextView.setText(item.name, false)
                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.show()

        closeImageView.setOnClickListener {
            dialog.dismiss()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (mMap != null) {
            val permission = ContextCompat.checkSelfPermission(
                activity!!.baseContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            if (permission == PackageManager.PERMISSION_GRANTED) {
                mMap?.isMyLocationEnabled = true
                mMap?.setOnMapClickListener(this)
            } else {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_REQUEST_CODE)
            }
        }
    }

    private fun requestPermission(permissionType: String, requestCode: Int) {
        requestPermissions(arrayOf(permissionType), requestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        activity?.applicationContext,
                        getString(R.string.need_permissions),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    mapFragment?.getMapAsync(this)
                }
            }
        }
    }

}