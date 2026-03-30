package com.github.merkost.mercury.model

data class DatabaseViewInfo(
    val name: String,
    val viewName: String,
    val qualifiedName: String,
    val query: String,
    val columns: List<ColumnInfo> = emptyList()
)
