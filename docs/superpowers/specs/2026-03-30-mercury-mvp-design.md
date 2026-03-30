# Mercury MVP — Design Specification

> IntelliJ IDEA plugin for Android and Kotlin Multiplatform projects that visualizes Room database schemas directly within the IDE.

**Author:** Konstantin Merenkov (@Merkost)
**Date:** 2026-03-30
**Status:** Approved

---

## 1. Vision

Mercury gives developers immediate, live insight into their Room data model without leaving the IDE. It scans source code in real time, renders interactive schema trees and ER diagrams, and supports multi-database comparison — all in a clean, monochrome UI.

## 2. Architecture

### 2.1 Pattern: Layered Architecture (evolving toward Microkernel)

MVP uses clean layered separation. Each layer has a single responsibility and communicates through well-defined interfaces. Future versions will expose layers as IntelliJ extension points, enabling third-party extensibility (Microkernel/C pattern).

```
PSI Scanner ──┐
              ├→ Unified RoomSchema Model → UI Layer (Tree + Diagram)
JSON Parser ──┘           ↑
                     Change Listener (debounced)
```

### 2.2 Package Structure

```
com.github.merkost.mercury/
├── model/                    # Domain models (pure Kotlin, no IDE deps)
│   ├── RoomSchema.kt         # Database, Entity, Column, Index, ForeignKey
│   ├── DaoInfo.kt            # DAO methods, queries, return types
│   ├── TypeConverterInfo.kt  # Type converter mappings
│   ├── DatabaseViewInfo.kt   # @DatabaseView definitions
│   └── SchemaDiff.kt         # Diff result for comparison
│
├── parser/                   # Data layer — schema extraction
│   ├── PsiRoomParser.kt      # PSI-based live scanner
│   ├── JsonSchemaParser.kt   # Room schema JSON parser
│   └── SchemaResolver.kt     # Merges PSI + JSON, resolves conflicts
│
├── analysis/                 # Domain logic
│   ├── SchemaAnalyzer.kt     # Builds unified schema from parsers
│   ├── SchemaDiffer.kt       # Compares two schemas
│   ├── RelationshipResolver.kt # Resolves FK, @Relation, embedded refs
│   └── SearchEngine.kt       # Filter/search across entities, DAOs, fields
│
├── listener/                 # Event layer — real-time updates
│   ├── RoomPsiListener.kt    # PSI tree change listener (debounced)
│   └── SchemaChangeNotifier.kt # Pub/sub for schema changes → UI
│
├── ui/                       # UI layer (Compose + Jewel)
│   ├── theme/
│   │   ├── MercuryPrimitives.kt    # Raw color palette
│   │   ├── MercuryColorScheme.kt   # Semantic color tokens
│   │   ├── MercuryTypography.kt    # Type scale
│   │   ├── MercurySpacing.kt       # Spacing tokens
│   │   ├── MercurySize.kt          # Sizing tokens
│   │   ├── MercuryElevation.kt     # Shadow/depth tokens
│   │   ├── MercuryMotion.kt        # Animation tokens
│   │   └── MercuryTheme.kt         # CompositionLocal provider
│   ├── navigation/
│   │   ├── MercuryDestination.kt   # Sealed @Serializable destinations
│   │   ├── MercuryNavHost.kt       # NavDisplay integration
│   │   └── MercuryNavigator.kt     # Navigation action dispatcher
│   ├── components/                  # Reusable design system components
│   │   ├── MercuryCard.kt
│   │   ├── MercuryTree.kt
│   │   ├── MercuryTabs.kt
│   │   ├── MercurySearch.kt
│   │   ├── MercuryBadge.kt
│   │   ├── MercuryDropdown.kt
│   │   └── MercuryDivider.kt
│   ├── toolwindow/
│   │   ├── MercuryToolWindowFactory.kt
│   │   └── MercuryToolWindowPanel.kt
│   ├── tree/
│   │   └── SchemaTreeScene.kt
│   ├── diagram/
│   │   ├── DiagramScene.kt
│   │   ├── EntityCard.kt
│   │   ├── RelationshipLine.kt
│   │   └── DiagramLayoutEngine.kt
│   ├── queries/
│   │   └── QueriesScene.kt
│   └── comparison/
│       └── ComparisonScene.kt
│
├── kmp/                      # KMP support
│   └── SourceSetScanner.kt   # Discovers source sets, scans across them
│
└── services/                 # IntelliJ service registration
    └── MercuryProjectService.kt
```

