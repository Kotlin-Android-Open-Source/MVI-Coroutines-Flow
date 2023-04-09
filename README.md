## MVI-Coroutines-Flow
- MVI-Coroutines-Flow-Clean-Architecture
- MVI-Coroutines-Flow-Clean-Architecture-ArrowKt
- MVI-Coroutines-Flow-Clean-Architecture-ArrowKt-KoinDI

<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

[![Build CI](https://github.com/Kotlin-Android-Open-Source/MVI-Coroutines-Flow/actions/workflows/build.yml/badge.svg)](https://github.com/Kotlin-Android-Open-Source/MVI-Coroutines-Flow/actions/workflows/build.yml)
[![Unit Tests CI](https://github.com/Kotlin-Android-Open-Source/MVI-Coroutines-Flow/actions/workflows/unit-test.yml/badge.svg)](https://github.com/Kotlin-Android-Open-Source/MVI-Coroutines-Flow/actions/workflows/unit-test.yml)
[![codecov](https://codecov.io/gh/Kotlin-Android-Open-Source/MVI-Coroutines-Flow/branch/master/graph/badge.svg?token=QBV7IE7RU6)](https://codecov.io/gh/Kotlin-Android-Open-Source/MVI-Coroutines-Flow)
[![Qodana](https://github.com/Kotlin-Android-Open-Source/MVI-Coroutines-Flow/actions/workflows/qodana.yml/badge.svg)](https://github.com/Kotlin-Android-Open-Source/MVI-Coroutines-Flow/actions/workflows/qodana.yml)
[![Validate Gradle Wrapper](https://github.com/Kotlin-Android-Open-Source/MVI-Coroutines-Flow/actions/workflows/gradle-wrapper-validation.yml/badge.svg)](https://github.com/Kotlin-Android-Open-Source/MVI-Coroutines-Flow/actions/workflows/gradle-wrapper-validation.yml)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![Kotlin](https://img.shields.io/badge/kotlin-1.8.10-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Hits](https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https%3A%2F%2Fgithub.com%2FKotlin-Android-Open-Source%2FMVI-Coroutines-Flow&count_bg=%2379C83D&title_bg=%23555555&icon=&icon_color=%23E7E7E7&title=hits&edge_flat=false)](https://hits.seeyoufarm.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-purple.svg)](https://opensource.org/licenses/MIT)
[![Gitter](https://badges.gitter.im/Kotlin-Android-Open-Source/community.svg)](https://gitter.im/Kotlin-Android-Open-Source/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

<br>
Liked some of my work? Buy me a coffee (or more likely a beer)

<a href="https://www.buymeacoffee.com/hoc081098" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-blue.png" alt="Buy Me A Coffee" height=64></a>

## Coroutine + Flow = MVI :heart:
*   Play MVI with Kotlin Coroutines Flow.
*   Multiple modules, Clean Architecture, Functional programming with [ΛRROW.kt](https://arrow-kt.io/).
*   Unit tests for MVI ViewModel, domain and data layer.
*   Master branch using Koin for DI.
*   **Checkout [dagger_hilt branch](https://github.com/Kotlin-Android-Open-Source/MVI-Coroutines-Flow/tree/dagger_hilt), using Dagger Hilt for DI** (_obsolete, will update as soon as possible_).
*   **[Download latest debug APK here](https://nightly.link/Kotlin-Android-Open-Source/MVI-Coroutines-Flow/workflows/build/master/app-debug.zip)**.

## More

- [Jetpack Compose Version](https://github.com/Kotlin-Android-Open-Source/Jetpack-Compose-MVI-Coroutines-Flow)
- [Pagination Horizontal List in Vertical List](https://github.com/Kotlin-Android-Open-Source/Pagination-MVI-Flow)

<details><summary> Light theme Screenshots </summary>

| List view state | Error view state |
| --------------- | ---------------- |
| <img src="screenshots/Screenshot_01.png" height="480" /> | <img src="screenshots/Screenshot_02.png" height="480"> |

| Add new user | Search user  |
| ------------ | ------------ |
| <img src="screenshots/Screenshot_03.png" height="480"> | <img src="screenshots/Screenshot_04.png" height="480"> |

</details>


<details><summary> Dark theme Screenshots </summary> 

| List view state | Error view state |
| --------------- | ---------------- |
| <img src="screenshots/Screenshot_dark_01.png" height="480" /> | <img src="screenshots/Screenshot_dark_02.png" height="480"> |

| Add new user | Search user  |
| ------------ | ------------ |
| <img src="screenshots/Screenshot_dark_03.png" height="480"> | <img src="screenshots/Screenshot_dark_04.png" height="480"> |

<!-- Pixel 3 XL API 30 -->
</details>

<br>

## MVI pattern

This pattern was specified by [André Medeiros (Staltz)](https://twitter.com/andrestaltz) for a JavaScript framework he has written called [cycle.js](https://cycle.js.org/)

<p align="center">
  <img src="MVI_diagram.png">
<p>

- `intent()`: This function takes the input from the user (i.e. UI events, like click events) and translate it to “something” that will be passed as parameter to `model()` function.
  This could be a simple string to set a value of the model to or more complex data structure like an Object. We could say we have the intention to change the model with an intent.
- `model()`: The `model()` function takes the output from `intent()` as input to manipulate the Model. The output of this function is a new Model (state changed).
  So it should not update an already existing Model. **We want immutability!**
  We don’t change an already existing Model object instance.
  We create a new Model according to the changes described by the intent.
  Please note, that the `model()` function is the only piece of your code that is allowed to create a new Model object.
  Then this new immutable Model is the output of this function.
  Basically, the `model()` function calls our apps business logic (could be an Interactor, Usecase, Repository … whatever pattern / terminology you use in your app) and delivers a new Model object as result.
- `view()`: This method takes the model returned from `model()` function and gives it as input to the `view()` function. Then the View simply displays this Model somehow. `view()` is basically the same as `view.render(model)`.

### Reference

- [Model-View-Intent Design Pattern on Android](https://xizzhu.me/post/2021-06-21-android-mvi-kotlin-coroutines-flow-compose/)
- [Reactive Apps with Model-View-Intent](https://hannesdorfmann.com/android/mosby3-mvi-1/)

## Contributors 

<a href="https://github.com/Kotlin-Android-Open-Source/MVI-Coroutines-Flow/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=Kotlin-Android-Open-Source/MVI-Coroutines-Flow" />
</a>
