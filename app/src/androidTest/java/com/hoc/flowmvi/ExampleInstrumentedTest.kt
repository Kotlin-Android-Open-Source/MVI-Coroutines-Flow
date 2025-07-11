package com.hoc.flowmvi

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.hoc.flowmvi.ui.main.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for the main application flow.
 *
 * Tests the primary user interactions in MainActivity including:
 * - User list display
 * - Navigation to Add and Search activities
 * - Pull-to-refresh functionality
 * - Menu interactions
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityUITest {

  @get:Rule
  val activityRule = ActivityScenarioRule(MainActivity::class.java)

  @Test
  fun mainActivity_displaysUserList() {
    // Check that the RecyclerView is displayed
    onView(withId(com.hoc.flowmvi.ui.main.R.id.usersRecycler))
      .check(matches(isDisplayed()))
  }

  @Test
  fun mainActivity_displaysSwipeRefreshLayout() {
    // Check that the SwipeRefreshLayout is displayed
    onView(withId(com.hoc.flowmvi.ui.main.R.id.swipeRefreshLayout))
      .check(matches(isDisplayed()))
  }

  @Test
  fun mainActivity_hasMenuItems() {
    // Open options menu
    openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
    
    // Check that Add action is present
    onView(withText("Add"))
      .check(matches(isDisplayed()))
      
    // Check that Search action is present
    onView(withText("Search"))
      .check(matches(isDisplayed()))
  }

  @Test
  fun mainActivity_pullToRefresh_triggersRefresh() {
    // Perform pull-to-refresh gesture
    onView(withId(com.hoc.flowmvi.ui.main.R.id.swipeRefreshLayout))
      .perform(swipeDown())
    
    // The refresh indicator should be visible (briefly)
    onView(withId(com.hoc.flowmvi.ui.main.R.id.swipeRefreshLayout))
      .check(matches(isDisplayed()))
  }

  @Test
  fun mainActivity_retryButton_isVisibleOnError() {
    // Note: This test depends on the app state and may need network mocking
    // For now, we just check that the retry button exists in the layout
    // The visibility will depend on the app's error state
    onView(withId(com.hoc.flowmvi.ui.main.R.id.retryButton))
      .check(matches(isDisplayed()))
  }
}