### 2.3 Key Principle

The `model/` package has zero IDE dependencies — pure Kotlin data classes. Testable without IntelliJ test environment. Foundation for future extensibility.

## 3. Data Sources

### 3.1 PSI-Based Live Scanner (Primary)

Parses `@Entity`, `@Dao`, `@Database`, `@TypeConverter`, `@DatabaseView`, `@Embedded`, `@Relation` annotations directly from the live PSI tree. Updates in real time as the user types.

**Scanned annotations:**
- `@Database` → database name, version, entities list, views list
- `@Entity` → table name, columns from properties, indices, foreign keys
- `@ColumnInfo` → column name override, type affinity
- `@PrimaryKey` → PK flag, autoGenerate
- `@ForeignKey` → parent entity, parent columns, child columns, onDelete/onUpdate
- `@Index` → columns, unique flag
- `@Embedded` → prefix, nested fields
- `@Relation` → parentColumn, entityColumn, associateBy
- `@Dao` → methods annotated with @Query, @Insert, @Update, @Delete, @Upsert
- `@TypeConverter` → from/to types
- `@DatabaseView` → view name, SQL query

### 3.2 Room Schema JSON Parser (Secondary)

Reads Room's exported schema JSON files from the `schemas/` directory (generated when `room.schemaLocation` is configured in build.gradle). Used to:
- Validate PSI-parsed schema accuracy
- Fill in details PSI can't resolve (complex type converter resolution, auto-migration info)
- Provide schema when PSI analysis is incomplete (build errors, partial code)

### 3.3 Schema Resolver

