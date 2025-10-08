package com.hoc.flowmvi.core_ui

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.core.content.IntentCompat
import androidx.core.os.BundleCompat

/**
 * Wrapper around [IntentCompat.getParcelableExtra] for type-safe parcelable retrieval.
 *
 * @see IntentCompat.getParcelableExtra
 */
inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? =
  IntentCompat.getParcelableExtra(this, key, T::class.java)

/**
 * Wrapper around [BundleCompat.getParcelable] for type-safe parcelable retrieval.
 *
 * @see BundleCompat.getParcelable
 */
inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? =
  BundleCompat.getParcelable(this, key, T::class.java)
