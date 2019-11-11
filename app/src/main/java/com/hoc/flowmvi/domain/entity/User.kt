package com.hoc.flowmvi.domain.entity

data class User(
  val id: Int,
  val email: String,
  val firstName: String,
  val lastName: String,
  val avatar: String
)