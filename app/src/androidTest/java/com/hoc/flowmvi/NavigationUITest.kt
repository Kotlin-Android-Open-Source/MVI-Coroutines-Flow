package com.hoc.flowmvi

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.hoc.flowmvi.ui.add.AddActivity
import com.hoc.flowmvi.ui.main.MainActivity
import com.hoc.flowmvi.ui.search.SearchActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for navigation between activities.
 * 
 * Tests navigation flows:
 * - MainActivity to AddActivity
 * - MainActivity to SearchActivity
 * - Back navigation
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class NavigationUITest {

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
  fun navigateToAddActivity_fromMenu() {
    // Open the options menu
    openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
    
    // Click on Add menu item
    onView(withText("Add"))
      .perform(click())
    
    // Verify that AddActivity was launched
    intended(hasComponent(AddActivity::class.java.name))
  }

  @Test
  fun navigateToSearchActivity_fromMenu() {
    // Open the options menu
    openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
    
    // Click on Search menu item
    onView(withText("Search"))
      .perform(click())
    
    // Verify that SearchActivity was launched
    intended(hasComponent(SearchActivity::class.java.name))
  }
}