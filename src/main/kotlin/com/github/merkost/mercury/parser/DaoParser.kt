package com.github.merkost.mercury.parser

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.github.merkost.mercury.model.*

class DaoParser {

    fun parse(psiClass: PsiClass): DaoInfo? {
        if (!AnnotationUtil.isAnnotated(psiClass, RoomAnnotations.DAO, 0)) return null

        val methods = psiClass.methods
            .mapNotNull { parseMethod(it) }

        return DaoInfo(
            name = psiClass.name ?: return null,
            qualifiedName = psiClass.qualifiedName ?: return null,
            methods = methods
        )
    }

    private fun parseMethod(method: PsiMethod): DaoMethod? {
        val queryAnnotation = AnnotationUtil.findAnnotation(method, RoomAnnotations.QUERY)
        if (queryAnnotation != null) return parseQueryMethod(method, queryAnnotation)

        val insertAnnotation = AnnotationUtil.findAnnotation(method, RoomAnnotations.INSERT)
        if (insertAnnotation != null) return parseModifyMethod(method, DaoMethodType.INSERT, insertAnnotation)

        val updateAnnotation = AnnotationUtil.findAnnotation(method, RoomAnnotations.UPDATE)
        if (updateAnnotation != null) return parseModifyMethod(method, DaoMethodType.UPDATE, updateAnnotation)

        val deleteAnnotation = AnnotationUtil.findAnnotation(method, RoomAnnotations.DELETE)
        if (deleteAnnotation != null) return parseModifyMethod(method, DaoMethodType.DELETE, null)

        val upsertAnnotation = AnnotationUtil.findAnnotation(method, RoomAnnotations.UPSERT)
        if (upsertAnnotation != null) return parseModifyMethod(method, DaoMethodType.UPSERT, null)

        val rawQueryAnnotation = AnnotationUtil.findAnnotation(method, RoomAnnotations.RAW_QUERY)
        if (rawQueryAnnotation != null) return parseRawQueryMethod(method)

        return null
    }

    private fun parseQueryMethod(method: PsiMethod, annotation: PsiAnnotation): DaoMethod {
        val query = AnnotationUtil.getStringAttributeValue(annotation, "value") ?: ""
        val returnType = method.returnType?.presentableText ?: "Unit"
        val parameters = extractParameters(method)

        return DaoMethod(
            name = method.name,
            type = DaoMethodType.QUERY,
            query = query,
            returnType = returnType,
            parameters = parameters,
            touchedEntities = extractTableNamesFromSql(query)
        )
    }

    private fun parseModifyMethod(
        method: PsiMethod,
        type: DaoMethodType,
        annotation: PsiAnnotation?
    ): DaoMethod {
        val onConflict = annotation?.let { resolveOnConflict(it) }
        val returnType = method.returnType?.presentableText ?: "Unit"
        val parameters = extractParameters(method)

        return DaoMethod(
            name = method.name,
            type = type,
            returnType = returnType,
            parameters = parameters,
            onConflict = onConflict
        )
    }

    private fun parseRawQueryMethod(method: PsiMethod): DaoMethod {
        val returnType = method.returnType?.presentableText ?: "Unit"
        val parameters = extractParameters(method)

        return DaoMethod(
            name = method.name,
            type = DaoMethodType.RAW_QUERY,
            returnType = returnType,
            parameters = parameters
        )
    }

    private fun extractParameters(method: PsiMethod): List<DaoParameter> =
        method.parameterList.parameters.map { param ->
            DaoParameter(
                name = param.name,
                type = param.type.presentableText
            )
        }

    private fun resolveOnConflict(annotation: PsiAnnotation): OnConflictStrategy? {
        val value = annotation.findAttributeValue("onConflict") ?: return null
        val text = value.text
        return when {
            text.contains("REPLACE") -> OnConflictStrategy.REPLACE
            text.contains("ABORT") -> OnConflictStrategy.ABORT
            text.contains("IGNORE") -> OnConflictStrategy.IGNORE
            text.contains("ROLLBACK") -> OnConflictStrategy.ROLLBACK
            text.contains("NONE") -> OnConflictStrategy.NONE
            else -> null
        }
    }

    private fun extractTableNamesFromSql(sql: String): List<String> {
        val sqlWithoutStrings = sql.replace(Regex("'[^']*'"), "''")
        val pattern = Regex(
            """(?:FROM|JOIN|INTO|UPDATE)\s+[`"\[]?([a-zA-Z_]\w*)[`"\]]?""",
            RegexOption.IGNORE_CASE
        )
        return pattern.findAll(sqlWithoutStrings)
            .map { it.groupValues[1] }
            .filter { it.uppercase() !in SQL_KEYWORDS }
            .distinct()
            .toList()
    }

    companion object {
        private val SQL_KEYWORDS = setOf(
            "SELECT", "WHERE", "AND", "OR", "NOT", "IN", "AS", "ON",
            "SET", "VALUES", "ORDER", "BY", "GROUP", "HAVING", "LIMIT",
            "OFFSET", "UNION", "ALL", "DISTINCT", "CASE", "WHEN", "THEN",
            "ELSE", "END", "NULL", "TRUE", "FALSE", "LIKE", "BETWEEN",
            "EXISTS", "INNER", "OUTER", "LEFT", "RIGHT", "CROSS", "NATURAL"
        )
    }
}
