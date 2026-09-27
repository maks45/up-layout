# AGENTS.md

Last update date: 2026-09-27

## Project Overview

**UserPositionedLayout** is an open-source (Apache 2.0) Kotlin Multiplatform / Compose Multiplatform library targeting Android and iOS.

It provides a composable container where the user can directly manipulate children inside the container:

- change position (drag / move),
- change rotation angle (rotate),
- change size (resize / scale).

Additional manipulation functions will be defined later. All interaction logic lives in shared common code so behavior is identical on Android and iOS.

## Scope

These instructions apply to the entire repository rooted at this directory.

## Open-Source Policy

- This project is open source under the Apache License 2.0. See `LICENSE` and the `License` section in `README.md`.
- Do not introduce dependencies or code with a license incompatible with Apache 2.0.
- Do not commit secrets, keystores, signing credentials, or local SDK paths. `local.properties` and `keystore.*` must never be added to git.
- Never add sensitive information or credentials to git: API keys, tokens, passwords, signing keys, keystores, private certificates, or personal access data. Keep credentials in local untracked files or environment variables only.

## Platform Development Policy

- The library supports both Android and iOS platforms.
- All current changes, development, and tests must target Android and iOS.
- Keep all shared UI and manipulation logic in `commonMain`; keep platform-specific code in `androidMain` / `iosMain` only.
- Versioning rule (Android + iOS sync) remains mandatory — version bumps are mechanical and require corresponding iOS version update.

## Architecture Overview

The project follows the standard Kotlin Multiplatform structure with a layered architecture:

- `up_layout` is the future library KMP module (the `UserPositionedLayout` container itself).
  - Source sets are split into `commonMain`, `androidMain`, and `iosMain`.
  - All container state/model (position, rotation, size), constraints, pure transformation math, and gesture/interaction logic live in `up_layout/commonMain` so behavior is identical on Android and iOS and it can be covered by `up_layout/commonTest`.
  - `up_layout` must not depend on `shared`, `androidApp`, or demo code. It only depends on Compose Multiplatform UI artifacts.
  - It is built as an Android multiplatform library (AAR) plus static iOS frameworks (`UpLayout`); later it will be published as a library without structural changes.
- `shared` is the shared KMP demo module that consumes `up_layout` and showcases layout functions.
  - Source sets are split into `commonMain`, `androidMain`, and `iosMain`.
  - Main layers in `commonMain`:
    - `presentation`: demo screens consuming the `UserPositionedLayout` container from `up_layout`.
    - `di`: dependency wiring (if/when DI is introduced, follow one module-per-area pattern).
  - `androidMain` / `iosMain`: platform `actual` implementations and entry points only (e.g. `Platform.*`, `MainViewController`). No business logic here.
  - The `Shared` iOS framework exports `up_layout`, so `iosApp` only embeds `:shared:embedAndSignAppleFrameworkForXcode` and Swift only imports `Shared`.
- `androidApp` is a thin Android application shell (demo host) that consumes `shared` and holds platform app config (`AndroidManifest.xml`, app resources).
- `iosApp` is a thin iOS host app (Swift/Xcode project) that embeds the shared framework.
- Gradle enforces layer direction: `androidApp -> shared -> up_layout`, `shared/commonMain` must not depend on platform source sets; platform source sets depend on common; `up_layout` depends on nothing in this repo.

## Feature Conventions

All new features must follow the existing architecture and boundaries:

- Add or extend transform state/model contracts first (position, rotation, size, constraints) in `up_layout/commonMain`, then implement gesture/interaction logic in `up_layout/commonMain`, then add platform `actual`s only if needed, then consume in `shared` demo `presentation`.
- Keep transformation math pure and platform-independent so it can be covered by `commonTest`.
- Keep platform-specific code in `androidMain` / `iosMain`; keep business logic in `commonMain`.
- Avoid cross-layer shortcuts (for example, UI directly performing unvalidated state mutation bypassing the transform model).
- When a DI framework is introduced, reuse one consistent pattern (`*Module.kt`, interface + implementation) and wire by feature area.

## Navigation Conventions

Demo/sample navigation routes should follow this:

- All navigation parameters should be serializable.
- Never pass lambda as parameter to navigation route.
- Navigation graph should not have loops or any type of dead routes.

## Branching Convention

- Long-lived branches:
  - `main`
  - `development`
- Release branches:
  - `rel/<major>.<minor>.<patch>` (e.g. `rel/1.1.6`)
