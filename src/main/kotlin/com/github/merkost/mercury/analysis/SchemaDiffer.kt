package com.github.merkost.mercury.analysis

import com.github.merkost.mercury.model.*

object SchemaDiffer {

    fun compare(left: DatabaseInfo, right: DatabaseInfo): SchemaDiff {
        val leftByName = left.entities.associateBy { it.name }
        val rightByName = right.entities.associateBy { it.name }
        val matchedNames = leftByName.keys.intersect(rightByName.keys)

        val matchedEntities = matchedNames.map { name ->
            val le = leftByName.getValue(name)
            val re = rightByName.getValue(name)
            val columnDiffs = compareColumns(le, re)
            EntityMatch(le, re, if (columnDiffs.isEmpty()) MatchType.IDENTICAL else MatchType.SIMILAR, columnDiffs)
        }.sortedBy { it.matchType.ordinal }

        return SchemaDiff(
            leftDatabase = left.name, rightDatabase = right.name,
            matchedEntities = matchedEntities,
            leftOnly = (leftByName.keys - rightByName.keys).map { leftByName.getValue(it) },
            rightOnly = (rightByName.keys - leftByName.keys).map { rightByName.getValue(it) }
        )
    }

    private fun compareColumns(left: EntityInfo, right: EntityInfo): List<ColumnDiff> {
        val leftByCn = left.columns.associateBy { it.columnName }
        val rightByCn = right.columns.associateBy { it.columnName }
        val diffs = mutableListOf<ColumnDiff>()
        for (cn in (leftByCn.keys + rightByCn.keys).toSortedSet()) {
            val lc = leftByCn[cn]; val rc = rightByCn[cn]
            when {
                lc != null && rc == null -> diffs.add(ColumnDiff(cn, lc.type, null, ColumnDiffType.LEFT_ONLY))
                lc == null && rc != null -> diffs.add(ColumnDiff(cn, null, rc.type, ColumnDiffType.RIGHT_ONLY))
                lc != null && rc != null && lc.type != rc.type -> diffs.add(ColumnDiff(cn, lc.type, rc.type, ColumnDiffType.TYPE_CHANGED))
                lc != null && rc != null && lc.isNullable != rc.isNullable -> {
                    val ld = if (lc.isNullable) "${lc.type}?" else lc.type
                    val rd = if (rc.isNullable) "${rc.type}?" else rc.type
                    diffs.add(ColumnDiff(cn, ld, rd, ColumnDiffType.NULLABILITY_CHANGED))
                }
            }
        }
        return diffs
    }
}
