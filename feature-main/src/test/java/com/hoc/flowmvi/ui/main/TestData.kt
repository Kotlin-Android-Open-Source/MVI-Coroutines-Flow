package com.hoc.flowmvi.ui.main

import arrow.core.valueOr
import com.hoc.flowmvi.domain.model.User

internal val USERS = listOf(
  User.create(
    id = "1",
    email = "email1@gmail.com",
    firstName = "first1",
    lastName = "last1",
    avatar = "1.png"
  ),
  User.create(
    id = "2",
    email = "email1@gmail.com",
    firstName = "first2",
    lastName = "last2",
    avatar = "2.png"
  ),
  User.create(
    id = "3",
    email = "email1@gmail.com",
    firstName = "first3",
    lastName = "last3",
    avatar = "3.png"
  ),
).map { validated -> validated.valueOr { error("$it") } }

internal val USER_ITEMS = USERS.map(::UserItem)
