# UserPositionedLayout

Last update date: 2026-10-03

This is a Kotlin Multiplatform / Compose Multiplatform project targeting Android and iOS.

**UserPositionedLayout** is a composable container where the user can directly manipulate children inside the container:

- change position (drag / move),
- change rotation angle (rotate),
- change size (resize / scale).

Additional manipulation functions will be defined later.

* [/up_layout](./up_layout/src) is the library KMP module (the `UserPositionedLayout` container itself, distributed as a library via GitHub Releases).
  It contains several subfolders:
  - [commonMain](./up_layout/src/commonMain/kotlin) is for the container state/model, transformation math, and gesture logic common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.

* [/shared](./shared/src) is the shared KMP demo module that consumes `up_layout` and showcases layout functions.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for demo UI code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.

* [/androidApp](./androidApp) contains the Android demo host application that consumes `shared`.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

### Using the library in your KMP project

The library is published to GitHub Packages on every release
(see [Releasing](#releasing) below), so no manual download is needed.

1. Create a GitHub Personal Access Token with `read:packages` scope and save
   it outside version control in `~/.gradle/gradle.properties` (never commit
   tokens):

   ```properties
   githubUsername=your-github-username
   githubToken=your-token-with-read-packages
   ```

2. Register the GitHub Packages repository in your `settings.gradle.kts`:

   ```kotlin
   dependencyResolutionManagement {
       repositories {
           google()
           mavenCentral()
           maven(url = uri("https://maven.pkg.github.com/maks45/up-layout")) {
               credentials {
                   val githubUser: String by settings
                   val githubToken: String by settings
                   username = githubUser
                   password = githubToken
               }
           }
       }
   }
   ```

3. Add the dependency to your shared module's `commonMain`:

   ```kotlin
   kotlin {
       sourceSets {
           commonMain.dependencies {
               implementation("com.mdsw.uplayout:up_layout:0.1.0")
           }
       }
   }
   ```

   Replace `0.1.0` with the release version you want to use. If your project
   uses a version catalog (`gradle/libs.versions.toml`), declare it there
   instead:

   ```toml
   [versions]
   uplayout = "0.1.0"

   [libraries]
   uplayout = { module = "com.mdsw.uplayout:up_layout", version.ref = "uplayout" }
   ```

   ```kotlin
   commonMain.dependencies {
       implementation(libs.uplayout)
   }
   ```
   Gradle's variant-aware resolution picks the matching artifact per target
   (AAR on Android, klib on iOS), so a single `commonMain` dependency
   is enough.

   For fully offline use, each GitHub Release also attaches
   `up-layout-<version>.zip` (Maven repository layout): unzip it and point a
   `maven(url = uri(...))` repository at the unzipped directory instead of the
   GitHub Packages block above.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Library tests: `./gradlew :up_layout:iosSimulatorArm64Test`

### Releasing

Library releases are cut from Git tags on `main`. Pushing a tag `vMAJOR.MINOR.PATCH`
automatically builds `up_layout`, runs its checks, and creates a GitHub Release
with the library archive attached.

```bash
git checkout main
git pull
git tag v0.1.0
git push origin v0.1.0
```

The tag is the source of truth for the version: the leading `v` is stripped, so
tag `v0.1.0` produces library version `0.1.0` (Maven coordinates
`com.mdsw.uplayout:up_layout:0.1.0`). No version edit is needed before tagging;
local builds default to `0.0.0-SNAPSHOT`.

Tags must match `v<number>.<number>.<number>` exactly (no prerelease suffixes)
and must point to a commit contained in `main`; otherwise the workflow fails
before building.

The resulting release and its `up-layout-<version>.zip` package (Maven
repository layout with the root, Android, and iOS publications) can be found on
the repository's GitHub Releases page.

---

## Contributing

When contributing with agentic coding, follow the principles in [AGENTS.md](./AGENTS.md).

## License

This project is open source under the Apache License 2.0. See [LICENSE](./LICENSE).

Full license text: [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
