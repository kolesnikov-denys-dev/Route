package com.task.test.util

import android.graphics.Rect
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds


class MapUtils {
    companion object {

        fun decodePolyline(encoded: String): List<LatLng> {
            val poly = ArrayList<LatLng>()
            var index = 0
            val len = encoded.length
            var lat = 0
            var lng = 0

            while (index < len) {
                var b: Int
                var shift = 0
                var result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lat += dlat

                shift = 0
                result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lng += dlng

                val latLng = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
                poly.add(latLng)
            }
            return poly
        }

        fun computeCameraView(
            items: List<LatLng?>,
            map: GoogleMap, padding: Rect,
            viewWidth: Int, viewHeight: Int
        ): CameraUpdate? { // Compute bounds without padding
            val i = items.iterator()
            val builder = LatLngBounds.builder()
            while (i.hasNext()) {
                builder.include(i.next())
            }
            var bounds = builder.build()
            // Create a first CameraUpdate and apply
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100)
            map.moveCamera(cameraUpdate)
            // Convert padding to lat/lng based on current projection
            bounds = map.projection.visibleRegion.latLngBounds
            val mapWidth = bounds.northeast.longitude - bounds.southwest.longitude
            val mapHeight = bounds.northeast.latitude - bounds.southwest.latitude
            val pixelToLng = mapWidth / viewWidth
            val pixelToLat = mapHeight / viewHeight
            padding.top = ((padding.top * pixelToLat).toInt())
            padding.bottom = ((padding.bottom * pixelToLat).toInt())
            padding.left = ((padding.left * pixelToLng).toInt())
            padding.right = ((padding.right * pixelToLng).toInt())
            // Now padding holds insets in lat/lng values.
            // Let's create two fake points and bound
            val northEast = LatLng(
                bounds.northeast.latitude + padding.top,
                bounds.northeast.longitude + padding.right
            )
            val southWest = LatLng(
                bounds.southwest.latitude - padding.bottom,
                bounds.southwest.longitude - padding.left
            )
            val newBounds = LatLngBounds.Builder()
                .include(northEast).include(southWest).build()
            return CameraUpdateFactory.newLatLngBounds(newBounds, 100)
        }
    }

}