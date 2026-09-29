# Frame bounds toggle

Last update date: 2026-09-29

## Goal

Expose a `show frame bounds` toggle so the user can hide all per-item frame
borders in edit mode. When disabled, no item bounds are drawn — neither the
gray dashed unselected frame nor the solid primary selected frame.

## Model

`up_layout/src/commonMain/kotlin/com/mdsw/uplayout/UpModel.kt`:

- `UpEditSettings` gains `showFrameBounds: Boolean = true` (default on,
  preserves current rendering).

## Rendering

`up_layout/src/commonMain/kotlin/com/mdsw/uplayout/UpLayout.kt`
(`EditModeLayout` only):

- Per-item border modifier becomes a three-way branch: when
  `settings.showFrameBounds` is false, no border modifier is applied at all;
  otherwise the existing selected (solid primary `2.dp`) vs unselected
  (gray dashed) frames are drawn as before.
- Visual-only change: selection state, drag, pinch scale/rotate, tap overlay,
  and snap guides are unaffected — items stay manipulable while borderless.
- Container dashed border and view mode are unchanged.

## Demo wiring

`shared/.../App.kt` overflow menu (edit mode only):

- `Show frame bounds` -> `editSettings.copy(showFrameBounds = ...)` via the
  existing `ToggleMenuItem`, placed with the other visibility toggles.
- Label added to `shared/.../composeResources/values/strings.xml`:
  `show_frame_bounds`.

## Verification

- `./gradlew :up_layout:check` passes (common + iOS simulator tests).
- `./gradlew :androidApp:assembleDebug` passes (shared compiles with the new
  resource).
- Manual: edit mode overflow menu hides/shows all item frames live; with
  bounds off, tapping/dragging items still selects and moves them.
