package com.shyampatel.database.geofence

import androidx.room.TypeConverter
import com.shyampatel.core.common.geofence.GeofenceEvent
import java.util.Date

class GeofenceEventTypeConverters {
  @TypeConverter
  fun toGeofenceEvent(value: Int): GeofenceEvent {
    return when (value) {
      GeofenceEvent.TRANSITION_ENTER.value -> GeofenceEvent.TRANSITION_ENTER
      GeofenceEvent.TRANSITION_EXIT.value -> GeofenceEvent.TRANSITION_EXIT
      GeofenceEvent.TRANSITION_DWELL.value -> GeofenceEvent.TRANSITION_DWELL
      else -> throw IllegalArgumentException("Invalid value: $value")
    }
  }

  @TypeConverter
  fun fromGeofenceEvent(value: GeofenceEvent): Int {
    return value.value
  }
}