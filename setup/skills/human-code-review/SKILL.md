---
name: human-code-review
description: |
  Use when reviewing code for "looks human-written" quality, hunting AI/port artifacts,
  or being asked to make code look like a programmer wrote it (not an LLM/transpiler).
  Triggers: "проведи ревью", "проверь код", "почисти", "как написано человеком",
  "не AI-стиль", "code review for human readability", "remove AI artifacts",
  "audit comments". Also auto-trigger when about to deliver a refactor of a codebase
  that was ported from another language/framework (RN→Native, Java→Kotlin, JS→TS, etc.).
---

# Human-grade code review

Goal: leave code that reads like a thoughtful programmer wrote it, not a tireless
machine that ports line-by-line. Catch the patterns LLMs and transpilers reliably
produce, fix them without inventing new ones.

This skill is a checklist + tactics. Apply patterns, don't recite them. When unsure
whether a finding matters, ask: **"would a senior engineer write a comment about
this in PR review?"** If no, skip it.

---

## Workflow

1. **Read the project's module map first** if one exists (`docs/`, `README.md`,
   `CLAUDE.md`). It tells you which files are owned by which layer — saves you
   from grep-spelunking.
2. **One pass = one category.** Don't mix "remove AI comments" with "refactor
   error handling" in the same edit batch. Easier to revert one bad pass.
3. **Compile + run tests after every category.** Especially after deletions.
4. **Grep before delete.** A symbol with no production use-sites may still be
   covered by tests — those tests freeze a contract. Decide explicitly: keep
   the symbol (and the test), or delete both. Never silently break tests.
5. **Update docs in the same commit** as the structural change. Stale doc
   pointers (file moved/deleted, symbol renamed) are themselves AI-style debt.

---

## Category 1 — Comments that smell of AI/port

These are the highest-signal artifacts. A human writes comments to explain
**why**, not to cross-reference the source they ported from.

**Red flags to grep:**
```
"(аналог .* в RN)"     "(equivalent of .* in X)"
"как в RN"              "as in the original"
"TS:"  "JS:"  "Py:"     "согласно .* (.tsx|.py|.java)"
"(\\.tsx|\\.jsx|\\.py|\\.java) "
"AsyncStorage" / "Reanimated" / "expo-" / "epubjs"  (when not actually used)
"// TODO: same as upstream"
```

Also kill:
- KDoc/JSDoc that just **restates the function name** ("Returns the user's name").
- Step-by-step running commentary ("Now we set X to Y. Then we call Z.").
- "Used by X" or "Added for issue #123" — belongs in the PR description.
- One-line wrappers with three-paragraph docstrings.
- Any comment that would *not surprise* a reader who already understands the code.

**Keep:**
- Constraints not visible in code (`// must run on main thread because Foo asserts`).
- Workarounds for specific bugs (`// crashes on Android < 8 due to bug 123456`).
- Non-obvious WHY ("polling because the SDK fires events before being ready").

**Tactic:**
1. Grep for the red-flag patterns above.
2. For each hit, rewrite the comment to state *why this code exists*, or delete it.
3. If the explanation is just "it ports X from Y" — delete. The reader doesn't have
   access to the original anyway.

---

## Category 2 — Silent `catch` and nested try

`try { … } catch (_: Exception) {}` is almost always a bug or laziness. Even when
the swallow is correct, the *silence* is wrong — losing a stack trace destroys
diagnostics.

**Decision tree per `catch`:**
1. Can the operation legitimately fail and the program continues correctly? → log
   at `warn` with enough context (which key, which file, which book id) to debug
   later. Never zero-info `Log.w("failed", e)`.
2. Is failure impossible by invariant? → don't catch. Let it propagate.
3. Is the catch wrapping a foreign exception in a domain one
   (`catch (e: SQLException) { throw StorageError(code, e) }`)? → fine, but make
   sure the domain message is a **stable code**, not a localized string.
4. Best-effort persistence (theme/locale write fails)? → silent OK, but add a
   one-line comment: `// best-effort: losing this preference is not fatal`.

**Nested try in one function** (`try { … try { … } catch … } catch …`) is a code
smell. Two patterns to refactor with:

