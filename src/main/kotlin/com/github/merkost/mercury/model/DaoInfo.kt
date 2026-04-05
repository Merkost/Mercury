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
