# Lock snaps

Last update date: 2026-09-29

## Goal

Let the user lock the snapped alignment: when `lockSnaps` is enabled, a drag
keeps the frame's current alignment and only recomputes that alignment's
paddings from the drop position. The frame can still move, but it never
changes snaps.

## Model

`up_layout/src/commonMain/kotlin/com/mdsw/uplayout/UpModel.kt`:

- `UpEditSettings` gains `lockSnaps: Boolean = false` (default off, current
  behavior preserved), grouped with the other transform locks.
- Unlike `lockMove` (which freezes the drag entirely), `lockSnaps` allows
  movement and only locks the snapped alignment.

## Interaction

`up_layout/src/commonMain/kotlin/com/mdsw/uplayout/UpDropMath.kt` (pure) +
`UpLayout.kt` (`EditModeLayout`):

- `resolveDrop` gains `lockedAlignment: UpAlignment? = null`: when provided,
  the center-based alignment resolution is skipped and only the paddings of
  that alignment are recomputed from the drop rect (grid rounding via
  `snapToGrid` still applies).
- `onDragEnd` passes the frame's pre-drop alignment as `lockedAlignment` when
  `lockSnaps` is set; the `lockMove` gate is unchanged.
- Uniform emergent behavior per alignment, no special-casing: CENTER has no
  paddings and stays put; TOP/BOTTOM/START/END move along their single free
  axis; corners move within their corner.

## Demo wiring

`shared/.../App.kt` overflow menu (edit mode only):

- `Lock snaps` -> `editSettings.copy(lockSnaps = ...)` via the existing
  `ToggleMenuItem`, placed after the other locks.
- Label added to `shared/.../composeResources/values/strings.xml`:
  `lock_snaps`.

## Verification

- `./gradlew :up_layout:check` passes, including the new `UpDropMathTest`
  cases (locked alignment keeps alignment and recomputes its paddings;
  locked CENTER stays put with zero padding).
- `./gradlew :androidApp:assembleDebug` passes (shared compiles with the new
  resource).
- Manual: enable Lock snaps, drag a corner frame — it moves but keeps its
  corner; drag a CENTERed frame — it stays put; disable to change snaps
  again.
