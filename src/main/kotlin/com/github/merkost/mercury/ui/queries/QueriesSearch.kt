package com.github.merkost.mercury.ui.queries

import com.github.merkost.mercury.model.DaoInfo
import com.github.merkost.mercury.model.DaoMethod

fun filterDaos(daos: List<DaoInfo>, query: String): List<DaoInfo> {
    if (query.isBlank()) return daos

    val lowerQuery = query.lowercase()

    return daos.mapNotNull { dao ->
        if (dao.name.lowercase().contains(lowerQuery) ||
            dao.qualifiedName.lowercase().contains(lowerQuery)
        ) {
            return@mapNotNull dao
        }

        val matchingMethods = dao.methods.filter { method -> methodMatchesQuery(method, lowerQuery) }
        if (matchingMethods.isNotEmpty()) {
            dao.copy(methods = matchingMethods)
        } else {
            null
        }
    }
}

private fun methodMatchesQuery(method: DaoMethod, lowerQuery: String): Boolean =
    method.name.lowercase().contains(lowerQuery) ||
        method.returnType.lowercase().contains(lowerQuery) ||
        method.query?.lowercase()?.contains(lowerQuery) == true ||
        method.parameters.any { it.name.lowercase().contains(lowerQuery) || it.type.lowercase().contains(lowerQuery) } ||
        method.type.name.lowercase().contains(lowerQuery)