- **`runCatching { … }.onFailure { log }`** for inner stages that don't change the
  outer flow. Cleaner than a fresh try-block, makes "I'm doing best-effort here"
  visible at the call site.
- **Extract the inner stage into a named function** that has its own try once. The
  outer code reads as "stage A → stage B → stage C" with catches matching stages.

A *cascade* of `try { … } catch (Domain) { throw } catch (Foreign) { throw Domain }`
is **not** nested — that's the legitimate "translate foreign into domain" pattern.
Don't refactor it just because it has multiple `catch` clauses.

---

## Category 3 — Dead code

Ports leave behind constants, enums, and helper functions that the new
framework's idioms don't need. Common shapes:

- **String icon names** (`object MaterialIcons { const val MENU = "menu" }`) when
  the new platform has a typed icon API.
- **Layout/StyleSheet token blocks** (40+ `*_DP` / `*_FONT_SP` constants) that
  the UI now sets inline.
- **Alpha-suffix strings** (`"33"`, `"55"`, `"99"`) for hex+alpha concatenation,
  unused when the new framework has `Color.copy(alpha = ...)`.
- **Tuning blocks** for old runtimes (`FlatListTuning`, `epubjs.bookReady`).
- **Pre-render escapes / fallbacks** for limitations the new platform doesn't have.

**Procedure for deletion:**
1. Grep the symbol across the whole repo.
2. **Tests count as use-sites.** A test on `chapterProgressLabel()` means the
   contract is asserted, even if no production code calls it. Decide:
   - Keep the symbol — it's part of the contract.
   - Delete the symbol *and* the test — explicitly.
   - Never just delete the production code and let the test compile-fail.
3. Compile + run tests after deletion. Re-grep for stale doc pointers.

**Especially watch:** big spec/contract objects often hold mostly-dead constants
once UI is rewritten. Don't trust file size — measure usage.

---

## Category 4 — DRY at the data layer

LLM-generated code loves to repeat data-class fields. Two specific patterns to
fix:

### Spec/screen objects with near-identical bodies
Three "spec" objects that differ only in an i18n key and one boolean → one
`data class` with named factories:

```kotlin
data class BookListScreenSpec(val emptyKey: String, val hasFab: Boolean) {
    fun emptyMessage(...) = ...
    companion object {
        val ReadingNow = BookListScreenSpec("readingNow.empty", hasFab = true)
        val Favorites  = BookListScreenSpec("favorites.empty",  hasFab = false)
    }
}
```

### Composition over field duplication
A type that repeats every field of another type → compose, don't restate:

```kotlin
// before
data class BookWithProgress(
    val id: String, val title: String, val author: String, /* 7 more */
    val lastChapter: Int?, val progress: Double?,
)

// after
data class BookWithProgress(
    val record: BookRecord,        // owns id/title/author/...
    val lastChapter: Int?,
    val progress: Double?,
)
```

Use-sites read `book.record.title`. The cost of typing `.record.` is repaid by
not maintaining 10 mirrored fields and a mapper that copies them.

---

## Category 5 — Locale strings in the wrong layer

Hardcoded user-visible strings in storage/networking/domain layers are an
anti-pattern. The fix is **stable codes + UI-side mapping**, not pulling i18n
into the data layer:

```kotlin
// data layer — codes only
const val STORAGE_ERR_OPEN_FAILED = "STORAGE_OPEN_FAILED"
throw StorageError(STORAGE_ERR_OPEN_FAILED, cause)

// UI layer — maps code → localized
fun storageErrorMessage(locale, code) = when (code) {
    STORAGE_ERR_OPEN_FAILED -> i18n("storage.errors.openFailed", locale)
    ...
    else -> code   // unknown code → show the code itself, never empty
}
```

Add the keys to all locale catalogs in the same change. If the project has
shared i18n infrastructure already, plug into it instead of inventing new.

---

## Category 6 — UX/Accessibility tells

LLM-generated UI code reliably ships these:

- `combinedClickable(onClick = f, onLongClick = f)` — long-press = tap. Use
  plain `clickable`.
- `contentDescription = placeholder_text` for an unrelated icon button (close,
  clear). Either give it a real label or `null` (decorative — only OK when an
  enclosing IconButton has the semantics).
