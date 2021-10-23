# MVI-Coroutines-Flow
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

[![Build CI](https://github.com/Kotlin-Android-Open-Source/MVI-Coroutines-Flow/actions/workflows/build.yml/badge.svg)](https://github.com/Kotlin-Android-Open-Source/MVI-Coroutines-Flow/actions/workflows/build.yml)
[![Unit Tests CI](https://github.com/Kotlin-Android-Open-Source/MVI-Coroutines-Flow/actions/workflows/unit-test.yml/badge.svg)](https://github.com/Kotlin-Android-Open-Source/MVI-Coroutines-Flow/actions/workflows/unit-test.yml)
[![codecov](https://codecov.io/gh/Kotlin-Android-Open-Source/MVI-Coroutines-Flow/branch/master/graph/badge.svg?token=QBV7IE7RU6)](https://codecov.io/gh/Kotlin-Android-Open-Source/MVI-Coroutines-Flow)

## Coroutine + Flow = MVI :heart:
*   Play MVI with Kotlin Coroutines Flow.
*   Multiple modules, Clean Architecture.
*   Unit tests for MVI ViewModel, domain and data layer.
*   Master branch using Koin for DI.
*   **Checkout [dagger_hilt branch](https://github.com/Kotlin-Android-Open-Source/MVI-Coroutines-Flow/tree/dagger_hilt), using Dagger Hilt for DI** (_obsolete_).
*   **[Download latest debug APK here](https://nightly.link/Kotlin-Android-Open-Source/MVI-Coroutines-Flow/workflows/build/master/app-debug.zip)**.

> **Jetpack Compose Version** 👉 https://github.com/Kotlin-Android-Open-Source/Jetpack-Compose-MVI-Coroutines-Flow

| List view state | Error view state |
| --------------- | ---------------- |
| <img src="Screenshot_01.png" height="480" /> | <img src="Screenshot_02.png" height="480"> |

| Add new user | Search user  |
| ------------ | ------------ |
| <img src="Screenshot_03.png" height="480"> | <img src="Screenshot_04.png" height="480"> |

<!-- Pixel 3 XL API 30 -->

# MVI pattern

This pattern was specified by [André Medeiros (Staltz)](https://twitter.com/andrestaltz) for a JavaScript framework he has written called [cycle.js](https://cycle.js.org/). From a theoretical (and mathematical) point of view we could describe Model-View-Intent as follows [^1]

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

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tr>
    <td align="center"><a href="https://www.linkedin.com/in/hoc081098/"><img src="https://avatars.githubusercontent.com/u/36917223?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Petrus Nguyễn Thái Học</b></sub></a><br /><a href="https://github.com/Kotlin-Android-Open-Source/MVI-Coroutines-Flow/commits?author=hoc081098" title="Code">💻</a> <a href="#maintenance-hoc081098" title="Maintenance">🚧</a> <a href="#ideas-hoc081098" title="Ideas, Planning, & Feedback">🤔</a> <a href="#design-hoc081098" title="Design">🎨</a> <a href="https://github.com/Kotlin-Android-Open-Source/MVI-Coroutines-Flow/issues?q=author%3Ahoc081098" title="Bug reports">🐛</a></td>
  </tr>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!

[^1]: https://hannesdorfmann.com/android/mosby3-mvi-2/
