# Harmony complete visible-copy inventory and Japanese localization design

## Goal
Replace the old localization definition based on the historic `EXACT_ENGLISH_CONTENT` key set with a complete inventory of every Harmony-owned text that can become visible to a normal user, then make Japanese complete against that inventory.

The current 952-key figure is not a valid completeness metric and must no longer be used as the canonical count.

## Scope
Included:
- all customer-facing Compose UI text in Home, Games, Chat, Moments, Profile, navigation, dialogs, sheets, filters, empty states, errors, confirmations, buttons and intermediate screens
- every question, answer option, category, pack title, description, result text and generated/static game content
- full flows for all games, including Panda games, Das oder Das, Unterbewusstsein/Introspection and every screen reachable inside those flows
- dynamic text templates and composed sentences, including placeholders such as partner/user names, counts and formatted values
- accessibility/customer-visible descriptions where they form part of the app experience
- Android XML strings, notification/widget copy and any other app-owned resource text that can be rendered
- app-owned SVG text and text baked into bitmap artwork; these assets require localized variants or a text-free/localized rendering path where necessary
- all currently merged production code and assets on `main`, starting from commit `396f8f0623a021ddbe15365c987b2106db8e500a`

Excluded:
- Developer Studio / developer mode and its internal tooling copy
- internal IDs, stable keys, debug-only labels, source-code metadata and test-only text that cannot be shown in production UI
- third-party/system UI text that Harmony does not own
- brand names and brand logos. Names such as Coca-Cola, Netflix, McDonald's, PlayStation and iPhone remain in their original branding rather than being translated/transliterated.

## Canonical source of truth
German becomes the canonical localization source.

Completeness must never again be inferred from `EXACT_ENGLISH_CONTENT`, another locale catalog, or a previously generated translation map. Instead a generated German visible-copy inventory is authoritative.

Every inventory entry must contain at least:
- a stable inventory ID
- exact German source text/template
- source location(s)
- presentation type (UI, question, game option, dialog, dynamic template, XML, widget/notification, SVG/bitmap asset, etc.)
- placeholder signature where applicable
- whether the text is shared by multiple render locations
- whether the item is exempt and why

## Inventory discovery
Use a hybrid discovery pipeline rather than one regex over translation maps.

### 1. Static Kotlin/Compose discovery
Scan production Kotlin under `app/src/main/java` while excluding Developer Studio implementation paths. Detect text passed into customer-visible APIs such as `Text`, labels, placeholders, content descriptions, dialogs, snackbars/toasts, buttons and composed strings.

The scanner must also detect variables/data fields that become visible rather than considering only string literals.

### 2. Product-content discovery
Parse every production content source, especially the large generated question/game datasets such as `GeneratedHarmonyContent.kt`, installers, model defaults and game-specific content. Every displayed question, answer, title, description, result and instruction must enter the inventory.

### 3. Dynamic-template discovery
Treat complete visible templates as translation units, not isolated German words. Placeholder names and counts must be preserved exactly. Examples include partner/user name templates, counters and conditional sentences.

### 4. Android resource discovery
Include visible resource strings from `res/values`, layouts, widgets, notifications and other resource-backed UI.

### 5. Asset discovery
Inspect app-owned SVGs and images for visible text. Text-bearing SVG nodes are directly inventoryable. Bitmap images with intentional app-owned text must be manually registered/localized when static extraction cannot reliably prove the text content. Brand-logo text is exempt.

### 6. Route/runtime verification
Static discovery is necessary but not sufficient. Build a route/screen coverage manifest for normal-user product paths and use Japanese runtime/regression checks to detect German leakage on reachable screens. The supplied screen recording is a regression source, not the canonical source of completeness.

## Inventory metrics
The inventory report must publish three separate numbers:

1. **Unique visible translation units** — unique German strings/templates/assets that require localization.
2. **Visible render occurrences** — every concrete source/render location, even if several use the same text.
3. **German word count** — total words across the canonical German translation units, with dynamic placeholders treated as placeholders rather than words.

The repository must persist the generated report so future additions change these counts deterministically.

## Japanese target
Japanese is the first locale rebuilt against the new inventory.

Japanese is complete only when:
- every non-exempt inventory unit has a Japanese counterpart or localized asset
- every placeholder signature matches the German template exactly
- no customer-facing German Harmony copy remains on Japanese product routes
- game questions/options/instructions/results are localized, not just shell UI
- Unterbewusstsein/Introspection is localized from entry through completion/results
- dynamic values are inserted into natural Japanese sentence structures rather than word-for-word fragments
- Japanese typography/layout remains readable without clipping important controls
- brand names/logos remain unchanged unless surrounding Harmony copy is translated

## Architecture changes
1. Introduce a generated German visible-copy inventory as the canonical set.
2. Change localization completeness checks to compare locales against this inventory, not English keys.
3. Keep stable IDs/internal metadata separate from customer copy.
4. Route any newly discovered hardcoded production text through localization instead of merely adding it to an audit allowlist.
5. Extend asset localization support for text-bearing SVG/bitmap resources.
6. Add source and runtime leakage gates so a future German hardcoded string cannot silently bypass localization.

The existing locale files may be reused as translation material, but they are not trusted as evidence of completeness.

## Validation gates
Before Japanese can be called complete, all of the following must pass:
- inventory generator runs successfully on the current production tree
- inventory has no unexplained production-visible text omissions
- Developer Studio is excluded explicitly rather than accidentally
- Japanese inventory coverage is 100% for non-exempt entries
- placeholder/template parity is 100%
- static hardcoded-copy leakage audit has zero unlocalized customer-facing German findings
- runtime/route regression checks for Japanese have zero Harmony-owned German leakage on covered product paths
- text-bearing assets have Japanese variants/rendering or an explicit brand-only exemption
- `:app:compileDebugKotlin` succeeds

## Delivery sequence
Phase 1: build the complete German inventory and report its three final metrics.

Phase 2: review the inventory for false positives/exemptions and lock it as the new canonical baseline.

Phase 3: rebuild/fill Japanese against that complete baseline and route all leaked UI text through localization.

Phase 4: run static, asset, runtime and Android compile gates. Only then mark Japanese complete.

Other languages are intentionally not upgraded in this change until the German inventory and Japanese implementation prove the new architecture.

## Acceptance criteria
The work is successful when the user can switch Harmony to Japanese and traverse every normal-user feature outside Developer Studio without encountering Harmony-owned German text, including game content and intermediate screens, while the repository reports the exact canonical German inventory counts and prevents future untranslated additions from bypassing CI.
