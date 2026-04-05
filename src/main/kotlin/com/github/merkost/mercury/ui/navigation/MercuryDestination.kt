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
