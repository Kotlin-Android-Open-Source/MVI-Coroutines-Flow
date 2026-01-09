# GitHub Copilot Instructions for MVI-Coroutines-Flow

## Project Overview

This is an **Android application** demonstrating the **Model-View-Intent (MVI)** architectural pattern using **Kotlin Coroutines** and **Flow**. The project showcases a user management system with features to view, add, search, and remove users.

### Key Technologies
- **Language**: Kotlin 2.0.20
- **Architecture**: MVI (Model-View-Intent) + Clean Architecture
- **Concurrency**: Kotlin Coroutines & Flow
- **Functional Programming**: Arrow-kt for functional error handling
- **Dependency Injection**: Koin (master branch), Dagger Hilt (dagger_hilt branch - obsolete)
- **Testing**: JUnit, MockK, Kotlinx-Coroutines-Test
- **Minimum SDK**: API 21 (Android 5.0)
- **Target SDK**: API 35
- **Build System**: Gradle with Kotlin DSL

## MVI Pattern Implementation

### Core Concepts

The MVI pattern in this project follows these principles:

1. **Intent** (`MviIntent`): Represents user actions/intentions
   - Immutable objects that describe what the user wants to do
   - Examples: `ViewIntent.Refresh`, `ViewIntent.RemoveUser`, `ViewIntent.EmailChanged`

2. **Model/State** (`MviViewState`): Represents the UI state
   - Immutable, Parcelable data classes
   - Single source of truth for the UI
   - Example: `ViewState(isLoading, users, error)`

3. **View** (`MviView`): Renders the state
   - Activities/Fragments that implement `MviView<I, S, E>`
   - Observes `StateFlow<ViewState>` and `Flow<SingleEvent>`
   - Sends intents to ViewModel via `processIntent()`

4. **ViewModel** (`AbstractMviViewModel<I, S, E>`):
   - Processes intents through reactive streams
   - Produces partial state changes
   - Reduces partial changes into complete state
   - Emits single events for side effects (toasts, navigation)

### MVI Flow Architecture

```
User Action → ViewIntent → ViewModel.processIntent()
                ↓
         Intent Processor (Flow transformations)
                ↓
         PartialStateChange
                ↓
         State Reducer (scan operator)
                ↓
         ViewState (StateFlow) → View.render()
```

### State Management Pattern

```kotlin
// ViewState: Immutable UI state
data class ViewState(
  val isLoading: Boolean,
  val users: List<UserItem>,
  val error: UserError?
) : MviViewState

// ViewIntent: User actions
sealed interface ViewIntent : MviIntent {
  object Initial : ViewIntent
  object Refresh : ViewIntent
  data class RemoveUser(val user: UserItem) : ViewIntent
}

// SingleEvent: One-time events (toasts, navigation)
sealed interface SingleEvent : MviSingleEvent {
  data class ShowError(val error: UserError) : SingleEvent
  object NavigateToAddUser : SingleEvent
}
```

## Project Structure

### Module Architecture

The project follows **Clean Architecture** with multi-module structure:

```
├── app/                          # Application module (DI setup, initializers)
├── mvi/
│   ├── mvi-base/                # Core MVI abstractions (ViewModel, View, Intent, State)
│   └── mvi-testing/             # Testing utilities for MVI
├── domain/                       # Business logic layer
│   ├── model/                   # Domain models (User, Email, FirstName, LastName)
│   ├── repository/              # Repository interfaces
│   └── usecase/                 # Use cases (AddUser, RemoveUser, ObserveUsers, etc.)
├── data/                         # Data layer implementation
│   ├── remote/                  # API services (UserApiService, DTOs)
│   ├── mapper/                  # Mappers between layers
│   └── UserRepositoryImpl       # Repository implementation
├── feature-main/                 # Main screen feature (list users)
├── feature-add/                  # Add user feature
├── feature-search/               # Search users feature
├── core/                         # Core utilities (Mapper, EitherNes, Extensions)
├── core-ui/                      # UI utilities (ViewBinding, Extensions, Navigator)
└── test-utils/                   # Test utilities
```

### Dependency Flow
```
app → features → domain ← data
       ↓
    mvi-base
       ↓
    core, core-ui
```

## Coding Conventions & Patterns

### 1. Functional Error Handling with Arrow-kt

Use `Either` and `EitherNes` (Either with NonEmptySet) for error handling:

