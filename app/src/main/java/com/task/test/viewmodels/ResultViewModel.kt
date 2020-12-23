package com.task.test.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.task.test.MyApp.Companion.instance
import com.task.test.model.direction.Direction
import com.task.test.networking.Api
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class ResultViewModel : ViewModel(), CoroutineScope {

    override var coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()
    private var mutableLiveData: MutableLiveData<Direction> = MutableLiveData()
    private val errors = MutableLiveData<Throwable>()
    private var job: Job? = null

    @Inject lateinit var api: Api

    init {
        instance.appComponent.injectResultViewModel(this)
    }

    fun getErrors(): LiveData<Throwable?>? {
        return errors
    }

    fun clearErrors() {
        errors.value = null
    }

    fun getDirections(): LiveData<Direction> {
        return mutableLiveData
    }

    // Пасхалка С новым годом Хо хо хо!

    fun getDirections(
        origin: String,
        destination: String,
        sensor: Boolean,
        mode: String,
        key: String
    ) {
        job = launch {
            try {
                val response = api.getDirectionAsync(origin, destination, sensor, mode, key).await()
                val direction = response.body()
                launch(Dispatchers.Main) {
                    mutableLiveData.value = direction
                }
            } catch (e: Exception) {
                errors.value = e
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }

}