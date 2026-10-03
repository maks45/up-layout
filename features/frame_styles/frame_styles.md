# Frame and grid styles via BorderStroke

Last update date: 2026-09-29

## Goal

Let the library consumer restyle every edit-mode affordance — unselected
frame, selected frame, snap guides, alignment grid, dot grid — including
colors, through the existing `androidx.compose.foundation.BorderStroke`
instrument (width + brush, brush carries color or gradient) instead of a
custom style class.

## Model

`up_layout/src/commonMain/kotlin/com/mdsw/uplayout/UpModel.kt`:

- `UpEditSettings` gains five nullable overrides, all defaulting to `null`
  (null keeps the exact built-in rendering, including the theme-primary
  defaults resolved at composition time):
  - `frameBorder` — unselected item frame.
  - `selectedFrameBorder` — selected item frame.
  - `snapGuidesBorder` — item-to-bound snap guides.
  - `alignmentGridBorder` — center alignment lines.
  - `gridBorder` — dot grid; brush is the dot color, width is the dot
    diameter.
- Example: `editSettings.copy(frameBorder = BorderStroke(1.dp,
  SolidColor(Color.Red)))`.

## Rendering

- `UpDecorations.dashedBorderStroke(border)` draws a dashed rect from a
  `BorderStroke`'s brush + width with the existing `UpDashIntervals`, so the
  unselected frame override keeps the dashed look with a custom color/width.
- `drawAlignmentGrid(..., border?)` and `drawSnapGrid(..., border?)` branch
  per primitive: null runs the unchanged color path; non-null uses
  `border.brush` + `border.width` (alignment lines stay solid, dots use
  width / 2 as radius).
- `SnapGuidesOverlay(..., border?)`: null keeps color + fixed `2f` px dashed
  lines; non-null uses brush + density-converted width with the same dash.
- `UpLayout.EditModeLayout`: unselected frame is `dashedBorderStroke` when
  overridden else gray dashed; selected frame is `Modifier.border(border)`
  (solid, natively supported by `BorderStroke`) when overridden else solid
  primary `2.dp`. Gated by `showFrameBounds` as before; view mode unchanged.

## Demo wiring

No demo change: `shared/.../App.kt` already holds `UpEditSettings` state and
passes it as `settings`, so any consumer override flows through. Demo style
controls (e.g. color pickers) are a separate follow-up.

## Verification

- `./gradlew :up_layout:check` passes (common + iOS simulator tests).
- `./gradlew :androidApp:assembleDebug` passes (consumers compile against the
  new fields).
- Manual: default settings render pixel-identical to before; setting e.g.
  `selectedFrameBorder = BorderStroke(3.dp, SolidColor(Color.Magenta))`
  recolors/resizes only the selected frame.
