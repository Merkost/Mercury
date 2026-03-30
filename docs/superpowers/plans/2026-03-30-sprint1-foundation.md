# Sprint 1: Foundation — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build system compiles with Compose/Jewel, domain models are defined and tested, design system tokens exist, navigation works, and the Mercury tool window loads in the IDE with themed tabs.

**Architecture:** Layered architecture with pure Kotlin domain models (`model/`), Compose UI with Jewel IDE bridge (`ui/`), token-based design system (`ui/theme/`), and Nav3-inspired declarative navigation (`ui/navigation/`). All UI consumes tokens via CompositionLocal — zero hardcoded values.

**Tech Stack:** Kotlin 2.1.20, IntelliJ Platform 2024.2.5, Compose for Desktop (via Jewel 0.27.0 IDE LaF Bridge), IntelliJ Platform Gradle Plugin 2.5.0

**Important Notes:**
- Jewel 0.27.0 is the final standalone release for platform 242 (repo archived, moved into intellij-community for 251+)
- Navigation 3's actual dependency may have Compose version conflicts with Jewel 0.27.0 (which uses Compose 1.7.1). We implement Nav3-inspired navigation using the same patterns (sealed destinations, state-driven `SnapshotStateList` back stack, declarative rendering) without the Nav3 artifact. This gives us the exact same API surface. When we upgrade to platform 251+, we can swap in the real Nav3 dependency with minimal changes.
- The `kotlin-clean-code` skill is active: zero comments in Kotlin code. Code must be self-documenting.

---

## File Structure

```
src/main/kotlin/com/github/merkost/mercury/
├── model/
│   ├── RoomSchema.kt            # Core domain models: DatabaseInfo, EntityInfo, ColumnInfo, etc.
│   ├── DaoInfo.kt               # DAO and method models
│   ├── TypeConverterInfo.kt     # Type converter model
│   ├── DatabaseViewInfo.kt      # Database view model
│   └── SchemaDiff.kt            # Diff result models for comparison
├── ui/
│   ├── theme/
│   │   ├── MercuryPrimitives.kt # Raw gray-scale color palette
│   │   ├── MercuryColors.kt     # Semantic color tokens + dark/light schemes
│   │   ├── MercuryTypography.kt # Type scale: body, title, label, code
│   │   ├── MercuryDimensions.kt # Spacing, sizing, elevation, motion tokens
│   │   └── MercuryTheme.kt      # CompositionLocal provider + theme object
│   ├── navigation/
│   │   ├── MercuryDestination.kt # Sealed destination hierarchy
│   │   └── MercuryNavigator.kt   # Navigation state + action dispatcher
│   ├── components/
│   │   ├── MercuryTabs.kt       # Underline-style tab bar
│   │   ├── MercurySearch.kt     # Search input field
│   │   ├── MercuryBadge.kt      # Count/label badges
│   │   ├── MercuryDropdown.kt   # Selector dropdown
│   │   └── MercuryDivider.kt    # Dotted section dividers
│   └── toolwindow/
│       ├── MercuryToolWindowFactory.kt  # IntelliJ tool window entry point
│       └── MercuryToolWindowPanel.kt    # Root Compose content with nav + tabs
├── services/
│   └── MercuryProjectService.kt # Project-scoped service (schema state holder)
└── MercuryBundle.kt             # i18n bundle (renamed from MyBundle)

src/main/resources/
├── META-INF/plugin.xml          # Modified: updated registrations
└── messages/MercuryBundle.properties  # Renamed bundle

src/test/kotlin/com/github/merkost/mercury/
├── model/
│   ├── RoomSchemaTest.kt        # Domain model tests
│   ├── DaoInfoTest.kt           # DAO model tests
│   └── SchemaDiffTest.kt        # Diff model tests
└── MercuryPluginTest.kt         # Plugin loading test (replaces MyPluginTest)
```

---

## Task 1: Configure Build System

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `settings.gradle.kts`
- Modify: `build.gradle.kts`
- Modify: `gradle.properties`

- [ ] **Step 1: Update version catalog with Compose and Jewel dependencies**

Replace the content of `gradle/libs.versions.toml`:

```toml
[versions]
junit = "4.13.2"
opentest4j = "1.3.0"

changelog = "2.2.1"
intelliJPlatform = "2.5.0"
kotlin = "2.1.20"
kover = "0.9.1"
qodana = "2024.3.4"

compose = "1.7.1"
jewel = "0.27.0"
kotlinxSerialization = "1.7.3"

[libraries]
junit = { group = "junit", name = "junit", version.ref = "junit" }
opentest4j = { group = "org.opentest4j", name = "opentest4j", version.ref = "opentest4j" }
jewel-ide-laf-bridge = { group = "org.jetbrains.jewel", name = "jewel-ide-laf-bridge-242", version.ref = "jewel" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerialization" }

[plugins]
changelog = { id = "org.jetbrains.changelog", version.ref = "changelog" }
intelliJPlatform = { id = "org.jetbrains.intellij.platform", version.ref = "intelliJPlatform" }
kotlin = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlinSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
composeCompiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
composeMultiplatform = { id = "org.jetbrains.compose", version.ref = "compose" }
kover = { id = "org.jetbrains.kotlinx.kover", version.ref = "kover" }
qodana = { id = "org.jetbrains.qodana", version.ref = "qodana" }
```

- [ ] **Step 2: Update settings.gradle.kts with plugin management**

Replace `settings.gradle.kts`:

```kotlin
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "Mercury"
```

- [ ] **Step 3: Update build.gradle.kts with Compose and Jewel**

Replace the plugins and dependencies sections of `build.gradle.kts`. Keep the existing `intellijPlatform`, `changelog`, `kover`, `tasks`, and `intellijPlatformTesting` blocks unchanged. Add the new plugins and dependencies:

Add to plugins block:
```kotlin
alias(libs.plugins.kotlinSerialization)
alias(libs.plugins.composeCompiler)
alias(libs.plugins.composeMultiplatform)
```

Add to repositories block:
```kotlin
google()
maven("https://packages.jetbrains.team/maven/p/kpm/public/")
maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
```

Add to dependencies block:
```kotlin
implementation(libs.jewel.ide.laf.bridge) {
    exclude(group = "org.jetbrains.kotlinx")
}
implementation(compose.desktop.currentOs) {
    exclude(group = "org.jetbrains.compose.material")
    exclude(group = "org.jetbrains.kotlinx")
}
```

- [ ] **Step 4: Update gradle.properties to add Kotlin and Java bundled plugins**

Add to `gradle.properties`:
```properties
platformBundledPlugins = com.intellij.java, org.jetbrains.kotlin
```

This is needed because Room annotations are in Java/Kotlin source files, and the PSI parser (Sprint 2) requires these bundled plugins. Adding them now avoids a build reconfiguration later.

