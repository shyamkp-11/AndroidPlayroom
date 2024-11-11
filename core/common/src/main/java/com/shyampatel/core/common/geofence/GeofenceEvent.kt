package com.shyampatel.core.common.geofence

enum class GeofenceEvent(val value: Int){
    TRANSITION_ENTER(1), TRANSITION_EXIT(2), TRANSITION_DWELL(3);

    companion object {
        fun valueOf(value: Int) = entries.find { it.value == value }
    }
}