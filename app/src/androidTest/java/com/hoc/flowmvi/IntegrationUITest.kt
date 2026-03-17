package com.hoc.flowmvi

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.hoc.flowmvi.ui.main.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for end-to-end user flows.
 * 
 * Tests complete user journeys:
 * - Navigate to Add, fill form, return to main
 * - Navigate to Search, perform search, return to main
 * - Multiple navigation flows
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class IntegrationUITest {

  @get:Rule
  val activityRule = ActivityScenarioRule(MainActivity::class.java)

  @Before
  fun setUp() {
    Intents.init()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun endToEndFlow_addUser() {
    // Start from MainActivity
    onView(withId(com.hoc.flowmvi.ui.main.R.id.usersRecycler))
      .check(matches(isDisplayed()))

    // Navigate to Add activity
    openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
    onView(withText("Add"))
      .perform(click())

    // Fill the form in AddActivity
    onView(withId(com.hoc.flowmvi.ui.add.R.id.emailEditText))
      .perform(click())
      .perform(typeText("integration@test.com"))

    onView(withId(com.hoc.flowmvi.ui.add.R.id.firstNameEditText))
      .perform(click())
      .perform(typeText("Integration"))

    onView(withId(com.hoc.flowmvi.ui.add.R.id.lastNameEditText))
      .perform(click())
      .perform(typeText("Test"))
      .perform(closeSoftKeyboard())

    // Note: We don't submit the form as it would require network/backend setup
    // Instead we just verify the form can be filled and navigate back

    // Navigate back to MainActivity
    pressBack()

    // Verify we're back at MainActivity
    onView(withId(com.hoc.flowmvi.ui.main.R.id.usersRecycler))
      .check(matches(isDisplayed()))
  }

  @Test
  fun endToEndFlow_searchUser() {
    // Start from MainActivity
    onView(withId(com.hoc.flowmvi.ui.main.R.id.usersRecycler))
      .check(matches(isDisplayed()))

    // Navigate to Search activity
    openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
    onView(withText("Search"))
      .perform(click())

    // Perform a search
    onView(withId(androidx.appcompat.R.id.search_src_text))
      .perform(click())
      .perform(typeText("test query"))
      .perform(pressImeActionButton())

    // Verify search results area is displayed
    onView(withId(com.hoc.flowmvi.ui.search.R.id.usersRecycler))
      .check(matches(isDisplayed()))

    // Navigate back to MainActivity
    pressBack()

    // Verify we're back at MainActivity
    onView(withId(com.hoc.flowmvi.ui.main.R.id.usersRecycler))
      .check(matches(isDisplayed()))
  }

  @Test
  fun multipleNavigation_addThenSearch() {
    // Navigate to Add
    openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
    onView(withText("Add"))
      .perform(click())

    // Verify we're in AddActivity
    onView(withId(com.hoc.flowmvi.ui.add.R.id.addButton))
      .check(matches(isDisplayed()))

    // Go back
    pressBack()

    // Navigate to Search
    openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
    onView(withText("Search"))
      .perform(click())

    // Verify we're in SearchActivity
    onView(withId(androidx.appcompat.R.id.search_src_text))
      .check(matches(isDisplayed()))

    // Go back to main
    pressBack()

    // Verify we're back at MainActivity
    onView(withId(com.hoc.flowmvi.ui.main.R.id.usersRecycler))
      .check(matches(isDisplayed()))
  }

  @Test
  fun mainActivity_swipeRefresh_integration() {
    // Verify initial state
    onView(withId(com.hoc.flowmvi.ui.main.R.id.swipeRefreshLayout))
      .check(matches(isDisplayed()))

    // Perform swipe to refresh
    onView(withId(com.hoc.flowmvi.ui.main.R.id.swipeRefreshLayout))
      .perform(swipeDown())

    // Verify the layout is still functional after refresh
    onView(withId(com.hoc.flowmvi.ui.main.R.id.usersRecycler))
      .check(matches(isDisplayed()))

    // Verify menu is still accessible after refresh
    openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
    onView(withText("Add"))
      .check(matches(isDisplayed()))

    // Close menu by pressing back
    pressBack()
  }
}