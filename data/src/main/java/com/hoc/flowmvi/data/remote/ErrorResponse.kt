package com.hoc.flowmvi.data.remote

import com.squareup.moshi.Json

data class ErrorResponse(
  @Json(name = "statusCode")
  val statusCode: Int, // 404
  @Json(name = "error")
  val error: String? = null, // Not Found
  @Json(name = "message")
  val message: String, // Cannot GET /23
  @Json(name = "data")
  val data: Any? = null,
)