```kotlin
// EitherNes = Either<NonEmptySet<Error>, Value>
typealias EitherNes<E, A> = Either<NonEmptySet<E>, A>

// Domain layer
suspend fun addUser(user: User): Either<UserError, Unit>

// Validation with error accumulation
fun validateUser(email: String?, firstName: String?, lastName: String?): 
  EitherNes<UserValidationError, User> =
  Either.zipOrAccumulateNonEmptySet(
    Email.create(email),
    FirstName.create(firstName),
    LastName.create(lastName)
  ) { e, f, l -> User(email = e, firstName = f, lastName = l) }
```

### 2. Flow Operators & Patterns

**Key Flow patterns used:**

```kotlin
// Defer creation until subscription
defer { observeUsersUseCase() }

// Start with initial value
flow.startWith(PartialStateChange.Loading)

// FlatMap variants
flow.flatMapLatest { } // Cancel previous, switch to new
flow.flatMapFirst { }  // Ignore new while processing
flow.flatMapMerge { }  // Process concurrently

// State reduction
flow.scan(initialState) { state, change -> change.reduce(state) }

// Sharing
flow.shareIn(viewModelScope, SharingStarted.WhileSubscribed())
flow.stateIn(viewModelScope, SharingStarted.Eagerly, initialState)
```

### 3. ViewModel Implementation Pattern

**Standard ViewModel structure:**

```kotlin
class FeatureVM(
  private val useCase: UseCase
) : AbstractMviViewModel<ViewIntent, ViewState, SingleEvent>() {
  
  override val viewState: StateFlow<ViewState> by selfReferenced {
    val initialVS = ViewState.initial()
    
    intentSharedFlow
      .debugLog("ViewIntent")
      .toPartialStateChangeFlow()
      .debugLog("PartialStateChange")
      .onEach { sendEvent(it.toSingleEventOrNull() ?: return@onEach) }
      .scan(initialVS) { state, change -> change.reduce(state) }
      .stateIn(viewModelScope, SharingStarted.Eagerly, initialVS)
  }
  
  private fun SharedFlow<ViewIntent>.toPartialStateChangeFlow(): Flow<PartialStateChange> =
    merge(
      filterIsInstance<ViewIntent.Refresh>().toRefreshFlow(),
      filterIsInstance<ViewIntent.Load>().toLoadFlow()
    )
}
```

### 4. View Implementation Pattern

**Activity/Fragment implementing MviView:**

```kotlin
class FeatureActivity : AbstractMviActivity<ViewIntent, ViewState, SingleEvent>() {
  
  override val viewModel: FeatureVM by viewModel()
  private val binding: ActivityBinding by viewBinding()
  
  override fun setupViews() {
    // Setup initial view state
    binding.setupListeners()
  }
  
  override fun viewIntents(): Flow<ViewIntent> = merge(
    flowOf(ViewIntent.Initial),
    binding.refreshButton.clicks().mapTo(ViewIntent.Refresh),
    binding.addButton.clicks().mapTo(ViewIntent.Add)
  )
  
  override fun render(viewState: ViewState) {
    // Update UI based on state
    binding.progressBar.isVisible = viewState.isLoading
    binding.recyclerView.submitList(viewState.items)
  }
  
  override fun handleSingleEvent(event: SingleEvent) {
    when (event) {
      is SingleEvent.ShowError -> toast(event.message)
      is SingleEvent.NavigateToDetail -> navigator.navigateToDetail()
    }
  }
}
```

### 5. Naming Conventions

