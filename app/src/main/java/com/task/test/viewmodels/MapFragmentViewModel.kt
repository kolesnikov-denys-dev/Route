package com.task.test.viewmodels

import android.app.Application
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.task.test.model.direction.Direction
import com.task.test.networking.Retrofit
import com.task.test.util.MapUtils
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

class MapFragmentViewModel(application: Application) : AndroidViewModel(application),
    CoroutineScope {

    override var coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

    private var liveDataSearchAddresses: MutableLiveData<List<Address>> = MutableLiveData()
    private var liveDataMapClickAddresses: MutableLiveData<List<Address>> = MutableLiveData()
    private val context = getApplication<Application>().applicationContext
    private val errors = MutableLiveData<Throwable>()
    private var jobLatLng: Job? = null
    private var jobStr: Job? = null

    fun getErrors(): LiveData<Throwable?>? {
        return errors
    }

    fun clearErrors() {
        errors.value = null
    }

    fun getSearchAddressesLiveData(): LiveData<List<Address>> {
        return liveDataSearchAddresses
    }

    fun getMapClickAddressesLiveData(): LiveData<List<Address>> {
        return liveDataMapClickAddresses
    }

    fun getAddressFromLatLng(
        position: LatLng,
        maxResult: Int
    ) {
        jobLatLng = launch {
            try {
                val addresses: List<Address>
                val geocoder = Geocoder(context, Locale.getDefault())
                addresses =
                    geocoder.getFromLocation(position.latitude, position.longitude, maxResult)
                launch(Dispatchers.Main) {
                    liveDataMapClickAddresses.value = addresses
                }
            } catch (e: Exception) {
                errors.value = e
            }
        }
    }

    fun getLatLngFromAddress(
        address: String,
        maxResult: Int
    ) {
        jobStr = launch {
            try {
                val addresses: List<Address>
                val geocoder = Geocoder(context, Locale.getDefault())
                addresses = geocoder.getFromLocationName(address, maxResult)
                launch(Dispatchers.Main) {
                    liveDataSearchAddresses.value = addresses
                }
            } catch (e: Exception) {
                errors.value = e
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        jobLatLng?.cancel()
        jobStr?.cancel()
    }

}