- Use `development` as the default integration base unless explicitly instructed otherwise.

## Design Guidelines

- By default, all design changes must correspond to the current application theme and design language.
- By default, all design changes should follow general Material Design UI/UX recommendations, unless explicitly specified otherwise.
- Prefer flexible layout composition using `Alignment` and `Arrangement` instead of hardcoded spacing values (for example, fixed `padding`) whenever possible, so UI adapts better to different screen sizes. This is especially important for a resizable/rotatable container.
- Interaction affordances (drag, rotate, resize handles) must remain discoverable and usable at different container sizes and densities.
- Do not use app or release versions as default labels in design deliverables. If a version label is required, ask the user for the exact version first.

## Versioning Rule (Android + iOS must match)

Android and iOS app versions must always be kept in sync in the same change — strict sync is required on every version bump:

- Android version values currently defined in:
  - `androidApp/build.gradle.kts` (`versionCode`, `versionName`).
- iOS version values currently defined in:
  - `iosApp/Configuration/Config.xcconfig` (`MARKETING_VERSION`, `CURRENT_PROJECT_VERSION`).

When updating release version, update both Android and iOS version definitions together in the same change.

## Workflow Rules

- Never commit changes unless the user explicitly asks to commit (`git add` is not a commit).
- All newly created source/build project files in tracked source sets (`shared/`, `up_layout/`, `androidApp/`, `iosApp/` stubs, `gradle/` — for example: `.kt`, `.json`, resource files) must be added to git immediately after creation if parent directory is not in gitignore. This scoping excludes `agentsResults/` (never add unless user explicitly asks per rule below).
- The root `features/` directory is reserved for discussing and describing feature implementation in the project.
- Every new feature must be described in its own Markdown file inside `features/`.
- Every new feature must have its own child directory inside `features/` (for example, `features/positioning/`).
- All files related to a feature (plans, specs, notes, and other feature artifacts) must be placed inside that feature child directory.
- Files created in `features/` must use lowercase snake_case file names.
- Every Markdown file created in the repository must include a "Last update date" field (this covers `features/` Markdown files as well — no separate rule needed).
- Service Markdown files in `agentsResults/` created by agents for investigation reports, planning notes, and audits must not be added to git unless the user explicitly asks for it.
- Markdown files or other files created for agentic work must be placed in `agentsResults/` (or a relevant subfolder there) instead of project source directories.
- All files created in `agentsResults/` must use lowercase snake_case file names.
- All new code must be minimalistic and human-readable.
- Apply SOLID, KISS, and DRY principles in all new code. When SOLID/DRY tensions with `Code Simplicity and Minimalism` (below), Minimalism governs — apply SOLID pragmatically and introduce abstractions (interfaces, indirection) only when they solve a current, concrete problem.
- Any project-wide investigation output (for example: bug checks, architecture reviews, broad Q&A about the whole project, or development plans) must be summarized into a topic-specific Markdown file.
- Reuse an existing relevant report file when it matches the same investigation topic.
- If no relevant report file exists yet, create a new Markdown file in `agentsResults/` and place the summary there.
- If an investigation cannot find enough related information, or no reliable conclusion can be made, the agent must explicitly report this negative result in the output/report.
- Do not hide uncertain outcomes: investigations are allowed to be unsuccessful, and that status must be documented clearly.

## Code Simplicity and Minimalism

- Prefer the simplest solution that correctly meets current requirements. Don't add complexity unless required by an existing use case.
- Every extra line increases maintenance, cognitive load, and bug risk. The burden of proof is on complexity, not simplicity.
- Write the minimum amount of code, prefer readability over cleverness, and avoid defensive programming "just in case."
- Use `try/catch` only when an exception can be meaningfully handled. Do not add unnecessary null checks or redundant state validations.
- Assume single-threaded execution unless true concurrent writes exist; don't introduce mutexes or atomic types hypothetically.
- Trust Kotlin's type system, avoid unnecessary null-safety boilerplate, and keep conditions simple (e.g., don't write redundant checks).
- Avoid extra coroutine scopes or dispatcher switches without a concrete need.
- Don't add new abstractions unless they solve a real existing problem. Avoid redundant validation.
- Log only what has diagnostic or business value, and avoid premature optimization.
- When generating code, always prefer the simplest correct implementation, and do not add defensive programming, synchronization, abstractions, or validations without clear justification.
