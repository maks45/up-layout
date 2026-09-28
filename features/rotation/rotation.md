# Two-finger rotation of selected frame

Last update date: 2026-09-28

## Goal

Rotate the selected frame with a two-finger twist, using the same
container-level multitouch path as pinch scaling:

- Only the selected item rotates (none selected by default, see
  `features/selection_scaling/selection_scaling.md`).
- Twist anywhere on the container rotates the selection, so the gesture
  works even when the second finger lands outside the item bounds.
- First intention wins per interaction: scaling locks out rotation and
  rotation locks out scaling until all fingers lift.

## Behavior contract

`up_layout/src/commonMain/kotlin/com/mdsw/uplayout/UpLayout.kt`
(`EditModeLayout`, container `pointerInput`):

- `awaitEachGesture { awaitFirstDown(requireUnconsumed = false); ... }`
  so press-to-select on first down applies before the second finger lands.
- When `>= 2` pointers pressed, read both `calculateZoom()` and
  `calculateRotation()` (degrees) from the same `PointerEvent` and
  accumulate (`accumZoom *= zoom`, `accumRotation += rotationDelta`).
- `resolveTransformLock(accumZoom, accumRotation)` (3% zoom vs 3 degrees,
  dominant wins ties) decides once per interaction; afterwards only the
  locked mode is applied (SCALE ignores rotation, ROTATE ignores zoom).
- Before the lock fires nothing is applied (threshold doubles as touch
  slop); the lock resets when all fingers lift.
- Single-finger events are never consumed, so drag (`detectDragGestures`)
  and tap overlay keep working; two-finger events are consumed.
- Rendering via existing `Modifier.rotate()` in `upItemBounds`, so edit
  and view mode stay consistent.

## Rotation math

- New pure math `up_layout/src/commonMain/kotlin/com/mdsw/uplayout/UpRotationMath.kt`:
  - `resolveRotationDegrees(currentDegrees, deltaDegrees)` adds the delta
    and normalizes to `[0, 360)`.
- New pure lock `up_layout/src/commonMain/kotlin/com/mdsw/uplayout/UpTransformLock.kt`:
  - `resolveTransformLock(accumZoomFactor, accumRotationDegrees)` returns
    `SCALE` / `ROTATE` / `null` (undecided), dominant score wins ties.
- Covered by `up_layout/src/commonTest/kotlin/com/mdsw/uplayout/UpRotationMathTest.kt`
  (addition, wrap over 360, negative wrap, zero-delta normalization) and
  `UpTransformLockTest.kt` (undecided, scale lock, rotate lock, dominant).

## Verification

- `./gradlew :up_layout:check :shared:testAndroidHostTest` — `resolveDrop` +
  `resolveScaledSize` + `resolveRotationDegrees` + `resolveTransformLock`
  math plus host tests pass.
- Manual: tap item to select (solid frame), pinch -> scales without
  rotating; lift fingers, twist -> rotates without scaling; tap empty area
  clears selection.