- [ ] **Step 5: Verify build compiles**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL (may have warnings about unused dependencies, that's fine)

- [ ] **Step 6: Commit**

```bash
git add gradle/libs.versions.toml settings.gradle.kts build.gradle.kts gradle.properties
git commit -m "feat: configure Compose for Desktop and Jewel dependencies"
```

---

## Task 2: Define Domain Models

**Files:**
- Create: `src/main/kotlin/com/github/merkost/mercury/model/RoomSchema.kt`
- Create: `src/main/kotlin/com/github/merkost/mercury/model/DaoInfo.kt`
- Create: `src/main/kotlin/com/github/merkost/mercury/model/TypeConverterInfo.kt`
- Create: `src/main/kotlin/com/github/merkost/mercury/model/DatabaseViewInfo.kt`
- Create: `src/main/kotlin/com/github/merkost/mercury/model/SchemaDiff.kt`

- [ ] **Step 1: Create RoomSchema.kt with core data models**

Create `src/main/kotlin/com/github/merkost/mercury/model/RoomSchema.kt`:

```kotlin
package com.github.merkost.mercury.model

data class RoomSchema(
    val databases: List<DatabaseInfo>
)

data class DatabaseInfo(
    val name: String,
    val qualifiedName: String,
    val version: Int,
    val entities: List<EntityInfo>,
    val views: List<DatabaseViewInfo>,
    val typeConverters: List<TypeConverterInfo>,
    val daos: List<DaoInfo>,
    val sourceSet: String = "main"
)

data class EntityInfo(
    val name: String,
    val tableName: String,
    val qualifiedName: String,
    val columns: List<ColumnInfo>,
    val primaryKey: PrimaryKeyInfo,
    val foreignKeys: List<ForeignKeyInfo>,
    val indices: List<IndexInfo>,
    val sourceSet: String = "main"
)

data class ColumnInfo(
    val name: String,
    val columnName: String,
    val type: String,
    val isNullable: Boolean,
    val defaultValue: String? = null,
    val typeConverter: TypeConverterInfo? = null,
    val isEmbedded: Boolean = false,
    val embeddedPrefix: String? = null
)

data class PrimaryKeyInfo(
    val columnNames: List<String>,
    val autoGenerate: Boolean = false
)

data class ForeignKeyInfo(
    val parentEntity: String,
    val parentColumns: List<String>,
    val childColumns: List<String>,
    val onDelete: ForeignKeyAction = ForeignKeyAction.NO_ACTION,
    val onUpdate: ForeignKeyAction = ForeignKeyAction.NO_ACTION
)

enum class ForeignKeyAction {
    NO_ACTION,
    RESTRICT,
    SET_NULL,
    SET_DEFAULT,
    CASCADE
}

data class IndexInfo(
    val name: String,
    val columnNames: List<String>,
    val isUnique: Boolean = false,
    val orders: List<IndexOrder> = emptyList()
)

enum class IndexOrder {
    ASC,
    DESC
}
```

- [ ] **Step 2: Create DaoInfo.kt**

Create `src/main/kotlin/com/github/merkost/mercury/model/DaoInfo.kt`:

```kotlin
package com.github.merkost.mercury.model

data class DaoInfo(
    val name: String,
    val qualifiedName: String,
    val methods: List<DaoMethod>
)

data class DaoMethod(
    val name: String,
    val type: DaoMethodType,
    val query: String? = null,
    val returnType: String,
    val parameters: List<DaoParameter> = emptyList(),
    val onConflict: OnConflictStrategy? = null,
    val touchedEntities: List<String> = emptyList()
)

data class DaoParameter(
    val name: String,
    val type: String
)

enum class DaoMethodType {
    QUERY,
    INSERT,
    UPDATE,
    DELETE,
    UPSERT,
    RAW_QUERY
}

enum class OnConflictStrategy {
    NONE,
    REPLACE,
    ABORT,
    IGNORE,
    ROLLBACK
}
```

- [ ] **Step 3: Create TypeConverterInfo.kt**

Create `src/main/kotlin/com/github/merkost/mercury/model/TypeConverterInfo.kt`:

```kotlin
package com.github.merkost.mercury.model

data class TypeConverterInfo(
    val name: String,
    val qualifiedName: String,
    val fromType: String,
    val toType: String
)
```

- [ ] **Step 4: Create DatabaseViewInfo.kt**

Create `src/main/kotlin/com/github/merkost/mercury/model/DatabaseViewInfo.kt`:

```kotlin
package com.github.merkost.mercury.model

data class DatabaseViewInfo(
    val name: String,
    val viewName: String,
    val qualifiedName: String,
    val query: String,
    val columns: List<ColumnInfo> = emptyList()
)
```

- [ ] **Step 5: Create SchemaDiff.kt**

Create `src/main/kotlin/com/github/merkost/mercury/model/SchemaDiff.kt`:

```kotlin
package com.github.merkost.mercury.model

data class SchemaDiff(
    val leftDatabase: String,
    val rightDatabase: String,
    val matchedEntities: List<EntityMatch>,
    val leftOnly: List<EntityInfo>,
    val rightOnly: List<EntityInfo>
)

data class EntityMatch(
    val left: EntityInfo,
    val right: EntityInfo,
    val matchType: MatchType,
    val columnDiffs: List<ColumnDiff>
)

enum class MatchType {
    IDENTICAL,
    SIMILAR,
    NAME_ONLY
}

data class ColumnDiff(
    val columnName: String,
    val leftType: String?,
    val rightType: String?,
    val diffType: ColumnDiffType
)

enum class ColumnDiffType {
    TYPE_CHANGED,
    LEFT_ONLY,
    RIGHT_ONLY,
    NULLABILITY_CHANGED
}
```

- [ ] **Step 6: Commit**

```bash
git add src/main/kotlin/com/github/merkost/mercury/model/
git commit -m "feat: define Room schema domain models"
```

---

## Task 3: Write Domain Model Tests

**Files:**
- Create: `src/test/kotlin/com/github/merkost/mercury/model/RoomSchemaTest.kt`
- Create: `src/test/kotlin/com/github/merkost/mercury/model/DaoInfoTest.kt`
- Create: `src/test/kotlin/com/github/merkost/mercury/model/SchemaDiffTest.kt`

- [ ] **Step 1: Write RoomSchemaTest.kt**

Create `src/test/kotlin/com/github/merkost/mercury/model/RoomSchemaTest.kt`:

```kotlin
package com.github.merkost.mercury.model

import org.junit.Assert.*
import org.junit.Test

class RoomSchemaTest {

    @Test
    fun `entity with primary key and columns`() {
        val entity = EntityInfo(
            name = "UserEntity",
            tableName = "users",
            qualifiedName = "com.example.UserEntity",
            columns = listOf(
                ColumnInfo(name = "id", columnName = "id", type = "Long", isNullable = false),
                ColumnInfo(name = "name", columnName = "user_name", type = "String", isNullable = false),
                ColumnInfo(name = "email", columnName = "email", type = "String", isNullable = true)
            ),
            primaryKey = PrimaryKeyInfo(columnNames = listOf("id"), autoGenerate = true),
            foreignKeys = emptyList(),
            indices = listOf(
                IndexInfo(name = "idx_user_email", columnNames = listOf("email"), isUnique = true)
            )
        )

        assertEquals("UserEntity", entity.name)
        assertEquals("users", entity.tableName)
        assertEquals(3, entity.columns.size)
        assertTrue(entity.primaryKey.autoGenerate)
        assertEquals(1, entity.indices.size)
        assertTrue(entity.indices.first().isUnique)
    }

    @Test
    fun `entity with foreign key`() {
        val fk = ForeignKeyInfo(
            parentEntity = "ProfileEntity",
            parentColumns = listOf("id"),
            childColumns = listOf("profile_id"),
            onDelete = ForeignKeyAction.CASCADE,
            onUpdate = ForeignKeyAction.NO_ACTION
        )

        assertEquals("ProfileEntity", fk.parentEntity)
        assertEquals(ForeignKeyAction.CASCADE, fk.onDelete)
    }

    @Test
    fun `column with type converter`() {
        val converter = TypeConverterInfo(
            name = "DateConverter",
            qualifiedName = "com.example.DateConverter",
            fromType = "Long",
            toType = "Date"
        )
        val column = ColumnInfo(
            name = "createdAt",
            columnName = "created_at",
            type = "Date",
            isNullable = false,
            typeConverter = converter
        )

        assertNotNull(column.typeConverter)
        assertEquals("Long", column.typeConverter!!.fromType)
        assertEquals("Date", column.typeConverter!!.toType)
    }

    @Test
    fun `database info aggregates entities and daos`() {
        val entity = EntityInfo(
            name = "UserEntity",
            tableName = "users",
            qualifiedName = "com.example.UserEntity",
            columns = emptyList(),
            primaryKey = PrimaryKeyInfo(columnNames = listOf("id")),
            foreignKeys = emptyList(),
            indices = emptyList()
        )

        val dao = DaoInfo(
            name = "UserDao",
            qualifiedName = "com.example.UserDao",
            methods = emptyList()
        )

        val db = DatabaseInfo(
            name = "AppDatabase",
            qualifiedName = "com.example.AppDatabase",
            version = 3,
            entities = listOf(entity),
            views = emptyList(),
            typeConverters = emptyList(),
            daos = listOf(dao)
        )

        assertEquals(3, db.version)
        assertEquals(1, db.entities.size)
        assertEquals(1, db.daos.size)
    }

    @Test
    fun `room schema holds multiple databases`() {
        val db1 = DatabaseInfo(
            name = "AppDatabase",
            qualifiedName = "com.example.AppDatabase",
            version = 1,
            entities = emptyList(),
            views = emptyList(),
            typeConverters = emptyList(),
            daos = emptyList()
        )
        val db2 = DatabaseInfo(
            name = "CacheDatabase",
            qualifiedName = "com.example.CacheDatabase",
            version = 1,
            entities = emptyList(),
            views = emptyList(),
            typeConverters = emptyList(),
            daos = emptyList()
        )

        val schema = RoomSchema(databases = listOf(db1, db2))
        assertEquals(2, schema.databases.size)
    }

    @Test
    fun `embedded column has prefix`() {
        val column = ColumnInfo(
            name = "address",
            columnName = "addr_street",
            type = "String",
            isNullable = false,
            isEmbedded = true,
            embeddedPrefix = "addr_"
        )

        assertTrue(column.isEmbedded)
        assertEquals("addr_", column.embeddedPrefix)
    }
}
```

- [ ] **Step 2: Write DaoInfoTest.kt**

Create `src/test/kotlin/com/github/merkost/mercury/model/DaoInfoTest.kt`:

```kotlin
package com.github.merkost.mercury.model

import org.junit.Assert.*
import org.junit.Test

class DaoInfoTest {

    @Test
    fun `dao with query method`() {
        val method = DaoMethod(
            name = "getAll",
            type = DaoMethodType.QUERY,
            query = "SELECT * FROM users",
            returnType = "Flow<List<UserEntity>>",
            parameters = emptyList(),
            touchedEntities = listOf("users")
        )

        assertEquals(DaoMethodType.QUERY, method.type)
        assertEquals("SELECT * FROM users", method.query)
        assertEquals(1, method.touchedEntities.size)
    }

    @Test
    fun `dao with insert method and conflict strategy`() {
        val method = DaoMethod(
            name = "insert",
            type = DaoMethodType.INSERT,
            returnType = "Unit",
            parameters = listOf(DaoParameter(name = "user", type = "UserEntity")),
            onConflict = OnConflictStrategy.REPLACE
        )

        assertEquals(DaoMethodType.INSERT, method.type)
        assertEquals(OnConflictStrategy.REPLACE, method.onConflict)
        assertEquals(1, method.parameters.size)
    }

    @Test
    fun `dao with parameterized query`() {
        val method = DaoMethod(
            name = "getById",
            type = DaoMethodType.QUERY,
            query = "SELECT * FROM users WHERE id = :id",
            returnType = "UserEntity?",
            parameters = listOf(DaoParameter(name = "id", type = "Long"))
        )

        assertEquals(1, method.parameters.size)
        assertEquals("id", method.parameters.first().name)
    }

    @Test
    fun `dao info groups methods`() {
        val dao = DaoInfo(
            name = "UserDao",
            qualifiedName = "com.example.UserDao",
            methods = listOf(
                DaoMethod(name = "getAll", type = DaoMethodType.QUERY, query = "SELECT * FROM users", returnType = "List<UserEntity>"),
                DaoMethod(name = "insert", type = DaoMethodType.INSERT, returnType = "Unit"),
                DaoMethod(name = "delete", type = DaoMethodType.DELETE, returnType = "Unit")
            )
        )

        assertEquals(3, dao.methods.size)
        assertEquals(1, dao.methods.count { it.type == DaoMethodType.QUERY })
    }
}
```

- [ ] **Step 3: Write SchemaDiffTest.kt**

Create `src/test/kotlin/com/github/merkost/mercury/model/SchemaDiffTest.kt`:

```kotlin
package com.github.merkost.mercury.model

import org.junit.Assert.*
import org.junit.Test

class SchemaDiffTest {

    private fun entity(name: String, vararg columns: Pair<String, String>): EntityInfo {
        return EntityInfo(
            name = name,
            tableName = name.lowercase(),
            qualifiedName = "com.example.$name",
            columns = columns.map { (n, t) ->
                ColumnInfo(name = n, columnName = n, type = t, isNullable = false)
            },
            primaryKey = PrimaryKeyInfo(columnNames = listOf("id")),
            foreignKeys = emptyList(),
            indices = emptyList()
        )
    }

    @Test
    fun `diff with identical entities`() {
        val match = EntityMatch(
            left = entity("User", "id" to "Long", "name" to "String"),
            right = entity("User", "id" to "Long", "name" to "String"),
            matchType = MatchType.IDENTICAL,
            columnDiffs = emptyList()
        )

        assertEquals(MatchType.IDENTICAL, match.matchType)
        assertTrue(match.columnDiffs.isEmpty())
    }

    @Test
    fun `diff with type changed column`() {
        val diff = ColumnDiff(
            columnName = "timestamp",
            leftType = "Date",
            rightType = "Long",
            diffType = ColumnDiffType.TYPE_CHANGED
        )

        assertEquals(ColumnDiffType.TYPE_CHANGED, diff.diffType)
        assertEquals("Date", diff.leftType)
        assertEquals("Long", diff.rightType)
    }

    @Test
    fun `schema diff summary`() {
        val schemaDiff = SchemaDiff(
            leftDatabase = "AppDatabase",
            rightDatabase = "CacheDatabase",
            matchedEntities = listOf(
                EntityMatch(
                    left = entity("Settings"),
                    right = entity("Settings"),
                    matchType = MatchType.IDENTICAL,
                    columnDiffs = emptyList()
                )
            ),
            leftOnly = listOf(entity("User")),
            rightOnly = listOf(entity("CacheEntry"))
        )

        assertEquals(1, schemaDiff.matchedEntities.size)
        assertEquals(1, schemaDiff.leftOnly.size)
        assertEquals(1, schemaDiff.rightOnly.size)
    }

    @Test
    fun `column only in left side`() {
        val diff = ColumnDiff(
            columnName = "avatar",
            leftType = "String",
            rightType = null,
            diffType = ColumnDiffType.LEFT_ONLY
        )

        assertEquals(ColumnDiffType.LEFT_ONLY, diff.diffType)
        assertNull(diff.rightType)
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew test`
Expected: All tests pass. The existing `MyPluginTest` may still pass or can be temporarily ignored.

- [ ] **Step 5: Commit**

```bash
git add src/test/kotlin/com/github/merkost/mercury/model/
git commit -m "test: add domain model unit tests"
```

---

## Task 4: Implement Design System Tokens

**Files:**
- Create: `src/main/kotlin/com/github/merkost/mercury/ui/theme/MercuryPrimitives.kt`
- Create: `src/main/kotlin/com/github/merkost/mercury/ui/theme/MercuryColors.kt`
- Create: `src/main/kotlin/com/github/merkost/mercury/ui/theme/MercuryTypography.kt`
- Create: `src/main/kotlin/com/github/merkost/mercury/ui/theme/MercuryDimensions.kt`
- Create: `src/main/kotlin/com/github/merkost/mercury/ui/theme/MercuryTheme.kt`

- [ ] **Step 1: Create MercuryPrimitives.kt**

Create `src/main/kotlin/com/github/merkost/mercury/ui/theme/MercuryPrimitives.kt`:

```kotlin
package com.github.merkost.mercury.ui.theme

import androidx.compose.ui.graphics.Color

object MercuryPrimitives {
    val gray0 = Color(0xFF000000)
    val gray1 = Color(0xFF141414)
    val gray2 = Color(0xFF1A1A1A)
    val gray3 = Color(0xFF242424)
    val gray4 = Color(0xFF2E2E2E)
    val gray5 = Color(0xFF333333)
    val gray6 = Color(0xFF525252)
    val gray7 = Color(0xFF666666)
    val gray8 = Color(0xFF737373)
    val gray9 = Color(0xFF999999)
    val gray10 = Color(0xFFA3A3A3)
    val gray11 = Color(0xFFE5E5E5)
    val gray12 = Color(0xFFF0F0F0)
    val gray13 = Color(0xFFF5F5F5)
    val gray14 = Color(0xFFFAFAFA)
    val gray15 = Color(0xFFFFFFFF)

    val red = Color(0xFFFF4444)
    val redDim = Color(0xFF2D2020)
    val redLight = Color(0xFFDC2626)
    val redLightSurface = Color(0xFFFDF2F2)
    val green = Color(0xFF4ADE80)
    val greenDim = Color(0xFF1D2D20)
}
```

- [ ] **Step 2: Create MercuryColors.kt**

Create `src/main/kotlin/com/github/merkost/mercury/ui/theme/MercuryColors.kt`:

```kotlin
package com.github.merkost.mercury.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class MercuryColorScheme(
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val surfaceHover: Color,
    val surfacePressed: Color,
    val surfaceRecessed: Color,

    val border: Color,
    val borderSubtle: Color,
    val borderFocus: Color,

    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textInverse: Color,

    val accent: Color,
    val accentSubtle: Color,

    val error: Color,
    val errorSurface: Color,
    val diffAdd: Color,
    val diffRemove: Color,

    val diagramCanvas: Color,
    val diagramNode: Color,
    val diagramNodeHover: Color,
    val diagramEdge: Color,
    val diagramEdgeHighlight: Color,
    val diagramNodeDimmed: Color
)

val DarkColorScheme = MercuryColorScheme(
    background = MercuryPrimitives.gray2,
    surface = MercuryPrimitives.gray3,
    surfaceRaised = MercuryPrimitives.gray4,
    surfaceHover = MercuryPrimitives.gray4,
    surfacePressed = MercuryPrimitives.gray5,
    surfaceRecessed = MercuryPrimitives.gray1,

    border = MercuryPrimitives.gray5,
    borderSubtle = MercuryPrimitives.gray4,
    borderFocus = MercuryPrimitives.gray15,

    textPrimary = MercuryPrimitives.gray13,
    textSecondary = MercuryPrimitives.gray9,
    textMuted = MercuryPrimitives.gray7,
    textInverse = MercuryPrimitives.gray0,

    accent = MercuryPrimitives.gray15,
    accentSubtle = MercuryPrimitives.gray11,

    error = MercuryPrimitives.red,
    errorSurface = MercuryPrimitives.redDim,
    diffAdd = MercuryPrimitives.greenDim,
    diffRemove = MercuryPrimitives.redDim,

    diagramCanvas = MercuryPrimitives.gray1,
    diagramNode = MercuryPrimitives.gray3,
    diagramNodeHover = MercuryPrimitives.gray4,
    diagramEdge = MercuryPrimitives.gray6,
    diagramEdgeHighlight = MercuryPrimitives.gray15,
    diagramNodeDimmed = MercuryPrimitives.gray3.copy(alpha = 0.4f)
)

val LightColorScheme = MercuryColorScheme(
    background = MercuryPrimitives.gray14,
    surface = MercuryPrimitives.gray15,
    surfaceRaised = MercuryPrimitives.gray15,
    surfaceHover = MercuryPrimitives.gray13,
    surfacePressed = MercuryPrimitives.gray12,
    surfaceRecessed = MercuryPrimitives.gray13,

    border = MercuryPrimitives.gray11,
    borderSubtle = MercuryPrimitives.gray12,
    borderFocus = MercuryPrimitives.gray0,

    textPrimary = MercuryPrimitives.gray2,
    textSecondary = MercuryPrimitives.gray8,
    textMuted = MercuryPrimitives.gray10,
    textInverse = MercuryPrimitives.gray15,

    accent = MercuryPrimitives.gray0,
    accentSubtle = MercuryPrimitives.gray2,

    error = MercuryPrimitives.redLight,
    errorSurface = MercuryPrimitives.redLightSurface,
    diffAdd = MercuryPrimitives.gray12,
    diffRemove = MercuryPrimitives.redLightSurface,

    diagramCanvas = MercuryPrimitives.gray14,
    diagramNode = MercuryPrimitives.gray15,
    diagramNodeHover = MercuryPrimitives.gray13,
    diagramEdge = MercuryPrimitives.gray9,
    diagramEdgeHighlight = MercuryPrimitives.gray0,
    diagramNodeDimmed = MercuryPrimitives.gray15.copy(alpha = 0.4f)
)
```

- [ ] **Step 3: Create MercuryTypography.kt**

Create `src/main/kotlin/com/github/merkost/mercury/ui/theme/MercuryTypography.kt`:

```kotlin
package com.github.merkost.mercury.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class MercuryTypography(
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,

    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,

    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle,

    val codeLarge: TextStyle,
    val codeMedium: TextStyle,
    val codeSmall: TextStyle
)

fun mercuryTypography(
    sansFamily: FontFamily = FontFamily.Default,
    monoFamily: FontFamily = FontFamily.Monospace
): MercuryTypography = MercuryTypography(
    bodyLarge = TextStyle(fontFamily = sansFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
    bodyMedium = TextStyle(fontFamily = sansFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 18.sp),
    bodySmall = TextStyle(fontFamily = sansFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp),

    titleLarge = TextStyle(fontFamily = sansFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp),
    titleMedium = TextStyle(fontFamily = sansFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp),
    titleSmall = TextStyle(fontFamily = sansFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, lineHeight = 18.sp),

    labelLarge = TextStyle(fontFamily = sansFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp),
    labelMedium = TextStyle(fontFamily = sansFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, lineHeight = 14.sp),
    labelSmall = TextStyle(fontFamily = sansFamily, fontSize = 10.sp, fontWeight = FontWeight.Medium, lineHeight = 12.sp, letterSpacing = 0.5.sp),

    codeLarge = TextStyle(fontFamily = monoFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
    codeMedium = TextStyle(fontFamily = monoFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 18.sp),
    codeSmall = TextStyle(fontFamily = monoFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp)
)
```

- [ ] **Step 4: Create MercuryDimensions.kt**

Create `src/main/kotlin/com/github/merkost/mercury/ui/theme/MercuryDimensions.kt`:

```kotlin
package com.github.merkost.mercury.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.unit.dp

object MercurySpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
}

object MercurySize {
    val borderWidth = 1.dp
    val borderWidthFocus = 2.dp

    val radiusSm = 4.dp
    val radiusMd = 6.dp
    val radiusLg = 8.dp

    val iconSm = 14.dp
    val iconMd = 16.dp
    val iconLg = 20.dp

    val treeIndent = 16.dp
    val treeRowHeight = 28.dp

    val entityCardMinWidth = 180.dp
    val entityCardMaxWidth = 280.dp
    val entityCardPadding = 12.dp
    val entityFieldHeight = 24.dp
    val relationshipLineWidth = 1.dp
    val relationshipLineWidthHighlight = 2.dp

    val tabHeight = 36.dp
    val tabIndicatorHeight = 2.dp

    val searchHeight = 32.dp
}

object MercuryElevation {
    val none = 0.dp
    val subtle = 1.dp
    val raised = 3.dp
    val overlay = 8.dp
}

object MercuryMotion {
    val durationFast = 100
    val durationNormal = 200
    val durationSlow = 350

    val easingStandard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val easingDecelerate = CubicBezierEasing(0f, 0f, 0f, 1f)
    val easingAccelerate = CubicBezierEasing(0.3f, 0f, 1f, 1f)
}
```

- [ ] **Step 5: Create MercuryTheme.kt**

Create `src/main/kotlin/com/github/merkost/mercury/ui/theme/MercuryTheme.kt`:

```kotlin
package com.github.merkost.mercury.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalMercuryColors = staticCompositionLocalOf { DarkColorScheme }
val LocalMercuryTypography = staticCompositionLocalOf { mercuryTypography() }

object MercuryTheme {
    val colors: MercuryColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalMercuryColors.current

    val typography: MercuryTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalMercuryTypography.current
}

@Composable
fun MercuryTheme(
    isDark: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
    val typography = mercuryTypography()

    CompositionLocalProvider(
        LocalMercuryColors provides colorScheme,
        LocalMercuryTypography provides typography,
        content = content
    )
}
```

- [ ] **Step 6: Verify compilation**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add src/main/kotlin/com/github/merkost/mercury/ui/theme/
git commit -m "feat: implement Mercury design system tokens"
```

---

## Task 5: Set Up Navigation

**Files:**
- Create: `src/main/kotlin/com/github/merkost/mercury/ui/navigation/MercuryDestination.kt`
- Create: `src/main/kotlin/com/github/merkost/mercury/ui/navigation/MercuryNavigator.kt`

- [ ] **Step 1: Create MercuryDestination.kt**

Create `src/main/kotlin/com/github/merkost/mercury/ui/navigation/MercuryDestination.kt`:

```kotlin
package com.github.merkost.mercury.ui.navigation

sealed interface MercuryDestination {

    sealed interface Tab : MercuryDestination {
        data class Schema(val databaseId: String) : Tab
        data class Diagram(val databaseId: String) : Tab
        data class Queries(val databaseId: String) : Tab
        data class Comparison(val leftDbId: String, val rightDbId: String) : Tab
    }

    sealed interface Detail : MercuryDestination {
        data class Entity(val databaseId: String, val entityName: String) : Detail
        data class Dao(val databaseId: String, val daoName: String) : Detail
        data class TypeConverter(val databaseId: String, val converterName: String) : Detail
    }
}
```

- [ ] **Step 2: Create MercuryNavigator.kt**

Create `src/main/kotlin/com/github/merkost/mercury/ui/navigation/MercuryNavigator.kt`:

```kotlin
package com.github.merkost.mercury.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

class MercuryNavigator(
    initialDestination: MercuryDestination
) {
    private val _backStack: SnapshotStateList<MercuryDestination> = mutableStateListOf(initialDestination)
    val backStack: List<MercuryDestination> get() = _backStack

    var currentTab: MercuryDestination.Tab by mutableStateOf(
        initialDestination as? MercuryDestination.Tab
            ?: MercuryDestination.Tab.Schema("")
    )
        private set

    val current: MercuryDestination
        get() = _backStack.lastOrNull() ?: currentTab

    val canGoBack: Boolean
        get() = _backStack.size > 1

    fun switchTab(tab: MercuryDestination.Tab) {
        currentTab = tab
        _backStack.clear()
        _backStack.add(tab)
    }

    fun navigateToDetail(detail: MercuryDestination.Detail) {
        _backStack.add(detail)
    }

    fun goBack(): Boolean {
        if (_backStack.size <= 1) return false
        _backStack.removeLast()
        return true
    }

    fun openEntity(databaseId: String, entityName: String) {
        navigateToDetail(MercuryDestination.Detail.Entity(databaseId, entityName))
    }

    fun openDao(databaseId: String, daoName: String) {
        navigateToDetail(MercuryDestination.Detail.Dao(databaseId, daoName))
    }

    fun openComparison(leftDbId: String, rightDbId: String) {
        switchTab(MercuryDestination.Tab.Comparison(leftDbId, rightDbId))
    }
}

@Composable
fun rememberMercuryNavigator(
    initialDatabaseId: String = ""
): MercuryNavigator {
    return rememberSaveable(
        saver = Saver(
            save = { initialDatabaseId },
            restore = { dbId -> MercuryNavigator(MercuryDestination.Tab.Schema(dbId)) }
        )
    ) {
        MercuryNavigator(MercuryDestination.Tab.Schema(initialDatabaseId))
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/com/github/merkost/mercury/ui/navigation/
git commit -m "feat: implement Nav3-inspired navigation system"
```

---

## Task 6: Build Reusable Compose Components

**Files:**
- Create: `src/main/kotlin/com/github/merkost/mercury/ui/components/MercuryTabs.kt`
- Create: `src/main/kotlin/com/github/merkost/mercury/ui/components/MercurySearch.kt`
- Create: `src/main/kotlin/com/github/merkost/mercury/ui/components/MercuryBadge.kt`
- Create: `src/main/kotlin/com/github/merkost/mercury/ui/components/MercuryDropdown.kt`
- Create: `src/main/kotlin/com/github/merkost/mercury/ui/components/MercuryDivider.kt`

- [ ] **Step 1: Create MercuryTabs.kt**

Create `src/main/kotlin/com/github/merkost/mercury/ui/components/MercuryTabs.kt`:

```kotlin
package com.github.merkost.mercury.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.merkost.mercury.ui.theme.MercuryMotion
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

data class MercuryTab(
    val id: String,
    val label: String
)

@Composable
fun MercuryTabBar(
    tabs: List<MercuryTab>,
    selectedTabId: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(MercurySize.tabHeight)
            .padding(horizontal = MercurySpacing.lg),
        verticalAlignment = Alignment.Bottom
    ) {
        tabs.forEach { tab ->
            MercuryTabItem(
                tab = tab,
                isSelected = tab.id == selectedTabId,
                onClick = { onTabSelected(tab.id) }
            )
        }
    }
}

@Composable
private fun MercuryTabItem(
    tab: MercuryTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography

    val indicatorWidth by animateDpAsState(
        targetValue = if (isSelected) 1.dp else 0.dp,
        animationSpec = tween(MercuryMotion.durationNormal)
    )

    Column(
        modifier = Modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = MercurySpacing.md)
            .padding(bottom = MercurySpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = tab.label,
            style = if (isSelected) typography.titleSmall else typography.bodyMedium,
            color = if (isSelected) colors.textPrimary else colors.textMuted
        )
        Spacer(modifier = Modifier.height(MercurySpacing.xs))
        if (isSelected) {
            Box(
                modifier = Modifier
                    .height(MercurySize.tabIndicatorHeight)
                    .width(indicatorWidth.coerceAtLeast(1.dp))
                    .fillMaxWidth()
                    .background(colors.accent)
            )
        } else {
            Spacer(modifier = Modifier.height(MercurySize.tabIndicatorHeight))
        }
    }
}
```

- [ ] **Step 2: Create MercurySearch.kt**

Create `src/main/kotlin/com/github/merkost/mercury/ui/components/MercurySearch.kt`:

```kotlin
package com.github.merkost.mercury.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun MercurySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search entities, fields, queries...",
    modifier: Modifier = Modifier
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography
    var isFocused by remember { mutableStateOf(false) }

    val borderColor = if (isFocused) colors.borderFocus else colors.border
    val borderWidth = if (isFocused) MercurySize.borderWidthFocus else MercurySize.borderWidth

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        textStyle = typography.bodyMedium.copy(color = colors.textPrimary),
        cursorBrush = SolidColor(colors.accent),
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .height(MercurySize.searchHeight)
            .border(borderWidth, borderColor, RoundedCornerShape(MercurySize.radiusMd))
            .padding(horizontal = MercurySpacing.md)
            .onFocusChanged { isFocused = it.isFocused },
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxHeight()
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = typography.bodyMedium,
                        color = colors.textMuted
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxHeight()
            ) {
                innerTextField()
            }
        }
    )
}
```

- [ ] **Step 3: Create MercuryBadge.kt**

Create `src/main/kotlin/com/github/merkost/mercury/ui/components/MercuryBadge.kt`:

```kotlin
package com.github.merkost.mercury.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun MercuryBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography

    Text(
        text = text,
        style = typography.labelSmall,
        color = colors.textMuted,
        modifier = modifier
            .background(
                color = colors.surfaceRaised,
                shape = RoundedCornerShape(MercurySize.radiusSm)
            )
            .padding(horizontal = MercurySpacing.xs, vertical = MercurySpacing.xxs)
    )
}

