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
