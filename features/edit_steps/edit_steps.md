# Edit steps for move, rotation, and scale

Last update date: 2026-09-28

## Goal

Quantize all three transforms to user-visible steps and let the user
change them from the demo:

- Move step: existing `snapStepDp` (default 20, unchanged).
- Rotation step: new `rotationStepDegrees` (default 4 degrees).
- Scale step: new `scaleStepDp` (default 4 dp, sizes snap to multiples).
- Demo bottom bar (edit mode only) shows the three step values with
  `-` / `+` controls.

## Model

`up_layout/src/commonMain/kotlin/com/mdsw/uplayout/UpModel.kt`:

- `UpEditSettings` is the edit configuration: `showGrid`, `snapToGrid`,
  `snapStepDp = 20` (move), `visibleStepDp`, plus
  `rotationStepDegrees = 4f` and `scaleStepDp = 4`.
- Move default stays 20 for backward compatibility; only the two new
  steps default to 4.

## Snapping math

- `resolveScaledSize(..., stepDp = 0)`: after the zoom multiply and min
  clamp, each side snaps via `roundIntToStep(value, stepDp)` when
  `stepDp > 1`, then re-clamps to `minSizeDp`.
- `resolveRotationDegrees(..., stepDegrees = 0f)`: normalizes to
  `[0, 360)`, then snaps to `round(value / step) * step` and
  re-normalizes (360 wraps to 0) when `stepDegrees > 0`.
- Function defaults mean "no snap" so existing unit tests keep passing;
  `UpLayout` passes the configured steps.
- Covered by new tests: scale step snap + min, rotation step snap +
  wrap-to-zero (`UpScaleMathTest`, `UpRotationMathTest`).

## Gesture wiring

`up_layout/src/commonMain/kotlin/com/mdsw/uplayout/UpLayout.kt`
(`EditModeLayout`, container multitouch handler):

- Reads steps from `latestSettings` (`rememberUpdatedState(settings)`) so demo
  changes apply without restarting the gesture; drag snap also moved
  from stale `settings` to `latestSettings`.
- SCALE branch passes `stepDp = latestSettings.scaleStepDp`.
- ROTATE branch passes `stepDegrees = latestSettings.rotationStepDegrees`.
- First-intention lock (`resolveTransformLock`) is unchanged.

## Demo wiring

`shared/src/commonMain/kotlin/com/mdsw/usrpsnlyt/App.kt`:

- `editSettings` state (`remember { mutableStateOf(UpEditSettings()) }`)
  passed as `settings = editSettings` to `UpLayout`.
- `Scaffold(bottomBar)` shown only in edit mode: `Move` / `Rotate` /
  `Scale` step controls displaying current values, `-`/`+` adjust by
  1 (move/scale dp, rotation degrees), minima coerced to 1 / 1f.

## Verification

- `./gradlew :up_layout:check :shared:testAndroidHostTest` — scale,
  rotation, lock, and drop math plus host tests pass.
- Manual: edit mode shows bottom step bar; lower scale step to 1 for
  smooth resize, raise rotation step to 15 for 15-degree detents, change
  move step and drag to confirm padding snaps to it.
