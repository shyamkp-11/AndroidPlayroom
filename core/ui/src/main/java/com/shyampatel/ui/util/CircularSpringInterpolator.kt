package com.shyampatel.ui.util

import android.view.animation.Interpolator
import androidx.compose.animation.core.Easing
import kotlin.math.sin

class CircularSpringInterpolator(private val tension: Float = 50f)
    : Interpolator {
    override fun getInterpolation(input: Float): Float {
        return (sin(tension * input) * sin(Math.PI * input) 
            + input).toFloat()
    }

}
internal fun CircularSpringInterpolatorEasing(tension: Float = 50f)
    : Easing = CircularSpringInterpolator(tension).toEasing()