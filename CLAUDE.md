# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project context

SpamGuard Lite is a sample Android app for the course **AI-Assisted Android Development**. It reads the device call log, lets the user create block rules (exact number or prefix), and flags calls that match a rule. It uses Clean Architecture + MVVM with a **Jetpack Compose** UI hosted in Fragments (Navigation Component + bottom navigation). The screens were migrated from XML + ViewBinding in Labs 1–3; the dialogs still use XML layouts.

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

`feature/blocklist` has Robolectric tests (dialog and Compose UI). Robolectric runs offline there: its `android-all` jars are Gradle dependencies (`robolectricAndroidAllSdk*` in the version catalog) that must be bumped together with `robolectric`.

If seeding fails, fake calls with `adb emu gsm call <number>` followed by `adb emu gsm cancel <number>`.

Library versions are pinned in `gradle/libs.versions.toml` (AGP 8.7.3, Kotlin 2.0.21, KSP for Hilt and Room). Room exports its schema to `core/data/schemas/`. Commit changes there, because migrations depend on them.

## Architecture

Dependencies only point inward: `app` → `feature/*` → `core/domain` ← `core/data`. Read `docs/ARCHITECTURE.md` for the full diagram and the SOLID mapping.

- **`core/domain`** is pure Kotlin (a `java-library` module with no Android SDK). It holds the models, repository interfaces, single-purpose use cases (`operator fun invoke`), `PhoneNumberNormalizer`, and `SpamClassifier`. Match types use a strategy: to add one, add a `MatchType` plus a `BlockRuleMatcher` in `spam/BlockRuleMatcher.kt` (with its priority) rather than editing `SpamClassifier`.
- **`core/data`** contains Room (`BlockRuleDao`/`Entity`), the CallLog `ContentResolver` data source, mappers, and the repository impls, all `internal`. Interfaces are bound to impls with `@Binds` in `di/DataModule.kt`. Repository impls convert infrastructure failures into domain errors (`CallLogUnavailableException`).
- **`feature/*`** depend only on `core/domain` (plus `core/ui` for the theme) and never on `core/data`. Each screen is a host `Fragment` + `XxxRoute`/`XxxScreen` composables + `ViewModel` + a single `UiState` type + a `UiMapper` (domain model → display model: formatting, icons, string res).
- **`core/ui`** is an Android library holding the shared theme: the XML `Theme.SpamGuard` (used by `MainActivity` and dialogs, with `brand_*` colors) and the Compose `SpamGuardTheme`. Features wrap their Compose content in `SpamGuardTheme` instead of defining their own theme.
- **`app`** contains `MainActivity`, `nav_graph.xml`, `bottom_navigation.xml`, and `TimeModule`. `TimeModule` provides `Clock`/`ZoneId`. `Clock`, `ZoneId`, and `CoroutineDispatcher` (`@IoDispatcher`) are always injected, never called directly.
- **`core/testing`** contains `FakeCallLogRepository`, `FakeBlockRuleRepository`, `TestData` builders, and `MainDispatcherRule`. It exposes JUnit and coroutines-test as `api`, so modules only need `testImplementation(project(":core:testing"))`.

Call log data flow: `CallLogFragment` sends `onPermissionResult()` to `CallLogViewModel`, which calls `GetClassifiedCallsUseCase`. That use case reads the call log once and combines it with `BlockRuleRepository.observeRules()`, so it re-emits whenever the rules change. `CallUiMapper` turns the result into the `StateFlow<CallLogUiState>` that `CallLogScreen` renders.

## Conventions

- **UI state:** ViewModels expose `StateFlow`; don't use LiveData. `XxxRoute` collects it with `collectAsStateWithLifecycle()` and passes it to a stateless `XxxScreen(uiState, onEvent…)`. Composables hold no business logic and never touch repositories.
- **Host Fragment:** `onCreateView` returns a `ComposeView` with `ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed` wrapped in `SpamGuardTheme`. The Fragment only handles what needs a Fragment: runtime permissions (`registerForActivityResult`) and showing dialogs / listening for their results.
- **Compose:** give `LazyColumn` items a stable `key`, add `@Preview`s for each state of the screen, and keep touch targets ≥ 48dp with a `contentDescription` on icon buttons.
- **Collecting state in tests:** `uiState` uses `WhileSubscribed`, so ViewModel tests must start a collector first (see the `collectUiState()` helper in `BlocklistViewModelTest`).
- **One-off messages:** put them in UI state as `userMessageRes` and clear them through `onUserMessageShown()`.
- **Dialogs:** dialogs need an empty constructor. Pass data via `arguments` and return results via the Fragment Result API (`setFragmentResult`/`setFragmentResultListener`).
- **ViewBinding (dialogs only):** create the binding inside `onCreateDialog` and don't keep it in a field.
- **Errors:** ViewModels catch only known domain exceptions and let programming errors surface.
- **Strings:** all strings go in `strings.xml` with a module prefix (`call_log_*`, `blocklist_*`).
- **Personal data:** never log phone numbers or contact names. Tests use fake data.
- **Normalization:** normalize phone numbers before matching (`+84 900-123-456` → `0900123456`). Rule patterns are stored normalized.

## Adding a screen

1. Create `feature/<name>` by copying `feature/blocklist/build.gradle.kts`, then register it in `settings.gradle.kts` and as a dependency of `app`.
2. Add use cases (and a repository interface if needed) in `core/domain`.
3. Implement the repository in `core/data` and `@Binds` it in `DataModule`.
4. Write the feature's `UiState`, `UiMapper`, `ViewModel`, `XxxRoute`/`XxxScreen` (with previews), the host `Fragment`, and `strings.xml`, plus tests for the ViewModel and mapper (and Robolectric Compose tests for non-trivial UI logic).
5. Add a destination to `app/.../navigation/nav_graph.xml` and an item to `menu/bottom_navigation.xml`.
