package com.hoc.flowmvi.core_ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable

/**
 * https://stackoverflow.com/a/73311814/11191424
 */
inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? =
  // TODO: Use `>`, because https://issuetracker.google.com/issues/240585930#comment6
  if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) getParcelableExtra(key, T::class.java)
  else @Suppress("DEPRECATION") getParcelableExtra(key)

/**
 * https://stackoverflow.com/a/73311814/11191424
 */
inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? =
  // TODO: Use `>`, because https://issuetracker.google.com/issues/240585930#comment6
  if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) getParcelable(key, T::class.java)
  else @Suppress("DEPRECATION") getParcelable(key)
