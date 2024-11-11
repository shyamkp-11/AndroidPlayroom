package com.shyampatel.geofenceplayroom.utils

import android.graphics.Point
import android.location.Location
import com.google.android.gms.maps.Projection
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt


const val RADIUS_OF_EARTH_METERS = 6371009.0


fun calculateLocationAtADistance(location: LatLng, distance: Double, tc: Double = 0.0): LatLng {
    val distance = distance / 6371000
    val lat: Double = asin((sin(Math.toRadians(location.latitude)) * cos(distance)) + (cos(Math.toRadians(location.latitude)) * sin(distance) * cos(tc)))

    val lon: Double = if (cos(lat) == 0.0) {
        Math.toRadians(location.longitude) // endpoint at pole
    } else {
        (Math.toRadians(location.longitude) - asin(sin(tc) * sin(distance) / cos(lat)) + PI) % (2 * PI) - PI;
    }
    return LatLng(Math.toDegrees(lat), Math.toDegrees(lon * (180 / PI)))
}

fun calculateScreenWidthDistance(latLngBounds: LatLngBounds): Double {
    val result = FloatArray(1)
    val midway = (latLngBounds.northeast.latitude + latLngBounds.southwest.latitude) / 2.0
    Location.distanceBetween(
        midway,
        latLngBounds.northeast.longitude,
        midway,
        latLngBounds.southwest.longitude,
        result
    )
    return result[0].toDouble()
}

fun getCircleRadiusMarkerPoint(graphicPoint: Point, viewWidth: Int, viewHeight: Int): Point {
    graphicPoint[graphicPoint.x + (viewWidth * 1 / 4)] = graphicPoint.y
    return graphicPoint
}

fun getCircleRadiusMarkerLatLng(
    projection: Projection,
    center: LatLng,
    viewWidth: Int,
    viewHeight: Int
): LatLng {
    val graphicPoint = projection.toScreenLocation(center)
    graphicPoint.set(graphicPoint.x + (viewWidth * 1 / 4), graphicPoint.y)
    return projection.fromScreenLocation(graphicPoint)
}

fun getCircleBounds(center: LatLng, radius: Double): LatLngBounds {
    val targetNorthEast: LatLng = computeOffset(center, radius * sqrt(2.0), 45.0)
    val targetSouthWest: LatLng = computeOffset(center, radius * sqrt(2.0), 225.0)
    return LatLngBounds(targetSouthWest, targetNorthEast)
}

fun getMidPoint(latLng1: LatLng, latLng2: LatLng): LatLng {
    return LatLngBounds.builder().include(latLng1).include(latLng2).build().center;
}

fun getZoomLevel(radius: Double): Float {
    val scale = radius / 200
    return ((16 - ln(scale) / ln(2.0)).toFloat())
}

fun getSnapshotZoomLevel(radius: Double): Float {
    val scale = radius / 100
    return ((16 - ln(scale) / ln(2.0)).toFloat())
}

fun angleFromCoordinate(
   latLng1: LatLng, latLng2: LatLng
): Double {
    val lat1 = Math.toRadians(latLng1.latitude)
    val long1 = Math.toRadians(latLng1.longitude)
    val lat2 = Math.toRadians(latLng2.latitude)
    val long2 = Math.toRadians(latLng2.longitude)

    val dLon = (long2 - long1)

    val y = sin(dLon) * cos(lat2)
    val x =
        cos(lat1) * sin(lat2) - sin(lat1) * cos(
            lat2
        ) * cos(dLon)

    var brng = atan2(y, x)

    brng = Math.toDegrees(brng)
    brng = (brng + 360) % 360
//    brng = 360 - brng // count degrees counter-clockwise - remove to make clockwise
    return brng
}


/**
 * Returns the LatLng resulting from moving a distance from an origin
 * in the specified heading (expressed in degrees clockwise from north).
 *
 * @param from     The LatLng from which to start.
 * @param distance The distance to travel.
 * @param heading  The heading in degrees clockwise from north.
 */
fun computeOffset(from: LatLng, distance: Double, heading: Double): LatLng {
    var distance = distance
    var heading = heading
    distance /= RADIUS_OF_EARTH_METERS
    heading = Math.toRadians(heading)
    // http://williams.best.vwh.net/avform.htm#LL
    val fromLat: Double = Math.toRadians(from.latitude)
    val fromLng: Double = Math.toRadians(from.longitude)
    val cosDistance = cos(distance)
    val sinDistance = sin(distance)
    val sinFromLat = sin(fromLat)
    val cosFromLat = cos(fromLat)
    val sinLat = cosDistance * sinFromLat + sinDistance * cosFromLat * cos(heading)
    val dLng: Double = atan2(
        sinDistance * cosFromLat * sin(heading),
        cosDistance - sinFromLat * sinLat
    )
    return LatLng(Math.toDegrees(asin(sinLat)), Math.toDegrees(fromLng + dLng))
}


/**
 * Returns the distance between two LatLngs, in meters.
 */
fun computeDistanceBetween(from: LatLng, to: LatLng): Double {
    fun arcHav(x: Double): Double {
        return 2 * asin(sqrt(x))
    }

    fun hav(x: Double): Double {
        val sinHalf = sin(x * 0.5)
        return sinHalf * sinHalf
    }

    /**
     * Returns hav() of distance from (lat1, lng1) to (lat2, lng2) on the unit sphere.
     */
    fun havDistance(lat1: Double, lat2: Double, dLng: Double): Double {
        return hav(lat1 - lat2) + hav(dLng) * cos(lat1) * cos(lat2)
    }
    /**
     * Returns distance on the unit sphere; the arguments are in radians.
     */
    fun distanceRadians(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        return arcHav(havDistance(lat1, lat2, lng1 - lng2))
    }
    /**
     * Returns the angle between two LatLngs, in radians. This is the same as the distance
     * on the unit sphere.
     */
    fun computeAngleBetween(from: LatLng, to: LatLng): Double {
        return distanceRadians(
            Math.toRadians(from.latitude), Math.toRadians(from.longitude),
            Math.toRadians(to.latitude), Math.toRadians(to.longitude)
        )
    }
    return computeAngleBetween(from, to) * RADIUS_OF_EARTH_METERS
}

fun getDistanceString(distanceMeters: Double): String {
    val miles = distanceMeters * 0.000621371

    if(miles <  0.17) {
        val feet = distanceMeters * 3.28084
        return "${feet.round(1)} feet"
    }
    if (miles < 0.5) {
        val yards = distanceMeters * 1.09361
        return "${yards.round(1)} yards"
    }
    return "${miles.round(1)} miles"
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}