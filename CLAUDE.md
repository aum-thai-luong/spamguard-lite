# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project context

SpamGuard Lite is a sample Android app for the course **AI-Assisted Android Development**. It reads the device call log, lets the user create block rules (exact number or prefix), and flags calls that match a rule. It uses Clean Architecture + MVVM with **XML + ViewBinding** UI (a later lab migrates screens to Jetpack Compose).

- The repo **intentionally contains 3 bugs** for Lab 2; their symptoms are in `BUGS.md`. Don't fix them as a side effect of other work. When asked to fix one, follow the BUGS.md workflow: write a failing test first, explain the root cause, apply the minimal fix, and make one commit per bug. Note that the existing tests all pass, so green tests don't mean the code is correct.
- Docs, code comments, and commit messages are written in **Vietnamese** (commits use conventional prefixes like `feat(blocklist):`, `docs:`). Follow the same style.

## Commands

Use `./gradlew` from Git Bash or `.\gradlew.bat` from PowerShell.

```bash
./gradlew assembleDebug          # build
./gradlew installDebug           # install on emulator/device (minSdk 26, compileSdk 35, JDK 17)
./scripts/seed-call-log.sh       # seed sample call log via adb
./gradlew test                   # all unit tests
./gradlew lint                   # Android lint

# A single module (core:domain and core:testing are pure JVM modules and use `test`;
# Android modules use `testDebugUnitTest`)
./gradlew :core:domain:test
./gradlew :feature:blocklist:testDebugUnitTest

# A single test class or method
./gradlew :core:domain:test --tests "com.spamguard.core.domain.spam.SpamClassifierTest"
./gradlew :feature:calllog:testDebugUnitTest --tests "*CallLogViewModelTest"
```

If seeding fails, fake calls with `adb emu gsm call <number>` followed by `adb emu gsm cancel <number>`.

Library versions are pinned in `gradle/libs.versions.toml` (AGP 8.7.3, Kotlin 2.0.21, KSP for Hilt and Room). Room exports its schema to `core/data/schemas/`. Commit changes there, because migrations depend on them.

## Architecture

Dependencies only point inward: `app` → `feature/*` → `core/domain` ← `core/data`. Read `docs/ARCHITECTURE.md` for the full diagram and the SOLID mapping.

- **`core/domain`** is pure Kotlin (a `java-library` module with no Android SDK). It holds the models, repository interfaces, single-purpose use cases (`operator fun invoke`), `PhoneNumberNormalizer`, and `SpamClassifier`. Match types use a strategy: to add one, add a `MatchType` plus a `BlockRuleMatcher` in `spam/BlockRuleMatcher.kt` (with its priority) rather than editing `SpamClassifier`.
- **`core/data`** contains Room (`BlockRuleDao`/`Entity`), the CallLog `ContentResolver` data source, mappers, and the repository impls, all `internal`. Interfaces are bound to impls with `@Binds` in `di/DataModule.kt`. Repository impls convert infrastructure failures into domain errors (`CallLogUnavailableException`).
- **`feature/*`** depend only on `core/domain` and never on `core/data`. Each screen is `Fragment` + `ViewModel` + a single `UiState` type + a `UiMapper` (domain model → display model: formatting, icons, string res).
- **`app`** contains `MainActivity`, `nav_graph.xml`, `bottom_navigation.xml`, the theme, and `TimeModule`. `TimeModule` provides `Clock`/`ZoneId`. `Clock`, `ZoneId`, and `CoroutineDispatcher` (`@IoDispatcher`) are always injected, never called directly.
- **`core/testing`** contains `FakeCallLogRepository`, `FakeBlockRuleRepository`, `TestData` builders, and `MainDispatcherRule`. It exposes JUnit and coroutines-test as `api`, so modules only need `testImplementation(project(":core:testing"))`.

Call log data flow: `CallLogFragment` sends `onPermissionResult()` to `CallLogViewModel`, which calls `GetClassifiedCallsUseCase`. That use case reads the call log once and combines it with `BlockRuleRepository.observeRules()`, so it re-emits whenever the rules change. `CallUiMapper` turns the result into the `StateFlow<CallLogUiState>` that the fragment renders.

## Conventions

- **UI state:** use `StateFlow` and collect it with `repeatOnLifecycle(STARTED)`. Don't use LiveData. Fragments only forward events, collect state, and call `render(state)`, with no business logic.
- **Collecting state in tests:** `uiState` uses `WhileSubscribed`, so ViewModel tests must start a collector first (see the `collectUiState()` helper in `BlocklistViewModelTest`).
- **One-off messages:** put them in UI state as `userMessageRes` and clear them through `onUserMessageShown()`.
- **Dialogs:** dialogs need an empty constructor. Pass data via `arguments` and return results via the Fragment Result API (`setFragmentResult`/`setFragmentResultListener`).
- **ViewBinding:** don't store the binding in a Fragment field. Create it in `onViewCreated` and pass it to `render`.
- **Errors:** ViewModels catch only known domain exceptions and let programming errors surface.
- **Strings:** all strings go in `strings.xml` with a module prefix (`call_log_*`, `blocklist_*`).
- **Personal data:** never log phone numbers or contact names. Tests use fake data.
- **Normalization:** normalize phone numbers before matching (`+84 900-123-456` → `0900123456`). Rule patterns are stored normalized.

## Adding a screen

1. Create `feature/<name>` by copying `feature/blocklist/build.gradle.kts`, then register it in `settings.gradle.kts` and as a dependency of `app`.
2. Add use cases (and a repository interface if needed) in `core/domain`.
3. Implement the repository in `core/data` and `@Binds` it in `DataModule`.
4. Write the feature's `UiState`, `UiMapper`, `ViewModel`, `Fragment`, layout, and `strings.xml`, plus tests for the ViewModel and mapper.
5. Add a destination to `app/.../navigation/nav_graph.xml` and an item to `menu/bottom_navigation.xml`.
