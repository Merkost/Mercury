<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Mercury Changelog

## [1.0.0] - 2026-04-02

### Added
- Schema tree view with expandable entities, columns, indices, foreign keys, DAOs, type converters, and database views
- Interactive ER diagram with entity cards, FK relationship lines with arrowheads, cardinality indicators, pan/zoom/drag, auto-fit layout, and toolbar with zoom controls
- Queries tab with DAO browser, expandable SQL blocks with keyword bolding, "Touches" table links, conflict strategy badges, and search
- Migration diffs tab comparing Room exported JSON schema versions with side-by-side entity matching, column-level diffs, and migration summary
- Real-time schema updates via PSI tree change listener with 500ms debounce
- K2 Kotlin compiler support
- Kotlin Multiplatform (KMP) support with .klib dependency parsing
- Hybrid PSI parser: Java PSI for standard projects, Kotlin PSI fallback for KMP
- Foreign key extraction from both Java PSI and Kotlin PSI annotation paths
- Polished monochrome UI with smooth animations, hover states, skeleton loading, empty/error states
- Fuzzy search across entities, columns, DAO methods, SQL queries, and return types
- Source code navigation via double-click on any schema element
- Canvas grid dots, zoom-dependent detail hiding, and animated tab indicator
- Shared hover modifier utility for consistent interaction patterns
- Internationalized UI strings via MercuryBundle.properties

## [Unreleased]
