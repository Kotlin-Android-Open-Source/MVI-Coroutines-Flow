# UI Tests Summary

This directory contains comprehensive UI tests for the MVI Coroutines Flow Android application.

## Quick Start

1. **Connect an Android device or start an emulator**
2. **Run all tests**:
   ```bash
   ./run_ui_tests.sh --all
   ```

3. **Run specific module tests**:
   ```bash
   ./run_ui_tests.sh --app        # App module only
   ./run_ui_tests.sh --features   # Feature modules only
   ```

4. **Generate coverage report**:
   ```bash
   ./run_ui_tests.sh --coverage
   ```

## Test Files Overview

### App Module (`app/src/androidTest/`)
- **MainActivityUITest** - Core app functionality
- **NavigationUITest** - Inter-activity navigation
- **AddActivityUITest** - User creation form
- **SearchActivityUITest** - Search functionality  
- **IntegrationUITest** - End-to-end workflows

### Feature Modules
- **feature-main** - MainActivity specific tests
- **feature-add** - AddActivity specific tests  
- **feature-search** - SearchActivity specific tests

## Coverage

✅ **UI Components**: RecyclerView, SwipeRefreshLayout, SearchView, Forms  
✅ **User Interactions**: Touch, swipe, text input, navigation  
✅ **Validation**: Form validation, error states  
✅ **Navigation**: Intent verification, back navigation  
✅ **Integration**: End-to-end user workflows  

## Documentation

- **[UI_TESTS.md](UI_TESTS.md)** - Detailed documentation
- **[run_ui_tests.sh](run_ui_tests.sh)** - Test runner script

## Dependencies Added

- `androidx-test-espresso-contrib` - RecyclerView testing
- `androidx-test-espresso-intents` - Intent verification  
- `androidx-test-rules` - Additional test rules

The tests provide comprehensive validation of the MVI architecture app's UI functionality and user interactions.