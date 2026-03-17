# UI Tests Documentation

This document describes the comprehensive UI tests added to the MVI Coroutines Flow Android application.

## Overview

The UI tests are built using:
- **AndroidJUnit4** for test execution
- **Espresso** for UI interactions and assertions
- **ActivityScenarioRule** for activity lifecycle management
- **Intent testing** for navigation verification

## Test Structure

### App Module Tests (`app/src/androidTest/`)

1. **MainActivityUITest** (ExampleInstrumentedTest.kt)
   - Tests user list display via RecyclerView
   - Tests SwipeRefreshLayout pull-to-refresh functionality
   - Tests menu navigation to Add and Search activities
   - Tests error states and retry functionality

2. **NavigationUITest**
   - Tests Intent-based navigation from MainActivity to AddActivity
   - Tests Intent-based navigation from MainActivity to SearchActivity
   - Uses Espresso Intents for verification

3. **AddActivityUITest**
   - Tests form field display and input handling
   - Tests form validation for email, first name, last name
   - Tests add button functionality and validation errors

4. **SearchActivityUITest**
   - Tests SearchView display and interaction
   - Tests search input and submission
   - Tests search results RecyclerView display

5. **IntegrationUITest**
   - Tests complete end-to-end user flows
   - Tests navigation between multiple activities
   - Tests form filling and search workflows
   - Tests swipe-to-refresh integration

### Feature Module Tests

1. **feature-main** (`feature-main/src/androidTest/`)
   - **MainActivityUITest**: Module-specific tests for MainActivity

2. **feature-add** (`feature-add/src/androidTest/`)
   - **AddActivityUITest**: Module-specific tests for AddActivity form functionality

3. **feature-search** (`feature-search/src/androidTest/`)
   - **SearchActivityUITest**: Module-specific tests for SearchActivity search functionality

## Test Coverage

### UI Components Tested
- ✅ RecyclerView with user list
- ✅ SwipeRefreshLayout
- ✅ TextInputLayout form fields
- ✅ SearchView in ActionBar
- ✅ Material buttons and progress indicators
- ✅ Error states and retry buttons
- ✅ Navigation drawer/menu

### User Interactions Tested
- ✅ Pull-to-refresh gestures
- ✅ Text input and form validation
- ✅ Menu navigation
- ✅ Search input and submission
- ✅ Button clicks and form submission
- ✅ Back navigation
- ✅ Intent-based navigation between activities

### Error Scenarios Tested
- ✅ Form validation errors (invalid email, empty fields)
- ✅ Network error states with retry functionality
- ✅ Loading states and progress indicators

## Running the Tests

### Prerequisites
- Android device or emulator connected
- App built in debug mode

### Command Line Execution

```bash
# Run all UI tests
./gradlew connectedAndroidTest

# Run specific module tests
./gradlew app:connectedAndroidTest
./gradlew feature-main:connectedAndroidTest
./gradlew feature-add:connectedAndroidTest
./gradlew feature-search:connectedAndroidTest

# Run specific test class
./gradlew app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.hoc.flowmvi.MainActivityUITest

# Run with coverage
./gradlew app:connectedAndroidTest koverGenerateXmlReport
```

### Android Studio Execution

1. Right-click on test class or method
2. Select "Run 'TestName'"
3. View results in Test Results panel

## Test Dependencies

The following dependencies were added to support comprehensive UI testing:

```kotlin
// Core testing framework
androidTestImplementation(libs.androidx.test.junit.ktx)
androidTestImplementation(libs.androidx.test.core.ktx)
androidTestImplementation(libs.androidx.test.rules)

// Espresso UI testing
androidTestImplementation(libs.androidx.test.espresso.core)
androidTestImplementation(libs.androidx.test.espresso.contrib)
androidTestImplementation(libs.androidx.test.espresso.intents)
```

## Test Best Practices

1. **Page Object Pattern**: Consider implementing page objects for complex screens
2. **Test Data**: Use consistent test data across tests
3. **Isolation**: Each test should be independent and not rely on others
4. **Assertions**: Use specific assertions rather than generic ones
5. **Timing**: Use Espresso's built-in waiting mechanisms instead of Thread.sleep()

## Known Limitations

1. **Network Dependencies**: Some tests may require network mocking for consistent results
2. **State Management**: Tests assume certain initial states of the app
3. **Device Dependencies**: Some gestures may behave differently on different devices
4. **Build Configuration**: Tests require the app to be in a testable state (debug build)

## Future Enhancements

1. **Network Mocking**: Add OkHttp MockWebServer for network request testing
2. **Test Data Factory**: Implement test data builders for consistent test data
3. **Screenshot Testing**: Add screenshot comparison testing
4. **Performance Testing**: Add UI performance benchmarking
5. **Accessibility Testing**: Add accessibility verification tests
6. **Cross-platform Testing**: Extend tests for different device configurations

## Troubleshooting

### Common Issues

1. **Resource ID not found**: Ensure the correct module context and R.id references
2. **Activity not found**: Verify activity is exported and properly configured
3. **Test timeouts**: Increase timeout values for slow operations
4. **Flaky tests**: Add proper waits and state verification

### Debug Tips

1. Enable verbose logging with `adb logcat`
2. Use Espresso's layout hierarchy dumps
3. Add screenshot capture on test failure
4. Use debugging mode to step through test execution