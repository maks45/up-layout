# UpLayout API (rename + edit/view mode + content list)

Last update date: 2026-09-28

## Goal

Simplify the library entry point and make edit vs view behavior explicit:

- Rename `UserPositionedLayout` composable to `UpLayout` (matches `up_layout` module and `UpLayout` iOS framework baseName, shorter call sites).
- Expose `isEditMode: Boolean = true`: when true the layout is editable, when false it is a regular box layout with given constraints.
- Accept content as a list of composable functions with `BoxScope` receiver, one per item: `content: List<@Composable BoxScope.() -> Unit>`. For each content entry, `UpLayout` provides a `Box` with the given position (`alignment` + `padding`) and size (`widthDp` / `heightDp`).

## API

`up_layout/src/commonMain/kotlin/com/mdsw/uplayout/UpLayout.kt`:

- `UpLayout(items, onItemChanged, modifier, settings, centerDeadZone, isEditMode, onItemClick, configStore, content)`.
- `require(items.size == content.size)` — index `i` of `items` pairs with index `i` of `content`.
- Per-item wrapper is `key(item.id)` so drag state resets per identity, not per full data change.
- Shared bounds mapping lives in `Modifier.upPlacedItem` (alignment + bounds via `upItemBounds`).

## Edit / view mode contract

- `isEditMode = true` -> `EditModeLayout`: dashed border, alignment grid, snap dot grid, selection frames (unselected thin dashed gray, selected solid primary 2.dp, none selected by default; tap/drag/press selects, tap on empty area clears), single-finger drag (`detectDragGestures` + `resolveDrop` on drag end), container-level two-finger pinch scale (`resolveScaledSize` -> `widthDp`/`heightDp`) + twist rotate (`resolveRotationDegrees` -> `rotationDegrees`) on the selected item with first-intention lock per interaction (`resolveTransformLock`), tap overlay (`onItemClick`). Container `modifier` gains edit affordances. See `features/selection_scaling/selection_scaling.md` and `features/rotation/rotation.md`.
- `isEditMode = false` -> `ViewModeLayout`: plain `Box`, same alignment/padding/size per item, no gestures or affordances, caller `modifier` passed through untouched.
- Demo toggles the flag via the overflow menu item in `shared/.../App.kt`: "Edit layout" when in view mode, "View layout" when editing. The flag uses `rememberSaveable`, so the mode survives configuration changes (e.g. rotation).

## Frame model

`up_layout/src/commonMain/kotlin/com/mdsw/uplayout/UpModel.kt`:

- `UpItem` is the per-item class: element `id`, `padding` values, size (`widthDp` / `heightDp`), `rotationDegrees`, and `alignment`.
- `UpScreenConfig` is the screen configuration: holds all items in `items: List<UpItem>` (serialized as `frames` for backward compatibility).
- `UpEditSettings` is the edit configuration: `showGrid`, `snapToGrid`, move step `snapStepDp = 20`, `visibleStepDp`, rotation step `rotationStepDegrees = 4f`, scale step `scaleStepDp = 4`. See `features/edit_steps/edit_steps.md`.
- Rotation renders via `Modifier.rotate()` in the shared `upItemBounds` mapping, so edit and view mode stay consistent.

## Persistence

- `UpScreenConfigStore` (`UpScreenConfigStore.kt`) is the configuration saver interface: `suspend save(config)` writes the screen configuration to a JSON file, `suspend load()` reads it back (empty config when no file exists or content is corrupt).
- `UpModel` classes are `@Serializable`; JSON codec (`ignoreUnknownKeys = true`) lives in `commonMain`.
- Platform file locations via expect/actual: `rememberUpScreenConfigStore(fileName)` — Android writes to app `filesDir`, iOS to the documents directory. No changes needed in `androidApp` / `iosApp` shells.
- `UpLayout(..., configStore)` uses it directly: on first composition it loads and replays saved items through `onItemChanged` (matching ids only); every `items` change after restore is saved back. Saving starts only after restore completes, so defaults never overwrite a saved file. `null` (default) disables persistence.

## Demo wiring

- `shared/.../App.kt` holds an `UpScreenConfig` state and passes `config.items` plus `configStore = rememberUpScreenConfigStore()` to `com.mdsw.uplayout.UpLayout` (two `240x140` dp items) with `content = listOf({ Text("Drag me (a)") }, { Text("Drag me (b)") })`. Drag positions survive app restart. Edit mode also holds `UpEditSettings` state and passes it as `settings`; the bottom bar exposes move/rotate/scale step values with `-`/`+` controls.
- Demo texts fill their box (`fillMaxSize`) and autoscale to the largest fitting font (`TextAutoSize.StepBased()`, single line, centered).

## Verification

- `./gradlew :up_layout:check :shared:testAndroidHostTest` — pure `resolveDrop` + `resolveScaledSize` + `resolveRotationDegrees` + `resolveTransformLock` math plus host tests pass.
- Manual: toggle edit mode, drag both boxes, confirm drop persists alignment + padding and view mode renders same positions without grids/borders.
