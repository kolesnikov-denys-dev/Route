package com.task.test.model.direction


import com.google.gson.annotations.SerializedName

data class EndLocationX(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lng")
    val lng: Double
)