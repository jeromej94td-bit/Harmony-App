# Memory Pinboard Design QA

References:

- `C:\Users\Ralfg\Documents\Codex\2026-07-01\richte-eine-geplante-aufgabe-ein-die\.codex-remote-attachments\01a01b0e-fb62-7571-8cb2-24b15e088875\7d598eb3-fb7f-4e7b-a4af-46c38bc1be27\1-Photo-1.jpg`
- `C:\Users\Ralfg\Documents\Codex\2026-07-01\richte-eine-geplante-aufgabe-ein-die\.codex-remote-attachments\01a01b0e-fb62-7571-8cb2-24b15e088875\431f56e9-31bd-4518-9388-c4ece820ac66\3-Photo-3.jpg`
- `C:\Users\Ralfg\Documents\Codex\2026-07-01\richte-eine-geplante-aufgabe-ein-die\.codex-remote-attachments\01a01b0e-fb62-7571-8cb2-24b15e088875\431f56e9-31bd-4518-9388-c4ece820ac66\4-Photo-4.jpg`

Implementation captures:

- `app\build\outputs\roborazzi\memory-pinboard\00-integrated-shell.png`
- `app\build\outputs\roborazzi\memory-pinboard\01-current-populated.png`
- `app\build\outputs\roborazzi\memory-pinboard\04-checklist-editor.png`

Viewport: Pixel 8, 1078 x 2399 px.

## Result

- The large duplicated launcher mark, avatars, and subtitle are removed from the pinboard hero. The title starts directly below the top bar, freeing substantial vertical space.
- The real Harmony launcher artwork sits at compact size immediately after the `HARMONY` wordmark in the Notes tab.
- A list is rendered as one full-width Aurora-glass card with square checkbox rows, not as a separate card for every line.
- Completed checklist rows are grouped below the active rows, muted, checked, and struck through while remaining reversible.
- The checklist editor starts at the top of the sheet, keeps title and active rows visible, and uses IME padding so the keyboard does not cover the form controls.
- Search, history tabs, category rail, card controls, and the floating add action remain visually and functionally consistent with the established Harmony design system.

## Comparison history

1. The original app capture showed a tall duplicated hero and individual cards for comma-separated list content.
2. The first implementation capture moved the title upward and placed the selected app icon beside the top-bar wordmark.
3. The checklist capture was compared together with the Keep reference. Active rows, add-row action, completed section, grey treatment, and line-through state match the requested hierarchy while retaining Harmony styling.

## Intentional product differences

- The Keep reference is used for checklist interaction and information order, not for its flat black visual theme. Harmony's Aurora glass surfaces, typography, and accent colors remain intact.
- Category and Save controls remain in the same editor rather than moving to a separate overflow menu, so the existing category workflow is preserved.
- The entire list card opens the full editor; checkbox edits are saved there to avoid accidental toggles while scrolling the pinboard.

final result: passed

---

# Harmony Image Choice Questions Design QA

Date: 2026-08-23

Source references:

- Egg and Harmony styling: `1-Photo-1.jpg` (592 x 1280)
- Travel content: `2-Photo-2.jpg` (720 x 1280)
- Steak content: `3-Photo-3.jpg` (720 x 1280)

Implementation captures:

- `app/build/harmony-image-choice-preview/egg-question.png`
- `app/build/harmony-image-choice-preview/steak-question.png`
- `app/build/harmony-image-choice-preview/travel-question.png`
- Combined evidence: `app/build/harmony-image-choice-preview/design-qa/`

Capture context: 411 dp wide extended mobile viewport, dark Harmony theme, animations advanced by 2200 ms. The travel capture includes the first card selected.

## Result

- The three views consistently use Harmony's dark violet glass, pink-purple glow, rounded cards, luminous selected border, and heart/check affordances.
- The 12 source motifs are preserved in the correct order and presented as a 3 x 4 grid, matching the approved domino sequence.
- The travel title is exactly `Wie sieht deine Traumreise aus?`; all option labels describe travel types rather than destinations.
- Typography is intentionally changed from the condensed poster lettering to Harmony's native Compose hierarchy for consistency and accessibility.
- Long German labels fit without ellipsis in the final full-scroll captures. Image crops contain no baked-in source labels or placeholder art.
- Selection is visibly distinct through a 2 dp pink border, pink check, and lifted pink-purple surface tint.

## Iteration history

- P2: Pixel 8 capture clipped the first or last edge of the long 3 x 4 component. Fixed with an extended mobile QA viewport; the production screen remains vertically scrollable.
- P2: Travel selection did not initially produce a stable captured state. Fixed by exposing the selected semantic tag and advancing the recomposition frame before capture.
- P2: Initial egg and steak crops retained fragments of poster text. Re-cropped the affected source cells and verified all 36 final assets.
- P2: Long travel detail lines needed more room. Increased card height and allowed three detail lines.
- P3: A Roborazzi multi-test render omitted the reused steak header vector once. An isolated re-record rendered the production icon correctly; no application-code defect reproduced.

Status: passed for local preview and implementation handoff. No open P0, P1, or P2 findings.
