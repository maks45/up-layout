# Center snap zone (fraction-based)

Last update date: 2026-10-03

## Goal

Let the user resize the CENTER snapping area — the band where a drop snaps
to `CENTER` — as a percent of the container width/height, independently per
axis, with one call that sets both axes at once. The center alignment grid on
screen must reflect the zone live.

## Naming

Proposed `centerAlignmentPercent` was not used. Fractions (0..1) are used
instead of percents (0..100) so no `/ 100` conversions exist in math or
drawing, matching Compose conventions (`Modifier.fillMaxWidth(fraction)`).
The demo still shows `%` to the user; only the stored unit is a fraction.

- `UpCenterZone(horizontal, vertical)` — zone width as a fraction of the
  container width, zone height as a fraction of the container height.
- `UpCenterZone(all)` — secondary constructor overriding both axes at once.
- `UpCenterZone.Default` — 5% (`0.05f`), preserves the old `20.dp` horizontal
  behavior on a `400.dp`-wide container.

## Model

`up_layout/.../UpModel.kt`:

- `UpCenterZone` data class (plain, not serialized).
- `UpEditSettings.centerZone: UpCenterZone = UpCenterZone.Default`, next to
  the other snap/grid policy (`snapToGrid`, `snapStepDp`, ...). No new
  `UpLayout` parameter: settings is already the per-layout edit config, so
  the removed `UpLayout(centerDeadZone: Dp = 20.dp)` collapses into it.

## Math

`up_layout/.../UpDropMath.kt`:

- `resolveDrop(..., centerZone: UpCenterZone = UpCenterZone.Default, ...)`
  replaces `centerDeadZoneDp: Float = 20f`.
- Half-bands are per axis: `halfW = width * horizontal / 2`,
  `halfH = height * vertical / 2` (each fraction coerced to `0..1`).
- `0` means only the exact center snaps; `1` means the whole axis snaps.

## Rendering

`up_layout/.../UpDecorations.kt`:

- `drawAlignmentGrid(color, centerZone, ...)` replaces `centerSize: Dp`; grid
  lines sit at `size * (0.5 - fraction / 2)` per axis, so they track the zone
  on any container size with no density math.
- `upEditContainer(settings, ...)` reads `settings.centerZone` directly (one
  fewer parameter).

## Demo wiring

`shared/.../App.kt` bottom bar (edit mode only) gains a second row:

- `Center X` / `Center Y` steppers, `±1%`, clamped to `0..100%`, wired via
  `editSettings.copy(centerZone = centerZone.copy(...))`.
- Labels added to `composeResources/values/strings.xml`:
  `center_zone_x`, `center_zone_y`.

## Verification

- `./gradlew :up_layout:allTests` — `UpDropMathTest` gains uniform-constructor,
  wide-zone, zero-zone, and axis-independence cases; `UpModelTest` pins the
  `5%` default. Pre-existing drop tests pass unchanged.
- `./gradlew :androidApp:assembleDebug` — demo with the new steppers compiles.
- Manual: drag an item, widen Center X/Y, confirm the grid spreads and drops
  near the middle resolve to CENTER; set `0%` and confirm only exact-center
  drops stay centered.

## Migration (breaking)

- `UpLayout(..., centerDeadZone: Dp)` removed; set
  `settings = settings.copy(centerZone = ...)` instead.
- `resolveDrop(..., centerDeadZoneDp: Float)` replaced by
  `resolveDrop(..., centerZone: UpCenterZone)`; convert with
  `UpCenterZone(xDp / containerWidthDp, yDp / containerHeightDp)`.