- Icon mismatches: `Icons.Filled.Add` for a "copy" button, `Icons.Filled.Edit`
  for a "settings" button. Match icon to function.
- `onDismissRequest = { }` on a dialog — back/scrim won't dismiss. Either fix
  the UX or add a one-line comment explaining why dismissal is button-only.

---

## Category 7 — Parse / format defensiveness

External-input parsers must **degrade**, not crash:

```kotlin
// bad — crashes on non-hex chars
val v = hex.toLong(16)

// good — falls back to a safe default, never throws
val v = hex.toLongOrNull(16) ?: return DEFAULT_COLOR
```

Same for: JSON parsers (return `null` on bad input, not throw), regex extracts
(handle no-match), URI parsers (handle malformed). Each parser should document
its degradation contract in one line.

---

## Category 8 — Imports and namespace hygiene

- **Fully-qualified type names in code** (`androidx.compose.ui.graphics.vector.ImageVector`
  in a parameter) — replace with an import. Always.
- **Self-package imports** (`import com.foo.bar.X` inside `package com.foo.bar`)
  — delete, they compile but signal copy-paste.
- **Unused imports** after a rename or deletion — clean up.
- **Wildcard imports** (`import x.*`) when project style is explicit imports.

---

## Category 9 — Test naming

Tests with `_matchesReactNative` / `_matchesUpstream` / `_likeOriginalImpl` are
port-debt. Rename to describe **what is being verified**, not what it copies:

```
fun targetWidth_matchesReactNative()  →  fun targetWidth_is288dp()
fun storageKey_matchesOriginal()      →  fun storageKey_isStable()
fun parse_matchesUpstream()           →  fun parse_handlesEmptyString()
```

The body of the test usually tells you the right new name.

---

## Category 10 — Magic numbers

A bare `350L` in a coroutine `delay()` is opaque. A constant named
`SCROLL_DEBOUNCE_MS = 350L` is readable; with a one-line KDoc explaining
**why 350** (not "throttle interval", but "matches user finger-down + frame
latency on mid-tier devices"), it's documentation.

But **don't fight `@Suppress("MagicNumber")` battles** in math-heavy code
(animation curves, coordinate transforms) where a name would be longer than
the number. Pragmatism beats dogma.

---

## Anti-finding: things that look bad but aren't

Don't flag these unless context proves they're wrong:

- **`window.ReactNativeWebView` / `ReactNativeWebPolyfill`** — when it's a
  technical contract with an unmodified web page that genuinely speaks RN's
  bridge protocol.
- **`Log.w(TAG, e)` inside notify-listeners loops** — protects one bad
  subscriber from killing the rest.
- **`runCatching { File.delete() }`** on cleanup paths — temp files; nothing
  useful to do on failure.
- **A KDoc that says `(legacy)` or `(historical)`** when a constant is kept
  for storage compatibility — that's *useful* WHY.
- **`@Suppress("LongMethod")` on a state holder** with 15 mutable Compose
  fields — splitting it would obscure the state shape. A suppression annotation
  is a doc, not a sin.

---

## Output discipline

When delivering the review:

1. **Group findings by category**, not by file. Reader scans for "what kind of
   problems exist", not "every problem in file X".
2. Each finding: `path:line — one-sentence diagnosis → one-sentence fix`.
3. Separate the deferred-with-reason from the actually-applied. Don't pretend
   you fixed everything.
4. End with a build/test status line. "Compiles + tests green" is the only
   credible claim that you didn't break anything.
5. If you renamed/moved/deleted files, list them explicitly so docs maintainers
   can update cross-references in one pass.

---

## Things to never do under "make it look human"

- **Don't add comments to "explain" code that's already clear.** That's how you
  manufacture AI-comment smell trying to remove it.
- **Don't refactor unfamiliar code without tests.** The first passing test is
  the line you're allowed to refactor up to.
- **Don't bundle a stylistic cleanup with a behavior change.** The reviewer
  needs to see them separately.
- **Don't delete `@Suppress` annotations without the underlying fix.** They
  exist because the warning is annoying *and accurate*.
- **Don't rewrite working algorithms for "readability"** — most of the time
  what looks dense is dense for a reason (math, perf-critical loop). Profile
  before "improving".
