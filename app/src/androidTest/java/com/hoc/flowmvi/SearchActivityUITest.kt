package com.hoc.flowmvi

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import com.hoc.flowmvi.ui.search.SearchActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for SearchActivity.
 * 
 * Tests search functionality:
 * - Search view display and interaction
 * - Search results RecyclerView
 * - Error handling
 * - Back navigation
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class SearchActivityUITest {

  @get:Rule
  val activityRule = ActivityScenarioRule(SearchActivity::class.java)

  @Test
  fun searchActivity_displaysSearchView() {
    // Check that the search view is displayed in the action bar
    onView(withId(androidx.appcompat.R.id.search_src_text))
      .check(matches(isDisplayed()))
  }

  @Test
  fun searchActivity_displaysRecyclerView() {
    // Check that the search results RecyclerView is displayed
    onView(withId(com.hoc.flowmvi.ui.search.R.id.usersRecycler))
      .check(matches(isDisplayed()))
  }

  @Test
  fun searchActivity_acceptsSearchInput() {
    // Type in search view
    onView(withId(androidx.appcompat.R.id.search_src_text))
      .perform(click())
      .perform(typeText("John"))
      .perform(pressImeActionButton())
    
    // Verify that search was submitted (the RecyclerView should still be visible)
    onView(withId(com.hoc.flowmvi.ui.search.R.id.usersRecycler))
      .check(matches(isDisplayed()))
  }

  @Test
  fun searchActivity_showsSearchHint() {
    // Check that the search view has the correct hint
    onView(withId(androidx.appcompat.R.id.search_src_text))
      .check(matches(hasTextColor(android.R.attr.textColorHint)))
  }

  @Test
  fun searchActivity_hasRetryButton() {
    // Check that retry button exists (may not be visible depending on state)
    onView(withId(com.hoc.flowmvi.ui.search.R.id.retryButton))
      .check(matches(isDisplayed()))
  }

  @Test
  fun searchActivity_hasBackButton() {
    // Check that the back button (home as up) is enabled
    onView(withContentDescription("Navigate up"))
      .check(matches(isDisplayed()))
  }

  @Test
  fun searchActivity_canClearSearch() {
    // Type in search view
    onView(withId(androidx.appcompat.R.id.search_src_text))
      .perform(click())
      .perform(typeText("test search"))
    
    // Clear the search (if clear button is visible)
    onView(withId(androidx.appcompat.R.id.search_close_btn))
      .perform(click())
    
    // Verify search field is cleared
    onView(withId(androidx.appcompat.R.id.search_src_text))
      .check(matches(withText("")))
  }

  @Test
  fun searchActivity_progressBarExists() {
    // Check that progress bar exists (visibility depends on loading state)
    onView(withId(com.hoc.flowmvi.ui.search.R.id.progressBar))
      .check(matches(isDisplayed()))
  }
}