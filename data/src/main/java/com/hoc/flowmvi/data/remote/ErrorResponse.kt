package com.hoc.flowmvi.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ErrorResponse(
  @Json(name = "statusCode")
  val statusCode: Int, // 404
  @Json(name = "error")
  val error: String? = null, // Not Found
  @Json(name = "message")
  val message: String // Cannot GET /23
)
