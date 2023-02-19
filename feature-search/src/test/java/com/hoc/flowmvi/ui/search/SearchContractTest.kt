package com.hoc.flowmvi.ui.search

import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.test_utils.rightValueOrThrow
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchContractTest {
  @Test
  fun test_userItem_equals() {
    assertEquals(
      UserItem.from(createUser()),
      UserItem.from(createUser())
    )
  }

  @Test
  fun test_userItem_hashCode() {
    assertEquals(
      UserItem.from(createUser()).hashCode(),
      UserItem.from(createUser()).hashCode()
    )
  }

  @Test
  fun test_userItem_properties() {
    val item = UserItem.from(createUser())
    assertEquals(ID, item.id)
    assertEquals(EMAIL, item.email)
    assertEquals(AVATAR, item.avatar)
    assertEquals("$FIRST_NAME $LAST_NAME", item.fullName)
  }

  private companion object {
    private const val ID = "0"
    private const val EMAIL = "test@gmail.com"
    private const val AVATAR = "avatar.png"
    private const val FIRST_NAME = "first"
    private const val LAST_NAME = "last"

    private fun createUser(): User = User.create(
      id = ID,
      email = EMAIL,
      avatar = AVATAR,
      firstName = FIRST_NAME,
      lastName = LAST_NAME
    ).rightValueOrThrow
  }
}
