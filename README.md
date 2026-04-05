# Mercury

![Build](https://github.com/Merkost/Mercury/workflows/Build/badge.svg)

<img src="src/main/resources/META-INF/pluginIcon.svg" alt="Mercury" width="80"/>

**Room Database Schema Visualizer for IntelliJ IDEA & Android Studio**

---

<!-- Plugin description -->
Mercury gives developers immediate, live insight into their Room data model without leaving the IDE. It scans source code in real time, renders interactive schema trees and ER diagrams, and supports multi-database comparison — all in a clean, monochrome UI.

**Features:**

- **Schema Tree** — expandable tree view of your Room database structure with entities, columns, indices, foreign keys, DAOs, type converters, and database views
- **ER Diagram** — interactive entity-relationship diagram with entity cards, FK connection lines with arrows, pan/zoom/drag, auto-layout, and source code navigation
- **Queries Browser** — DAO method browser with expandable SQL blocks, keyword bolding, affected table links, and conflict strategy badges
- **Migration Diffs** — version-to-version schema comparison using Room's exported JSON, showing added/removed/modified entities with column-level detail
- **Real-Time Updates** — schema refreshes automatically as you edit Room classes (~500ms debounce)
- **K2 Compiler Support** — works with both K1 and K2 Kotlin compiler modes
- **Kotlin Multiplatform** — full support for KMP projects with .klib dependencies
- **Search** — fuzzy search across entities, columns, DAO methods, SQL queries, and return types

<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Mercury"</kbd> >
  <kbd>Install</kbd>

- Manually:

  Download the [latest release](https://github.com/Merkost/Mercury/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Compatibility

| IDE | Version |
|-----|---------|
| IntelliJ IDEA Community/Ultimate | 2024.2+ |
| Android Studio | 2024.2+ |

- Kotlin K1 and K2 compiler modes
- Kotlin Multiplatform (KMP) projects
- Room 2.5+ (Java and Kotlin annotations)

## Migration Diffs

To use the Diff tab for version comparison, enable Room schema export in your `build.gradle`:

```kotlin
room {
    schemaDirectory("$projectDir/schemas")
}
```

Mercury will auto-discover the exported JSON schema files and let you compare any two versions.

## Building from Source

```bash
./gradlew buildPlugin
```

The plugin ZIP will be in `build/distributions/`.

## License

Apache License 2.0 — see [LICENSE](LICENSE) for details.
