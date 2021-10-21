package com.hoc.flowmvi.data.remote

import com.squareup.moshi.Json

internal data class UserBody(
  @Json(name = "email")
  val email: String,
  @Json(name = "first_name")
  val firstName: String,
  @Json(name = "last_name")
  val lastName: String,
)
