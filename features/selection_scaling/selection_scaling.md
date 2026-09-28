# Selection in edit mode + pinch scaling

Last update date: 2026-09-28

## Goal

Add selection state to edit mode in `UpLayout`:

- No frame selected by default.
- Unselected item shows a thin dashed frame.
- Selected item (by user touch) shows a thin solid frame, thicker than unselected.
- Selected item can be scaled up/down with a two-finger pinch.

## Behavior contract

`up_layout/src/commonMain/kotlin/com/mdsw/uplayout/UpLayout.kt` (`EditModeLayout`):

- Internal `selectedId: String?` (`remember`, default `null` = none selected).
- Press on item selects it immediately (per-item `awaitFirstDown(requireUnconsumed = false)`, no consume so tap/drag still work), tap overlay also selects + calls `onItemClick(item)`, drag start selects too.
- Pinch scales the currently selected item (container-level handler, so the second finger may land outside the item bounds).
- Tap on empty container area clears selection (`detectTapGestures(onTap = { selectedId = null })` on the container; item taps are consumed by the child overlay so they do not clear).
- View mode has no selection affordance (unchanged).

## Visuals

- Unselected: `Modifier.dashedItemBorder(Color.Gray)` (1.dp dashed `drawRect`, `dashPathEffect(10f, 10f)`).
- Selected: `Modifier.border(2.dp, MaterialTheme.colorScheme.primary)` (solid, thicker than unselected).
- Shared container dashed border / alignment grid / snap grid unchanged.

## Scaling

- New pure math `up_layout/src/commonMain/kotlin/com/mdsw/uplayout/UpScaleMath.kt`:
  - `resolveScaledSize(currentWidthDp, currentHeightDp, fallbackWidthDp, fallbackHeightDp, zoom, minSizeDp = 32)`.
  - Scales width/height uniformly by `zoom`, falls back to measured content size when `widthDp`/`heightDp` is null, clamps to `minSizeDp`, ignores `zoom <= 0`.
- Gesture: container-level `pointerInput` uses `awaitEachGesture { awaitFirstDown(requireUnconsumed = false); ... }` + `calculateZoom()`:
  - Per-item pinch missed on real devices when the second finger lands outside the small item bounds, so the container handles the gesture and scales the selected item (pinch anywhere scales the selection).
  - Only acts when `>= 2` pointers pressed and never consumes single-finger events, so item `detectDragGestures` drag and tap overlay keep working.
  - Each zoom delta calls `onItemChanged(item.copy(widthDp, heightDp))` for live resize; drag drop (`resolveDrop`) is unchanged.
  - Fallback size comes from per-item `onGloballyPositioned { it.size }` stored in a `childSizes` map (unrotated layout size) minus item padding, converted px -> dp.
  - Uses `rememberUpdatedState` for `items` / `onItemChanged`; `selectedId` is read live from the remembered state so a press-to-select on first down applies to the same pinch.
- Covered by `up_layout/src/commonTest/kotlin/com/mdsw/uplayout/UpScaleMathTest.kt` (scale up/down, null fallback, min clamp, non-positive zoom).

## Verification

- `./gradlew :up_layout:check :shared:testAndroidHostTest` — pure `resolveDrop` + `resolveScaledSize` math plus host tests pass.
- Manual: enter edit mode, confirm no selection, tap item A -> solid primary 2.dp frame, item B stays dashed gray; pinch selected item scales live and persists via `configStore`; tap empty area clears selection.
