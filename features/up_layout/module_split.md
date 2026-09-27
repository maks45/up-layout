# up_layout module split

Last update date: 2026-09-27

## Goal

Separate the future `UserPositionedLayout` library from the demo application:

- `up_layout` — KMP library module (the composable container itself). All position / rotation / size state, transformation math, and gesture logic live in `up_layout/commonMain`. It must not depend on demo code. Later it will be published as a library without structural changes.
- `shared` + `androidApp` + `iosApp` — demo application showcasing layout functions. `shared/commonMain` consumes `up_layout` (demo screens), `androidApp` / `iosApp` are thin platform hosts.

## Structure

- `up_layout/build.gradle.kts` mirrors `shared` (kotlinMultiplatform + androidMultiplatformLibrary + composeMultiplatform). Android namespace `com.mdsw.uplayout`, iOS static framework `UpLayout` (`iosArm64`, `iosSimulatorArm64`).
- `up_layout/src/commonMain/kotlin/com/mdsw/uplayout/UserPositionedLayout.kt` — container entry point (currently a `Box` placeholder; transform features land here).
- `up_layout` depends only on Compose UI artifacts (`runtime`, `foundation`, `ui`) + `kotlin-test`.
- `settings.gradle.kts` includes `:up_layout`.
- `shared` consumes it via `api(project(":up_layout"))` in `commonMain` and re-exports it in the `Shared` iOS framework via `export(project(":up_layout"))`, so `iosApp` needs no Xcode changes (Swift keeps importing only `Shared`).
- Layer direction: `androidApp -> shared -> up_layout`.

## Demo wiring

- `shared/.../App.kt` renders `com.mdsw.uplayout.UserPositionedLayout` to prove the demo consumes the library on both platforms.

## Verification

- `./gradlew :up_layout:assemble` — builds AAR + iOS frameworks.
- `./gradlew :shared:assemble :androidApp:assembleDebug` — builds demo (includes iOS `Shared` framework with exported `UpLayout`).
- `./gradlew :up_layout:check :shared:testAndroidHostTest` — tests pass.
