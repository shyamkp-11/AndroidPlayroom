package com.shyampatel.githubplayroom.util

import android.animation.TimeInterpolator
import androidx.compose.animation.core.Easing

fun TimeInterpolator.toEasing() = Easing {
    x -> getInterpolation(x) 
}