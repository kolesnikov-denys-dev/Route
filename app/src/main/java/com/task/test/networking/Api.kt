package com.task.test.networking


import com.task.test.model.direction.Direction
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {

    @GET(Path.json)
    fun getDirectionAsync(
        @Query(Key.origin) origin: String,
        @Query(Key.destination) destination: String,
        @Query(Key.sensor) sensor: Boolean,
        @Query(Key.mode) mode: String,
        @Query(Key.key) key: String,
    ): Deferred<Response<Direction>>

    private object Key {
        const val origin = "origin"
        const val destination = "destination"
        const val sensor = "sensor"
        const val mode = "mode"
        const val key = "key"
    }

    private object Path {
        const val json = "json"
    }

}