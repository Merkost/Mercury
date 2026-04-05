---
name: kotlin-clean-code
description: Enforces clean Kotlin code with zero comments — no //, no /* */, no KDoc. Code must be self-documenting through naming, structure, and idiomatic Kotlin. Use this skill whenever writing, editing, reviewing, or generating Kotlin code (.kt, .kts files). Also trigger when the user mentions Kotlin, asks to write Android/JVM code, refactors Kotlin files, or creates new Kotlin classes/functions. Even for small edits — every line of Kotlin this skill touches must follow these rules.
---

# Kotlin Clean Code — Zero Comments

This skill exists because comments become lies over time. Code changes, comments don't. The codebase stays honest when the code itself communicates intent through naming, structure, and Kotlin's expressive type system.

## The One Absolute Rule

**Never write comments in Kotlin code.** No `//`, no `/* */`, no `/** */` KDoc blocks. If code needs a comment to be understood, the code is the problem — fix the code, not the reader.

When you encounter existing comments while editing a file, remove them and make the code self-explanatory instead.

## How to Write Code That Doesn't Need Comments

### Names Are Documentation

Choose names that make the reader's mental model match the code's behavior:

- Functions describe what they do: `fetchActiveUsers()`, not `getUsers()` or `getData()`
- Booleans read as questions: `isExpired`, `hasPermission`, `canRetry`
- Variables name the thing they hold: `remainingAttempts`, not `count` or `n`
- Parameters clarify their role: `filterByStatus(status: UserStatus)`, not `filter(s: String)`
- Enums name their domain: `ConnectionState.Disconnected`, not `State.S3`

When you're tempted to write a comment explaining what code does, rename things until the comment becomes redundant.

### Small Functions Over Inline Comments

If a block of code needs explanation, extract it into a well-named function:

```kotlin
// Bad — comment needed to understand the block
fun processOrder(order: Order) {
    // Check if the order is eligible for express shipping
    if (order.weight < 5.0 && order.destination.isLocal && order.priority == Priority.HIGH) {
        applyExpressShipping(order)
    }
}

// Good — the function name IS the explanation
fun processOrder(order: Order) {
    if (order.isEligibleForExpressShipping()) {
        applyExpressShipping(order)
    }
}
```

### Types Encode Constraints

Use Kotlin's type system to make invalid states unrepresentable:

- `sealed class`/`sealed interface` for restricted hierarchies instead of comment-documented string conventions
- `value class` for domain primitives: `value class EmailAddress(val value: String)` over `String` with a comment saying "must be email"
- `enum class` for fixed sets of values instead of int constants with comment headers
- Non-null types by default — only use `?` when absence is a genuine domain concept

### Structure Communicates Flow

- `when` expressions over `if`/`else` chains — they show exhaustive handling at a glance
- Extension functions to read like natural language: `user.hasActiveSubscription()`
- Scope functions for clear object configuration: `apply` for setup, `let` for transforms, `also` for side effects
- `require()` and `check()` for preconditions — they're self-documenting guards

## Kotlin Idioms to Prefer

Write Kotlin like Kotlin, not like Java with a `.kt` extension:

- `data class` for value holders
- Destructuring: `val (name, age) = user`
- `val` over `var` — immutability by default
- Single-expression functions when the body is simple: `fun isAdult() = age >= 18`
- `listOf`, `mapOf`, `buildList`, `buildMap` over manual construction
- Collection operations (`filter`, `map`, `groupBy`, `associate`) over manual loops
- `?.let { }` and `?:` (elvis) for null handling over `if (x != null)` blocks
- Trailing lambdas and `it` for single-parameter lambdas
- `object` for singletons and stateless implementations
- Companion factory functions (`of`, `from`, `create`) over secondary constructors
- `runCatching` for expected failures at system boundaries

## What Not to Do

- No TODO comments — create an issue in the tracker instead
- No commented-out code — delete it, git remembers
- No section-separator comments (`// ---- Utils ----`) — if a file needs sections, it needs to be split into separate files
- No "why" comments — encode the reason in a well-named function or sealed type
- No license/copyright headers unless the build system requires them (and then only via a template, not hand-written)
- No suppress annotations with explanatory comments — if you need `@Suppress`, the code structure is the problem

## File Organization

- One top-level class per file (with related private helpers below it)
- File name matches the primary class/interface
- Group related extension functions in a file named after the type they extend: `StringExtensions.kt`
- Keep files short — if a file exceeds ~200 lines, it likely has multiple responsibilities to split
