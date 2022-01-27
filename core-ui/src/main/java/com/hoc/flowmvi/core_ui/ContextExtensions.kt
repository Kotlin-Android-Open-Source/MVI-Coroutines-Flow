package com.hoc.flowmvi.core_ui

import android.content.Context
import android.widget.Toast

@Suppress("NOTHING_TO_INLINE")
inline fun Context.toast(text: CharSequence) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

@Suppress("NOTHING_TO_INLINE")
inline fun Context.dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