@Composable
fun MercuryCountBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    MercuryBadge(
        text = count.toString(),
        modifier = modifier
    )
}
```

- [ ] **Step 4: Create MercuryDropdown.kt**

Create `src/main/kotlin/com/github/merkost/mercury/ui/components/MercuryDropdown.kt`:

```kotlin
package com.github.merkost.mercury.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Popup
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun MercuryDropdown(
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .border(MercurySize.borderWidth, colors.border, RoundedCornerShape(MercurySize.radiusMd))
                .padding(horizontal = MercurySpacing.md, vertical = MercurySpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedValue,
                style = typography.titleSmall,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.width(MercurySpacing.xs))
            Text(
                text = "\u25BE",
                style = typography.bodySmall,
                color = colors.textMuted
            )
        }

        if (expanded) {
            Popup(
                onDismissRequest = { expanded = false }
            ) {
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .background(colors.surfaceRaised, RoundedCornerShape(MercurySize.radiusMd))
                        .border(MercurySize.borderWidth, colors.border, RoundedCornerShape(MercurySize.radiusMd))
                ) {
                    options.forEach { option ->
                        Text(
                            text = option,
                            style = typography.bodyMedium,
                            color = if (option == selectedValue) colors.textPrimary else colors.textSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onOptionSelected(option)
                                    expanded = false
                                }
                                .padding(horizontal = MercurySpacing.md, vertical = MercurySpacing.sm)
                        )
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 5: Create MercuryDivider.kt**

Create `src/main/kotlin/com/github/merkost/mercury/ui/components/MercuryDivider.kt`:

```kotlin
package com.github.merkost.mercury.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun MercuryDottedDivider(
    label: String? = null,
    modifier: Modifier = Modifier
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MercurySpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DottedLine(modifier = Modifier.weight(1f))
        if (label != null) {
            Text(
                text = label,
                style = typography.labelSmall,
                color = colors.textMuted,
                modifier = Modifier.padding(horizontal = MercurySpacing.sm)
            )
            DottedLine(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun DottedLine(modifier: Modifier = Modifier) {
    val color = MercuryTheme.colors.borderSubtle
    Canvas(
        modifier = modifier.height(1.dp)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
        )
    }
}

@Composable
fun MercurySolidDivider(modifier: Modifier = Modifier) {
    val colors = MercuryTheme.colors
    Canvas(
        modifier = modifier.fillMaxWidth().height(1.dp)
    ) {
        drawLine(
            color = colors.borderSubtle,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f)
        )
    }
}
```

- [ ] **Step 6: Verify compilation**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add src/main/kotlin/com/github/merkost/mercury/ui/components/
git commit -m "feat: build reusable Mercury UI components"
```

---

## Task 7: Create Tool Window and Plugin Registration

**Files:**
- Create: `src/main/kotlin/com/github/merkost/mercury/ui/toolwindow/MercuryToolWindowFactory.kt`
- Create: `src/main/kotlin/com/github/merkost/mercury/ui/toolwindow/MercuryToolWindowPanel.kt`
- Create: `src/main/kotlin/com/github/merkost/mercury/services/MercuryProjectService.kt`
- Create: `src/main/kotlin/com/github/merkost/mercury/MercuryBundle.kt`
- Create: `src/main/resources/messages/MercuryBundle.properties`
- Modify: `src/main/resources/META-INF/plugin.xml`
- Delete: `src/main/kotlin/com/github/merkost/mercury/toolWindow/MyToolWindowFactory.kt`
- Delete: `src/main/kotlin/com/github/merkost/mercury/services/MyProjectService.kt`
- Delete: `src/main/kotlin/com/github/merkost/mercury/startup/MyProjectActivity.kt`
- Delete: `src/main/kotlin/com/github/merkost/mercury/MyBundle.kt`
- Delete: `src/main/resources/messages/MyBundle.properties`

- [ ] **Step 1: Create MercuryBundle.kt**

Create `src/main/kotlin/com/github/merkost/mercury/MercuryBundle.kt`:

```kotlin
package com.github.merkost.mercury

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.MercuryBundle"

object MercuryBundle : DynamicBundle(BUNDLE) {

    @JvmStatic
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
        getMessage(key, *params)
}
```

- [ ] **Step 2: Create MercuryBundle.properties**

Create `src/main/resources/messages/MercuryBundle.properties`:

```properties
toolwindow.title=Mercury
toolwindow.tab.schema=Schema
toolwindow.tab.diagram=Diagram
toolwindow.tab.queries=Queries
toolwindow.tab.diff=Diff
search.placeholder=Search entities, fields, queries...
database.empty=No Room databases found
database.select=Select database
```

- [ ] **Step 3: Create MercuryProjectService.kt**

Create `src/main/kotlin/com/github/merkost/mercury/services/MercuryProjectService.kt`:

```kotlin
package com.github.merkost.mercury.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.github.merkost.mercury.model.RoomSchema

@Service(Service.Level.PROJECT)
class MercuryProjectService(private val project: Project) {

    @Volatile
    var schema: RoomSchema = RoomSchema(databases = emptyList())
        private set

    fun updateSchema(newSchema: RoomSchema) {
        schema = newSchema
    }
}
```

- [ ] **Step 4: Create MercuryToolWindowPanel.kt**

Create `src/main/kotlin/com/github/merkost/mercury/ui/toolwindow/MercuryToolWindowPanel.kt`:

```kotlin
package com.github.merkost.mercury.ui.toolwindow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.github.merkost.mercury.MercuryBundle
import com.github.merkost.mercury.ui.components.MercuryDropdown
import com.github.merkost.mercury.ui.components.MercurySearchField
import com.github.merkost.mercury.ui.components.MercuryTab
import com.github.merkost.mercury.ui.components.MercuryTabBar
import com.github.merkost.mercury.ui.navigation.MercuryDestination
import com.github.merkost.mercury.ui.navigation.MercuryNavigator
import com.github.merkost.mercury.ui.navigation.rememberMercuryNavigator
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

private val TABS = listOf(
    MercuryTab("schema", "Schema"),
    MercuryTab("diagram", "Diagram"),
    MercuryTab("queries", "Queries"),
    MercuryTab("diff", "Diff")
)

@Composable
fun MercuryToolWindowPanel(
    databaseNames: List<String>,
    selectedDatabase: String,
    onDatabaseSelected: (String) -> Unit
) {
    val colors = MercuryTheme.colors
    val navigator = rememberMercuryNavigator(selectedDatabase)
    var searchQuery by remember { mutableStateOf("") }
    var selectedTabId by remember { mutableStateOf("schema") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        MercuryHeader(
            databaseNames = databaseNames,
            selectedDatabase = selectedDatabase,
            onDatabaseSelected = onDatabaseSelected
        )

        MercurySearchField(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier = Modifier.padding(horizontal = MercurySpacing.lg, vertical = MercurySpacing.sm)
        )

        MercuryTabBar(
            tabs = TABS,
            selectedTabId = selectedTabId,
            onTabSelected = { tabId ->
                selectedTabId = tabId
                val tab = when (tabId) {
                    "schema" -> MercuryDestination.Tab.Schema(selectedDatabase)
                    "diagram" -> MercuryDestination.Tab.Diagram(selectedDatabase)
                    "queries" -> MercuryDestination.Tab.Queries(selectedDatabase)
                    "diff" -> MercuryDestination.Tab.Comparison(selectedDatabase, "")
                    else -> MercuryDestination.Tab.Schema(selectedDatabase)
                }
                navigator.switchTab(tab)
            }
        )

        MercuryContentArea(
            selectedTabId = selectedTabId,
            navigator = navigator,
            searchQuery = searchQuery,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MercuryHeader(
    databaseNames: List<String>,
    selectedDatabase: String,
    onDatabaseSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MercurySpacing.lg)
    ) {
        if (databaseNames.isNotEmpty()) {
            MercuryDropdown(
                selectedValue = selectedDatabase,
                options = databaseNames,
                onOptionSelected = onDatabaseSelected
            )
        } else {
            Text(
                text = MercuryBundle.message("database.empty"),
                style = MercuryTheme.typography.bodyMedium,
                color = MercuryTheme.colors.textMuted
            )
        }
    }
}

@Composable
private fun MercuryContentArea(
    selectedTabId: String,
    navigator: MercuryNavigator,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(MercurySpacing.lg)
    ) {
        Text(
            text = "${selectedTabId.replaceFirstChar { it.uppercase() }} view — coming in Sprint 3",
            style = typography.bodyMedium,
            color = colors.textMuted
        )
    }
}
```

- [ ] **Step 5: Create MercuryToolWindowFactory.kt**

Create `src/main/kotlin/com/github/merkost/mercury/ui/toolwindow/MercuryToolWindowFactory.kt`:

```kotlin
package com.github.merkost.mercury.ui.toolwindow

import androidx.compose.runtime.*
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.github.merkost.mercury.services.MercuryProjectService
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.bridge.addComposeTab

class MercuryToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val service = project.service<MercuryProjectService>()

        toolWindow.addComposeTab("Mercury") {
            MercuryTheme(isDark = true) {
                val schema = service.schema
                val databaseNames = schema.databases.map { it.name }
                var selectedDatabase by remember {
                    mutableStateOf(databaseNames.firstOrNull() ?: "")
                }

                MercuryToolWindowPanel(
                    databaseNames = databaseNames,
                    selectedDatabase = selectedDatabase,
                    onDatabaseSelected = { selectedDatabase = it }
                )
            }
        }
    }

    override fun shouldBeAvailable(project: Project) = true
}
```

- [ ] **Step 6: Delete old template files**

```bash
rm src/main/kotlin/com/github/merkost/mercury/toolWindow/MyToolWindowFactory.kt
rm src/main/kotlin/com/github/merkost/mercury/services/MyProjectService.kt
rm src/main/kotlin/com/github/merkost/mercury/startup/MyProjectActivity.kt
rm src/main/kotlin/com/github/merkost/mercury/MyBundle.kt
rm src/main/resources/messages/MyBundle.properties
rmdir src/main/kotlin/com/github/merkost/mercury/toolWindow 2>/dev/null || true
rmdir src/main/kotlin/com/github/merkost/mercury/startup 2>/dev/null || true
```

- [ ] **Step 7: Update plugin.xml**

Replace `src/main/resources/META-INF/plugin.xml`:

```xml
<idea-plugin>
    <id>com.github.merkost.mercury</id>
    <name>Mercury</name>
    <vendor>merkost</vendor>

    <depends>com.intellij.modules.platform</depends>
    <depends>com.intellij.modules.java</depends>
    <depends>org.jetbrains.kotlin</depends>

    <resource-bundle>messages.MercuryBundle</resource-bundle>

    <extensions defaultExtensionNs="com.intellij">
        <toolWindow
            factoryClass="com.github.merkost.mercury.ui.toolwindow.MercuryToolWindowFactory"
            id="Mercury"
            anchor="right"
            icon="AllIcons.Nodes.DataTables"
        />
        <projectService
            serviceImplementation="com.github.merkost.mercury.services.MercuryProjectService"
        />
    </extensions>
