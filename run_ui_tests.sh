#!/bin/bash

# UI Tests Runner Script
# This script provides convenient commands to run the comprehensive UI tests

set -e

echo "🧪 MVI Coroutines Flow - UI Tests Runner"
echo "========================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Android device/emulator is connected
check_device() {
    print_status "Checking for connected Android devices..."
    if ! command -v adb &> /dev/null; then
        print_error "ADB not found. Please install Android SDK and add to PATH."
        exit 1
    fi
    
    DEVICES=$(adb devices | grep -v "List of devices" | grep -v "^$" | wc -l)
    if [ "$DEVICES" -eq 0 ]; then
        print_error "No Android devices or emulators connected."
        print_status "Please connect a device or start an emulator and try again."
        exit 1
    fi
    
    print_success "Found $DEVICES connected device(s)"
}

# Build the app in debug mode
build_app() {
    print_status "Building app in debug mode..."
    if ./gradlew assembleDebug; then
        print_success "App built successfully"
    else
        print_error "Failed to build app"
        exit 1
    fi
}

# Run all UI tests
run_all_tests() {
    print_status "Running all UI tests..."
    ./gradlew connectedAndroidTest
}

# Run app module tests only
run_app_tests() {
    print_status "Running app module UI tests..."
    ./gradlew app:connectedAndroidTest
}

# Run feature module tests
run_feature_tests() {
    print_status "Running feature module UI tests..."
    ./gradlew feature-main:connectedAndroidTest feature-add:connectedAndroidTest feature-search:connectedAndroidTest
}

# Run specific test class
run_specific_test() {
    local test_class=$1
    if [ -z "$test_class" ]; then
        print_error "Please provide test class name"
        echo "Example: ./run_ui_tests.sh --class com.hoc.flowmvi.MainActivityUITest"
        exit 1
    fi
    
    print_status "Running specific test: $test_class"
    ./gradlew app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class="$test_class"
}

# Clean and run tests
clean_and_test() {
    print_status "Cleaning project and running tests..."
    ./gradlew clean
    run_all_tests
}

# Generate test coverage report
generate_coverage() {
    print_status "Running tests and generating coverage report..."
    ./gradlew connectedAndroidTest koverGenerateXmlReport
    print_success "Coverage report generated in build/reports/kover/"
}

# Show test results
show_results() {
    print_status "Test results locations:"
    echo "  • App module: app/build/reports/androidTests/connected/"
    echo "  • Feature modules: feature-*/build/reports/androidTests/connected/"
    echo "  • Coverage: build/reports/kover/"
}

# Main script logic
case "${1:-}" in
    --all | -a)
        check_device
        build_app
        run_all_tests
        show_results
        ;;
    --app)
        check_device
        build_app
        run_app_tests
        show_results
        ;;
    --features | -f)
        check_device
        build_app
        run_feature_tests
        show_results
        ;;
    --class | -c)
        check_device
        build_app
        run_specific_test "$2"
        show_results
        ;;
    --clean | -cl)
        check_device
        clean_and_test
        show_results
        ;;
    --coverage | -cov)
        check_device
        build_app
        generate_coverage
        show_results
        ;;
    --help | -h | "")
        echo "Usage: $0 [OPTION]"
        echo ""
        echo "Options:"
        echo "  --all, -a           Run all UI tests (app + feature modules)"
        echo "  --app               Run app module UI tests only"
        echo "  --features, -f      Run feature module UI tests only"
        echo "  --class TEST, -c    Run specific test class"
        echo "  --clean, -cl        Clean project and run all tests"
        echo "  --coverage, -cov    Run tests and generate coverage report"
        echo "  --help, -h          Show this help message"
        echo ""
        echo "Examples:"
        echo "  $0 --all                                    # Run all tests"
        echo "  $0 --class com.hoc.flowmvi.MainActivityUITest  # Run specific test"
        echo "  $0 --coverage                               # Run with coverage"
        ;;
    *)
        print_error "Unknown option: $1"
        echo "Use --help for usage information"
        exit 1
        ;;
esac