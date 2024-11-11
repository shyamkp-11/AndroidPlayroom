package com.shyampatel.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

inline val Int.dp: Dp
    @Composable get() = with(LocalDensity.current) { this@dp.toDp() }

inline val Dp.px: Float
    @Composable get() = with(LocalDensity.current) { this@px.toPx() }