# Grid settings toggles

Last update date: 2026-09-29

## Goal

Expose all edit-mode grid settings to the user from the demo overflow menu,
and add the missing visibility toggle for the center alignment grid.

## Model

`up_layout/src/commonMain/kotlin/com/mdsw/uplayout/UpModel.kt`:

- `UpEditSettings` gains `showAlignmentGrid: Boolean = true` (default on,
  preserves current rendering).
- Existing flags reused as-is: `showGrid` (dot snap grid), `showSnapGuides`
  (item-to-bound guides overlay), `snapToGrid` (drop-snapping behavior).

## Rendering

- `UpDecorations.drawAlignmentGrid(color, centerSize, showAlignmentGrid = true)`
  returns `this` unchanged when off, mirroring the existing `drawSnapGrid`
  early-return pattern.
- `UpLayout.EditModeLayout` passes `settings.showAlignmentGrid` through; dot
  grid and snap guides were already gated by `showGrid` / `showSnapGuides`.
- View mode draws nothing, unchanged.

## Demo wiring

`shared/.../App.kt` overflow menu (edit mode only):

- `Show grid` -> `editSettings.copy(showGrid = ...)`.
- `Show alignment grid` -> `editSettings.copy(showAlignmentGrid = ...)`.
- `Show snap guides` -> `editSettings.copy(showSnapGuides = ...)`.
- `Snap to grid` -> `editSettings.copy(snapToGrid = ...)`.
- `LockToggleItem(label, locked, onToggle)` generalized to
  `ToggleMenuItem(label, checked, onToggle)` and reused for grid + lock rows.
- Labels added to `shared/.../composeResources/values/strings.xml`:
  `show_grid`, `show_alignment_grid`, `show_snap_guides`, `snap_to_grid`.

## Verification

- `./gradlew :up_layout:check :shared:testAndroidHostTest` passes.
- Manual: edit mode overflow menu toggles each grid affordance independently;
  dot grid, center alignment lines, and snap guides hide/show live while
  `snapToGrid` only changes drop-snapping behavior.
