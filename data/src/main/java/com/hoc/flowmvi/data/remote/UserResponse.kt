package com.hoc.flowmvi.data.remote

import com.squareup.moshi.Json

internal data class UserResponse(
  @Json(name = "_id")
  val id: String,
  @Json(name = "email")
  val email: String,
  @Json(name = "first_name")
  val firstName: String,
  @Json(name = "last_name")
  val lastName: String,
  @Json(name = "avatar")
  val avatar: String
)