</idea-plugin>
```

- [ ] **Step 8: Verify compilation**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: Commit**

```bash
git add -A
git commit -m "feat: create Mercury tool window with Compose UI shell"
```

---

## Task 8: Update Tests and Verify Plugin Loads

**Files:**
- Modify: `src/test/kotlin/com/github/merkost/mercury/MyPluginTest.kt` → rename to `MercuryPluginTest.kt`

- [ ] **Step 1: Replace test file**

Delete old test and create `src/test/kotlin/com/github/merkost/mercury/MercuryPluginTest.kt`:

```kotlin
package com.github.merkost.mercury

import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.github.merkost.mercury.services.MercuryProjectService
import com.github.merkost.mercury.model.DatabaseInfo
import com.github.merkost.mercury.model.RoomSchema

class MercuryPluginTest : BasePlatformTestCase() {

    fun testProjectServiceLoads() {
        val service = project.service<MercuryProjectService>()
        assertNotNull(service)
    }

    fun testInitialSchemaIsEmpty() {
        val service = project.service<MercuryProjectService>()
        assertTrue(service.schema.databases.isEmpty())
    }

    fun testSchemaUpdate() {
        val service = project.service<MercuryProjectService>()
        val newSchema = RoomSchema(
            databases = listOf(
                DatabaseInfo(
                    name = "TestDb",
                    qualifiedName = "com.test.TestDb",
                    version = 1,
                    entities = emptyList(),
                    views = emptyList(),
                    typeConverters = emptyList(),
                    daos = emptyList()
                )
            )
        )
        service.updateSchema(newSchema)
        assertEquals(1, service.schema.databases.size)
        assertEquals("TestDb", service.schema.databases.first().name)
    }
}
```

- [ ] **Step 2: Delete old test file**

```bash
rm src/test/kotlin/com/github/merkost/mercury/MyPluginTest.kt
```

- [ ] **Step 3: Run all tests**

Run: `./gradlew test`
Expected: All tests pass (model tests + plugin test)

- [ ] **Step 4: Verify plugin loads in IDE sandbox**

Run: `./gradlew runIde`
Expected: IntelliJ opens. Look for "Mercury" tool window on the right sidebar. Click it — should show the empty panel with "No Room databases found" text, search bar, and tabs (Schema, Diagram, Queries, Diff).

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: complete Sprint 1 foundation with tests and plugin verification"
```

---

## Sprint 1 Summary

| Task | What it produces |
|------|-----------------|
| 1. Build System | Compose + Jewel + Serialization configured |
| 2. Domain Models | `RoomSchema`, `EntityInfo`, `DaoInfo`, `SchemaDiff`, etc. |
| 3. Model Tests | Unit tests for all domain models |
| 4. Design System | Color/typography/spacing/animation tokens |
| 5. Navigation | Sealed destinations + navigator with back stack |
| 6. Components | Tabs, search, badge, dropdown, divider |
| 7. Tool Window | `MercuryToolWindowFactory` + panel + plugin.xml |
| 8. Verification | Plugin loads in IDE sandbox with themed UI |

After Sprint 1, the plugin loads in IntelliJ showing an empty but fully themed Mercury tool window with working tabs and navigation infrastructure. Sprint 2 will fill it with live data from the PSI parser.
