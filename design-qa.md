# Memory Pinboard Design QA

Reference: `C:\Users\Ralfg\Documents\Codex\2026-07-01\richte-eine-geplante-aufgabe-ein-die\.codex-remote-attachments\01a01b0e-fb62-7571-8cb2-24b15e088875\3e835fcd-776d-44e2-945f-d0074e474b6d\2-Photo-2.jpg`

Implementation capture: `app\build\outputs\roborazzi\memory-pinboard\01-current-populated.png`

Viewport: Pixel 8, 1078 x 2399 px.

## Result

- The centered Harmony mark, title, couple avatars, subtitle, search, compact category rail, featured link card, two-column note grid, and floating add action follow the selected reference hierarchy.
- The couple's own link note is now the primary card copy. Preview title and raw URL are reduced to secondary, single-line metadata.
- Note cards remain readable in the adaptive two-column grid and retain Aurora glass borders, category accents, and completion controls.
- The add/edit sheet opens nearly full-height and exposes every editable field rather than leaving cards as read-only summaries.
- Selection mode is accessible from the search row or by long-pressing a card, clearly shows the selected count, and exposes select-all, delete, and close actions.
- `Filme` and `Serien` are represented by one `Filme & Serien` category, while all categories remain horizontally scrollable.

## Intentional product differences

- `Aktuell` and `Erledigte Notizen` remain above search because completed-history is a production requirement that is not represented in the visual reference.
- The selected production launcher artwork is reused for the pinboard mark, so the header carries the exact current Harmony identity.
- Link artwork is dynamic. The capture intentionally exercises the offline fallback; a resolved YouTube or article preview occupies the same reserved image area.
- Default and custom categories are managed by selecting a category and tapping the selected chip again. This keeps the reference's clean chip rail without adding a permanent pencil button.

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