Merges PSI and JSON sources into a unified `RoomSchema` model:
- PSI is authoritative for structure (live, always current)
- JSON fills gaps and validates (post-build ground truth)
- Conflicts logged, PSI wins for display (user sees what they're typing)

## 4. Real-Time Update Pipeline

```
User edits @Entity class
        ↓
RoomPsiListener (PsiTreeChangeListener)
        ↓  debounced ~300ms
SchemaAnalyzer.rebuildSchema()
        ↓
PsiRoomParser + JsonSchemaParser
        ↓
SchemaResolver.merge()
        ↓  incremental diff against previous schema
RoomSchema (unified model)
        ↓
SchemaChangeNotifier.notify()
        ↓
Tree + Diagram UI (preserves expansion/zoom/selection state)
```

**Key behaviors:**
- 300ms debounce prevents rapid re-scanning during fast typing
- Incremental diffing: only changed entities trigger UI updates
- UI state preservation: tree expansion, diagram zoom/pan, selected nodes survive re-scans
- Background thread for parsing, UI thread for rendering

## 5. Navigation (Navigation 3)

### 5.1 Dependency

```kotlin
implementation("org.jetbrains.androidx.navigation3:navigation3-ui:1.0.0-alpha06")
```

### 5.2 Destinations

Sealed `@Serializable` hierarchy:
- `SchemaTree(databaseId)` — tree view tab
- `Diagram(databaseId)` — ER diagram tab
- `Queries(databaseId)` — DAO/query browser tab
- `Comparison(leftDbId, rightDbId)` — diff view tab
- `EntityDetail(databaseId, entityName)` — entity detail (pushed onto stack)
- `DaoDetail(databaseId, daoName)` — DAO detail (pushed onto stack)
- `TypeConverterDetail(databaseId, converterName)` — converter detail (pushed onto stack)

### 5.3 Pattern

Nav3's `NavDisplay` + `entryProvider` renders the active destination. `MercuryNavigator` wraps the `SnapshotStateList` back stack and provides typed navigation actions. Back stack is serializable for state persistence.

## 6. UI Design

### 6.1 Design Language

**Monochrome.** Black/white with shades of gray. No color except semantic accents (error red, diff indicators). Typography-driven hierarchy — weight and size create structure, not color. Generous whitespace, thin 1px borders, subtle shadows.

### 6.2 Design System

Token-based architecture with four layers:
1. **Primitives** — 16-step gray scale + semantic red/green
2. **Semantic tokens** — `MercuryColorScheme` with named roles (surface, text.primary, border, etc.)
3. **Typography tokens** — `MercuryTypography` with body/title/label/code scales, sans + mono families
4. **Spacing/Size/Elevation/Motion tokens** — `MercurySpacing`, `MercurySize`, `MercuryElevation`, `MercuryMotion`

All tokens provided via `CompositionLocal`. Components consume `MercuryTheme.colors`, `MercuryTheme.typography`. Zero hardcoded values in UI code.

**Theme sync:** Detects IDE light/dark theme and applies matching `MercuryColorScheme` automatically.

### 6.3 Component Library

Reusable Compose components built on tokens:
- `MercuryCard` — entity cards, SQL blocks, panels
- `MercuryTree` — expandable/collapsible tree with indent guides
- `MercuryTabs` — underline-style tab bar (2px active indicator)
- `MercurySearch` — search input with filter icon
- `MercuryBadge` — PK, FK, unique, count indicators
- `MercuryDropdown` — database selector, layout mode picker
- `MercuryDivider` — dotted section dividers with labels

### 6.4 Tool Window Layout

Top bar: database dropdown + compare button
Search bar: fuzzy filter across all levels
Tab bar: Schema | Diagram | Queries | Diff
Content area: active tab scene

### 6.5 Schema Tree Tab

Typography-driven, no icons. Bold entity names, regular fields, monospace types. Field layout: name left-aligned, type tabbed, badges right-aligned in muted text. Counts as gray badges. Dotted section dividers. Hover highlights row, selected shows left accent bar.

### 6.6 ER Diagram Tab

Entity cards with surface background, 1px border, 4px radius, subtle shadow. Relationship lines: 1px solid, right-angle routing. Symbols: `●` PK, `◦` FK, `~` converter. Hover lifts card, highlights connected edges. Selected entity dims unrelated nodes to 40% opacity. Auto-layout (force-directed) + manual drag. Smooth animated transitions on schema changes.

### 6.7 Queries Tab

DAO methods grouped by DAO class. SQL in recessed monospace blocks with keyword bolding (no syntax colors). Method signatures with muted return types. "Touches" line shows affected entities as clickable links. Conflict strategy badges right-aligned.

### 6.8 Comparison Tab

Side-by-side two-column layout. `=` identical (muted), `≈` similar (normal), `·` no match (dimmed). Differing fields shown with subtle background tint. Summary bar with dot-separated stats.

## 7. Interactivity

- **Click** any entity/DAO/field → navigates to source code in editor (`PsiElement.navigate()`)
- **Hover** → tooltip with full type info, annotations, constraints
- **Right-click** → context menu: "Show in Diagram", "Copy DDL", "Find Usages"
- **Search** → fuzzy matching across entities, fields, DAOs, queries
- **Diagram gestures** → drag to pan, scroll to zoom, click to select, double-click to navigate
- **Filter** → collapse/expand relationships, show/hide specific entity types
- **Layout modes** → auto (force-directed), hierarchical, manual

## 8. Multi-Database Support

- Auto-discovers all `@Database` classes in the project
- Database selector dropdown in the tool window header
- Switchable via dropdown or tabs
- **Comparison mode:** side-by-side diff of two databases with structural matching (same-name entities, similar entities by column overlap, unique-to-each indicators)

## 9. KMP Support

- `SourceSetScanner` discovers KMP source sets (`commonMain`, `androidMain`, `iosMain`, etc.)
- Scans across all source sets for Room annotations
- Unified schema view regardless of where entities are declared
- Entities colored/badged by source set in the diagram for visual distinction

## 10. Testing Strategy

- **Model layer:** Unit tests (pure Kotlin, no IDE) — schema parsing, diffing, relationship resolution
- **Parser layer:** `BasePlatformTestCase` with test fixture files containing Room annotations
- **UI layer:** Compose UI tests for component rendering and interaction
- **Integration:** End-to-end tests with sample Room projects, verify schema extraction → visualization pipeline

## 11. Future: Evolution Toward Microkernel (Approach C)

Once the layered MVP is stable:
1. Define `SchemaProvider` extension point — third parties can add non-Room data sources
2. Define `Visualizer` extension point — third parties can add visualization modes
3. Define `SchemaExporter` extension point — export to SQL, Mermaid, PlantUML, etc.
4. Register via IntelliJ's `<extensionPoint>` in `plugin.xml`

---

## 12. Sprint Plan

### Sprint 1: Foundation (Infrastructure + Domain Model)

**Goal:** Build system compiles, design tokens exist, domain model is defined, plugin shell loads in IDE.

**Tasks:**
- [ ] Configure `build.gradle.kts`: add Compose for Desktop (Jewel), Navigation 3, Kotlin Serialization dependencies
- [ ] Define `model/` package: `RoomSchema`, `EntityInfo`, `ColumnInfo`, `IndexInfo`, `ForeignKeyInfo`, `DaoInfo`, `DaoMethod`, `TypeConverterInfo`, `DatabaseViewInfo`, `SchemaDiff`
- [ ] Implement full design system: `MercuryPrimitives`, `MercuryColorScheme` (dark + light), `MercuryTypography`, `MercurySpacing`, `MercurySize`, `MercuryElevation`, `MercuryMotion`, `MercuryTheme` CompositionLocal provider
- [ ] Build reusable components: `MercuryCard`, `MercuryTabs`, `MercurySearch`, `MercuryBadge`, `MercuryDropdown`, `MercuryDivider`, `MercuryTree`
- [ ] Set up navigation: `MercuryDestination` sealed hierarchy, `MercuryNavHost` with `NavDisplay`, `MercuryNavigator`
- [ ] Replace template `MyToolWindowFactory` with `MercuryToolWindowFactory` (empty shell with theme + nav)
- [ ] Register plugin in `plugin.xml` with updated IDs/names
- [ ] Unit tests for domain model creation and equality
- [ ] Verify plugin loads in IDE sandbox (`./gradlew runIde`)

**Deliverable:** Plugin loads in IDE, shows empty Mercury tool window with themed tabs and navigation working.

---

### Sprint 2: PSI Parser (Live Schema Extraction)

**Goal:** Mercury reads Room annotations from source code and produces a `RoomSchema` model.

**Tasks:**
- [ ] Implement `PsiRoomParser`: scan project for `@Database` annotated classes
- [ ] Parse `@Entity` annotations: table name, columns from constructor params / properties
- [ ] Parse column annotations: `@PrimaryKey`, `@ColumnInfo`, `@Embedded`, `@Ignore`
- [ ] Parse `@ForeignKey` definitions from `@Entity` annotation parameters
- [ ] Parse `@Index` definitions from `@Entity` annotation parameters
- [ ] Parse `@Dao` interfaces: extract methods with `@Query`, `@Insert`, `@Update`, `@Delete`, `@Upsert`
- [ ] Parse `@TypeConverter` classes: extract from/to type mappings
- [ ] Parse `@DatabaseView`: name and SQL query
- [ ] Parse `@Relation` and `@Embedded` for relationship resolution
- [ ] Implement `RelationshipResolver`: build relationship graph from FKs, @Relation, @Embedded
- [ ] Implement `SourceSetScanner` for KMP: discover `commonMain`, `androidMain`, etc. and scan all
- [ ] Write `BasePlatformTestCase` tests with fixture files for each annotation type
- [ ] Integration test: sample Room project → correct `RoomSchema` output

**Deliverable:** Given a project with Room entities, Mercury produces a complete, accurate `RoomSchema` model.

---

### Sprint 3: Schema Tree View (First Visualization)

**Goal:** Schema tree displays live in the tool window with full interactivity.

**Tasks:**
- [ ] Implement `SchemaTreeScene`: render `RoomSchema` as expandable tree
- [ ] Tree structure: Database → Entities → Columns/Indices/ForeignKeys, Views, TypeConverters, DAOs
- [ ] Field rows: name (left), type (tabbed monospace), badges (right-aligned muted: PK, FK, unique, auto, converter)
- [ ] Section dividers: dotted lines with labels for indices, foreign keys
- [ ] Counts as right-aligned gray badges on collapsible sections
- [ ] Implement `MercurySearch` fuzzy filtering: match across entity names, field names, types, DAO methods
- [ ] Click-to-navigate: clicking any item calls `PsiElement.navigate(true)` to jump to source
- [ ] Hover tooltips: full annotation details, constraints, type converter info
- [ ] Right-click context menu: "Show in Diagram", "Copy DDL", "Find Usages"
- [ ] Database selector dropdown: lists all `@Database` classes, switches active schema
- [ ] Selected state: left accent bar (2px white/black)
- [ ] Hover state: row background transition (150ms)
- [ ] Wire `MercuryProjectService` to hold current `RoomSchema` state
- [ ] UI tests for tree rendering and interaction

**Deliverable:** Users see their Room schema in the tree view, can search, click to navigate to source, and switch databases.

---

### Sprint 4: Real-Time Updates (Live Sync)

**Goal:** Schema tree updates automatically as the user edits Room classes.

**Tasks:**
- [ ] Implement `RoomPsiListener` extending `PsiTreeChangeListener`
- [ ] Filter: only trigger on changes to files containing Room annotations
- [ ] Debounce: 300ms delay before triggering re-scan (batch rapid keystrokes)
- [ ] Implement `SchemaChangeNotifier`: pub/sub pattern, UI subscribes to schema updates
- [ ] Incremental diffing: compare new schema against previous, identify changed entities only
- [ ] UI state preservation: save and restore tree expansion state across re-scans
- [ ] Background threading: parse on background thread, update UI on EDT
- [ ] Implement `SchemaAnalyzer` orchestration: coordinates parser → resolver → notifier pipeline
- [ ] Edge cases: handle syntax errors gracefully (partial parse, show last-known-good for broken entities)
- [ ] Performance test: measure re-scan time on a project with 50+ entities, ensure < 500ms

**Deliverable:** Edit an `@Entity` class → tree updates within ~300ms, no flicker, state preserved.

---

### Sprint 5: ER Diagram (Visual Schema)

**Goal:** Interactive entity-relationship diagram rendered on a Compose Canvas.

**Tasks:**
- [ ] Implement `MercuryDiagramCanvas`: custom Compose `Canvas` with pan (drag), zoom (scroll/pinch), and gesture handling
- [ ] Implement `EntityCard` composable: entity name header, field list with PK/FK/converter symbols, border + shadow
- [ ] Implement `RelationshipLine`: right-angle edge routing between FK columns, with cardinality labels (1:1, 1:N, N:M)
- [ ] Implement `DiagramLayoutEngine`: force-directed auto-layout algorithm (entities repel, connected entities attract)
- [ ] Layout mode: hierarchical option (root entities top, children below)
- [ ] Manual drag: user can reposition entity cards, positions persist
- [ ] Click entity → highlights entity + all connected relationships, dims unrelated to 40% opacity
- [ ] Hover entity → card shadow lifts, connected edges bold
- [ ] Double-click entity → navigates to source code
- [ ] Hover relationship line → popup with FK details (columns, onDelete action)
- [ ] Legend bar: `● PK  ◦ FK  ~ converted` + entity count
- [ ] Toolbar: zoom percentage, "Fit All" button, layout mode dropdown, "Export PNG" button
- [ ] Search/filter on diagram: highlight matching entities, dim others
- [ ] Smooth animated transitions when schema changes (entities slide, new ones fade in, removed fade out)
- [ ] KMP visual distinction: subtle badge or border style per source set
- [ ] UI tests for diagram rendering and gestures

**Deliverable:** Full interactive ER diagram with auto-layout, gestures, navigation, and animated live updates.

---

### Sprint 6: Queries Tab + JSON Parser

**Goal:** DAO query browser and schema JSON fallback parser.

**Tasks:**
- [ ] Implement `QueriesScene`: DAOs grouped as expandable sections
- [ ] Method display: return type, parameters, conflict strategy badge
- [ ] SQL blocks: monospace, recessed surface, keyword bolding (SELECT, FROM, WHERE, etc.)
- [ ] "Touches" line: analyze SQL to determine affected entities, show as clickable links
- [ ] Click method → navigate to DAO source
- [ ] Invalid query indicator: yellow/red accent if SQL references non-existent tables/columns
- [ ] Implement `JsonSchemaParser`: parse Room's exported JSON schema files from `schemas/` directory
- [ ] Implement `SchemaResolver.merge()`: combine PSI + JSON sources, PSI primary, JSON fills gaps
- [ ] Handle missing JSON gracefully (not all projects export schema)
- [ ] Search across queries: match by DAO name, method name, SQL content
- [ ] Unit tests for JSON parsing with sample schema files
- [ ] Integration test: PSI + JSON merge produces accurate unified schema

**Deliverable:** Queries tab shows all DAOs with highlighted SQL. JSON parser provides validation/fallback layer.

---

### Sprint 7: Comparison + Polish

**Goal:** Multi-database comparison view and final polish for marketplace readiness.

**Tasks:**
- [ ] Implement `ComparisonScene`: side-by-side two-column layout
- [ ] Structural matching: same-name entities (=), similar by column overlap (≈), unique to each (·)
- [ ] Differing fields highlighted with subtle background tint
- [ ] Summary bar: shared / left-only / right-only / differs counts
- [ ] Database swap button (⇄)
- [ ] Implement `SchemaDiffer`: produces `SchemaDiff` model with matched/unmatched/differing entities
- [ ] Export functionality: "Export PNG" from diagram, "Copy DDL" from context menu
- [ ] IDE theme sync: detect light/dark/new UI theme changes, update `MercuryColorScheme` automatically
- [ ] Error states: empty state (no Room found), loading state, partial parse state
- [ ] Plugin icon and branding for tool window
- [ ] Update `plugin.xml` with full description, change notes, vendor info
- [ ] Update `README.md` with feature overview, screenshots, installation instructions
- [ ] Update `CHANGELOG.md`
- [ ] End-to-end test: full pipeline with multi-database KMP project
- [ ] Performance audit: profile with large schema (100+ entities), optimize bottlenecks
- [ ] Marketplace preparation: signing, publish token, compatibility verification

**Deliverable:** Complete MVP ready for JetBrains Marketplace publication.

---

## Sprint Summary

| Sprint | Focus | Key Deliverable |
|--------|-------|-----------------|
| 1 | Foundation | Plugin shell + design system + navigation |
| 2 | PSI Parser | Live schema extraction from source code |
| 3 | Schema Tree | First visualization with full interactivity |
| 4 | Real-Time | Auto-updating on code changes |
| 5 | ER Diagram | Interactive visual diagram |
| 6 | Queries + JSON | DAO browser + JSON fallback parser |
| 7 | Comparison + Polish | Multi-DB diff + marketplace readiness |