- **Classes**: PascalCase (e.g., `UserRepository`, `MainVM`)
- **Functions**: camelCase (e.g., `observeUsers()`, `toPartialStateChangeFlow()`)
- **Properties**: camelCase (e.g., `viewState`, `isLoading`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MIN_LENGTH_FIRST_NAME`)
- **Sealed interfaces**: Use `sealed interface` instead of `sealed class` when no state
- **ViewModels**: Suffix with `VM` (e.g., `MainVM`, `AddVM`, `SearchVM`)
- **Use cases**: Suffix with `UseCase` (e.g., `AddUserUseCase`)

### 6. State Saving

Use `MviViewStateSaver` for process death handling:

```kotlin
class ViewState : MviViewState, Parcelable {
  companion object {
    private const val VIEW_STATE_KEY = "com.hoc.flowmvi.feature.StateSaver"
  }
  
  class StateSaver : MviViewStateSaver<ViewState> {
    override fun ViewState.toBundle() = bundleOf(VIEW_STATE_KEY to this)
    
    override fun restore(bundle: Bundle?) =
      bundle?.parcelable<ViewState>(VIEW_STATE_KEY)?.copy(isLoading = false)
        ?: initial()
  }
}
```

### 7. Dependency Injection (Koin)

```kotlin
// Module definition
val featureModule = module {
  factory { FeatureVM(get(), get()) }
  factory<UseCase> { UseCaseImpl(get()) }
}

// In Activity
class FeatureActivity : AbstractMviActivity<ViewIntent, ViewState, SingleEvent>() {
  override val viewModel: FeatureVM by viewModel()
}
```

## Testing Approach

### 1. Unit Testing ViewModels

Use `BaseMviViewModelTest` for testing ViewModels:

```kotlin
@ExperimentalCoroutinesApi
class FeatureVMTest : BaseMviViewModelTest<ViewIntent, ViewState, SingleEvent, FeatureVM>() {
  
  private lateinit var vm: FeatureVM
  private lateinit var useCase: UseCase
  
  override fun setup() {
    super.setup()
    useCase = mockk()
    vm = FeatureVM(useCase)
  }
  
  @Test
  fun test_intent_producesExpectedState() {
    coEvery { useCase() } returns mockData.right()
    
    runVMTest(
      vmProducer = { vm },
      intents = flowOf(ViewIntent.Load),
      expectedStates = listOf(
        ViewState.initial(),
        ViewState.loading(),
        ViewState.success(mockData)
      ),
      expectedEvents = emptyList()
    )
    
    coVerify { useCase() }
  }
}
```

### 2. Domain Layer Testing

Test use cases and domain models:

```kotlin
@Test
fun test_userValidation_withInvalidEmail_returnsError() {
  val result = User.create(
    id = "1",
    email = "invalid",
    firstName = "John",
    lastName = "Doe",
    avatar = "url"
  )
  
  assertTrue(result.isLeft())
  result.leftOrNull()!!.contains(UserValidationError.INVALID_EMAIL_ADDRESS)
}
```

### 3. Data Layer Testing

Test repository implementations:

```kotlin
@Test
fun test_repository_refresh_success() = runTest {
  coEvery { apiService.getUsers() } returns mockResponse
  
  val result = repository.refresh()
  
  assertTrue(result.isRight())
  coVerify { apiService.getUsers() }
}
```

### 4. Test Utilities

- `TestCoroutineDispatcherRule`: For testing coroutines
- `MockK`: For mocking dependencies
- `BaseMviViewModelTest`: Base class for ViewModel tests
- `rightValueOrThrow`: Extract value from Either.Right or throw

## Build & Development

### Building the Project

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Run all tests
./gradlew test

# Run unit tests
./gradlew testDebugUnitTest

# Check code style
./gradlew spotlessCheck

# Apply code formatting
./gradlew spotlessApply

# Run static analysis
./gradlew detekt
```

### Code Quality Tools

1. **Spotless**: Code formatting (Ktlint)
2. **Detekt**: Static code analysis
3. **Kover**: Code coverage
4. **Qodana**: JetBrains code quality platform

### CI/CD Workflows

- `build.yml`: Build debug APK on push/PR
- `unit-test.yml`: Run unit tests and upload coverage
- `qodana.yml`: Static code analysis
- `gradle-versions-checker.yml`: Check dependency updates
- `remove-old-artifacts.yml`: Cleanup old artifacts

## Best Practices

### 1. Immutability

- Always use `val` instead of `var` when possible
- Use `data class` with `val` properties for states
- Use `copy()` for state updates

### 2. Reactive Programming

- Prefer declarative Flow transformations over imperative code
- Use appropriate Flow operators (flatMapLatest, flatMapFirst, flatMapMerge)
- Share flows appropriately with `shareIn()` and `stateIn()`

### 3. Error Handling

- Use `Either<Error, Value>` instead of exceptions for expected errors
- Use `EitherNes<Error, Value>` for validation with error accumulation
- Always handle both `Left` (error) and `Right` (success) cases

### 4. Coroutines & Dispatchers

- Use `AppCoroutineDispatchers` for dispatcher injection
- Default to `Dispatchers.Main.immediate` for UI updates
- Use `Dispatchers.IO` for network/database operations
- Use `withContext()` to switch dispatchers

### 5. ViewBinding

- Use ViewBinding delegate for type-safe view access
- Preload binding methods in initializer for performance

### 6. Logging

- Use Timber for logging
- Tag logs with ViewModel class name: `Timber.tag(logTag).d(...)`
- Use debug logs to track intent/state flow in debug builds only

### 7. Documentation

- Document complex algorithms and business logic
- Use KDoc for public APIs
- Keep comments concise and meaningful

## Common Patterns & Snippets

### Creating a New Feature Module

1. Create feature module directory
2. Add to `settings.gradle.kts`: `include(":feature-name")`
3. Create `build.gradle.kts` with dependencies
4. Implement MVI contract (ViewIntent, ViewState, SingleEvent)
5. Implement ViewModel extending `AbstractMviViewModel`
6. Implement View extending `AbstractMviActivity`
7. Create Koin module for DI
8. Add navigation provider to `Navigator`

### Handling Form Validation

```kotlin
// Accumulate validation errors
sealed interface ViewIntent : MviIntent {
  data class EmailChanged(val email: String) : ViewIntent
  data class Submit(val email: String, val name: String) : ViewIntent
}

// In ViewModel
private fun Flow<ViewIntent.Submit>.toSubmitFlow() =
  map { intent ->
    User.create(
      email = intent.email,
      firstName = intent.name,
      lastName = "",
      id = UUID.randomUUID().toString(),
      avatar = ""
    )
  }.flatMapLatest { userEither ->
    userEither.fold(
      ifLeft = { errors -> flowOf(PartialChange.ValidationError(errors)) },
      ifRight = { user -> 
        flowFromSuspend { addUserUseCase(user) }
          .map { /* handle result */ }
      }
    )
  }
```

### Implementing Search with Debounce

```kotlin
filterIsInstance<ViewIntent.SearchQueryChanged>()
  .map { it.query }
  .debounce(300.milliseconds)
  .distinctUntilChanged()
  .flatMapLatest { query ->
    flowFromSuspend { searchUseCase(query) }
      .map { /* handle result */ }
      .startWith(PartialChange.Searching)
  }
```

## Architecture Decision Records

### Why MVI?

- **Unidirectional data flow**: Easier to understand and debug
- **Single source of truth**: ViewState represents the complete UI state
- **Testability**: Pure functions for state reduction
- **Time-travel debugging**: Can replay intents to reproduce issues
- **Thread-safety**: Immutable states prevent race conditions

### Why Arrow-kt?

- **Functional error handling**: Explicit error types without exceptions
- **Error accumulation**: Collect all validation errors at once
- **Composability**: Chain operations with map, flatMap, etc.
- **Type safety**: Compiler ensures all error cases are handled

### Why Koin over Dagger?

- **Simplicity**: Less boilerplate, easier to learn
- **Kotlin-first**: Designed specifically for Kotlin
- **No code generation**: Faster build times
- **Easy testing**: Simple mocking and module replacement

### Why Flow over RxJava?

- **Native Kotlin**: Built into Kotlin coroutines
- **Lightweight**: Smaller API surface, easier to learn
- **Structured concurrency**: Automatic cancellation and cleanup
- **Cold streams by default**: Better performance and resource management

## Resources & References

- [MVI Pattern](https://hannesdorfmann.com/android/mosby3-mvi-1/)
- [Arrow-kt Documentation](https://arrow-kt.io/)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Kotlin Flow](https://kotlinlang.org/docs/flow.html)
- [Koin Documentation](https://insert-koin.io/)
- [Android Architecture Components](https://developer.android.com/topic/architecture)

## Contributing

When contributing to this project:

1. Follow the existing MVI pattern and structure
2. Write unit tests for new features
3. Update documentation for significant changes
4. Run `./gradlew spotlessApply` before committing
5. Ensure all CI checks pass
6. Keep commits small and focused
7. Write clear commit messages

## Version Information

- **Application Version**: 2.2.0 (versionCode: 20200)
- **Kotlin**: 2.0.20
- **Gradle**: 8.x
- **Android Gradle Plugin**: 8.x
- **Compile SDK**: 35
- **Min SDK**: 21
- **Target SDK**: 35
