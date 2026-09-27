# Drag positioning port (IACreator -> up_layout v1)

Last update date: 2026-09-27

## Goal

Port the user-editable layout from the sibling `IACreator` project
(`app/.../screen/edit_screen/EditUIScreen.kt`, `DragAndDrop` composable) into
`up_layout/commonMain` as the first functional version of
`UserPositionedLayout`, and record the gaps that remain before the library
covers position + rotation + size.

## What IACreator does today

- Container `Box` with visual affordances: dashed border, center alignment
  grid (`drawAlignmentGrid`, 20.dp center zone), snap dot grid
  (`drawSnapGrid`, `GridSettings(showGrid, snapToGrid, snapGridSize=20,
  visibleGridSize=40)`).
- Each child is a `Box` positioned by `BoxScope.align` (9-way `UIAlignment`)
  plus padding offsets (`setAlign` / `setPaddings`) with fixed size
  (`setMeasurement`, width/height in dp).
- Drag is single-finger `detectDragGestures` with a live `IntOffset`; on
  `onDragEnd` the drop is persisted via `updateAlignmentAndPadding`:
  container/child `Rect`s (`boundsInParent`) -> nearest of 9 alignments
  (center zone 20.dp) + top/bottom/start/end padding ints (snapped to grid
  step when enabled).
- Tap overlay (`matchParentSize` + `clickable`) opens the property editor.
- State lives in `UIComponent.properties: List<IAData>` (generic string/int
  property bag) owned by `EditScreenViewModel`.

## What was ported (v1)

IACreator file -> `up_layout` equivalent:

- `UIAlignment` (9 values) -> `UpAlignment` (`UpModel.kt`, library-owned).
- `UIComponent` alignment/padding/measurement properties -> typed `UpItem`
  (`id`, `alignment`, `padding: UpPadding`, `widthDp`/`heightDp` nullable).
  The generic `IAData` property bag was deliberately NOT ported (IACreator
  dataset/binding concern, not layout concern).
- `GridSettings` -> `UpGridSettings` (same 4 fields).
- `updateAlignmentAndPadding` -> pure `resolveDrop` (`UpDropMath.kt`):
  all inputs/outputs are density-independent `Float` dp values, no
  `Density`/`Dp`/`Rect` in the signature, so it runs in `commonTest`.
  The Compose adapter (`UserPositionedLayout.kt`) converts px <-> dp.
- `DragAndDrop` -> `UserPositionedLayout(items, onItemChanged, ...)`
  (`UserPositionedLayout.kt`): same modifier order (align, padding, border,
  size, drag offset), same per-item drag state reset via `key(item)`, same
  tap overlay (`onItemClick`), same grid/border drawing (private modifiers).
- `UIAlignment.toAlignment()` -> private `UpAlignment.toComposeAlignment()`.

Deliberate behavior fix during port:

- IACreator `endP` was rounded to the grid step unconditionally
  (`UpdateAlignmentAndPadding.kt:63` ignores `snapToGrid`); the port rounds
  `endP` only when snapping is enabled, like the other three paddings.

## Edit / view mode

- `UserPositionedLayout(..., isEditMode: Boolean = true, ...)` splits into
  two paths: `EditModeLayout` (grids, borders, drag, tap overlay — the
  ported IACreator behavior) and `ViewModeLayout` (plain `Box`, same
  alignment/padding/size per item, no gestures or affordances, caller
  `modifier` passed through untouched).
- Shared alignment/padding/size mapping lives in `Modifier.upItemBounds`.
- Demo toggles the flag via the "Edit layout" overflow menu item.

## Gaps to cover next

1. **Rotate** — no rotation state, gesture, or handle (IACreator has none).
2. **Resize/scale** — size is fixed metadata (`widthDp`/`heightDp`); no
   pinch/corner-handle gesture (IACreator has none).
3. **Free positioning** — drop result is alignment enum + paddings, not a
   free `Offset`; pixel precision is lost by design (inherited limitation).
4. **Selection affordance** — only a gray border + invisible tap overlay;
   no visible selected state or drag/rotate/resize handles.
5. **Constraints** — no min/max size, no keep-in-bounds clamping (only
   `coerceAtLeast(0)` on paddings), no aspect-lock, no container limits.
6. **Z-order** — no bring-to-front / reorder on selection.
7. **Multi-touch** — single-finger drag only; no `TransformableState`.
8. **Persistence/serialization** — `UpItem` is not `@Serializable` yet.
9. **UI tests** — only pure `resolveDrop` math is covered by `commonTest`;
   no Compose UI test for the drag interaction.
10. **Demo structure** — `shared` demo drives the layout from `App.kt`
    directly; the `presentation` package split from `AGENTS.md` is still
    future work.
