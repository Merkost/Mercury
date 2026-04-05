package com.github.merkost.mercury.parser

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.*
import com.github.merkost.mercury.kmp.SourceSetScanner
import com.github.merkost.mercury.model.*
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.asJava.elements.KtLightField

class EntityParser(private val sourceSetScanner: SourceSetScanner) {

    private val log = Logger.getInstance(EntityParser::class.java)

    fun parse(psiClass: PsiClass): EntityInfo? {
        val entityAnnotation = AnnotationUtil.findAnnotation(psiClass, RoomAnnotations.ENTITY)
            ?: return null

        val tableName = AnnotationUtil.getStringAttributeValue(entityAnnotation, "tableName")
            ?.takeIf { it.isNotEmpty() }
            ?: psiClass.name
            ?: return null

        val columns = extractColumns(psiClass)
        val primaryKey = extractPrimaryKey(psiClass, entityAnnotation)
        val foreignKeys = extractForeignKeys(entityAnnotation)
        val indices = extractIndices(entityAnnotation)
        val sourceSet = sourceSetScanner.getSourceSetForElement(psiClass)

        return EntityInfo(
            name = psiClass.name ?: return null,
            tableName = tableName,
            qualifiedName = psiClass.qualifiedName ?: return null,
            columns = columns,
            primaryKey = primaryKey,
            foreignKeys = foreignKeys,
            indices = indices,
            sourceSet = sourceSet
        )
    }

    private fun extractColumns(psiClass: PsiClass): List<ColumnInfo> {
        val columns = mutableListOf<ColumnInfo>()

        for (field in psiClass.allFields) {
            if (AnnotationUtil.isAnnotated(field, RoomAnnotations.IGNORE, 0)) continue
            if (findAnnotationOnAccessors(field, psiClass, RoomAnnotations.IGNORE) != null) continue
            if (field.hasModifierProperty(PsiModifier.STATIC)) continue
            if (field.hasModifierProperty(PsiModifier.TRANSIENT)) continue

            val embedded = AnnotationUtil.findAnnotation(field, RoomAnnotations.EMBEDDED)
                ?: findAnnotationOnAccessors(field, psiClass, RoomAnnotations.EMBEDDED)
            if (embedded != null) {
                val prefix = AnnotationUtil.getStringAttributeValue(embedded, "prefix") ?: ""
                val embeddedType = field.type
                val embeddedClass = (embeddedType as? PsiClassType)?.resolve()
                if (embeddedClass != null) {
                    val embeddedColumns = extractColumns(embeddedClass)
                    embeddedColumns.forEach { col ->
                        columns.add(
                            col.copy(
                                columnName = prefix + col.columnName,
                                isEmbedded = true,
                                embeddedPrefix = prefix.takeIf { it.isNotEmpty() }
                            )
                        )
                    }
                }
                continue
            }

            columns.add(extractColumn(field, psiClass))
        }

        return columns
    }

    private fun extractColumn(field: PsiField, psiClass: PsiClass): ColumnInfo {
        val columnAnnotation = AnnotationUtil.findAnnotation(field, RoomAnnotations.COLUMN_INFO)
            ?: findAnnotationOnAccessors(field, psiClass, RoomAnnotations.COLUMN_INFO)
        val columnName = columnAnnotation?.let {
            AnnotationUtil.getStringAttributeValue(it, "name")?.takeIf { name -> name.isNotEmpty() }
        } ?: field.name

        val type = field.type.presentableText
        val isNullable = isFieldNullable(field)

        val defaultValue = columnAnnotation?.let {
            AnnotationUtil.getStringAttributeValue(it, "defaultValue")
        }

        return ColumnInfo(
            name = field.name,
            columnName = columnName,
            type = type,
            isNullable = isNullable,
            defaultValue = defaultValue
        )
    }

    private fun isFieldNullable(field: PsiField): Boolean {
        if (field.type.annotations.any { it.qualifiedName?.contains("Nullable") == true }) return true
        if (field.annotations.any { it.qualifiedName?.contains("Nullable") == true }) return true
        val ktOrigin = (field as? KtLightField)?.kotlinOrigin
        if (ktOrigin is KtProperty) {
            return ktOrigin.typeReference?.text?.endsWith("?") == true
        }
        return false
    }

    private fun extractPrimaryKey(psiClass: PsiClass, entityAnnotation: PsiAnnotation): PrimaryKeyInfo {
        val entityPkAttr = entityAnnotation.findAttributeValue("primaryKeys")
        val entityPkColumns = PsiAnnotationUtils.extractStringArray(entityPkAttr)
        if (entityPkColumns.isNotEmpty()) {
            return PrimaryKeyInfo(columnNames = entityPkColumns, autoGenerate = false)
        }

        for (field in psiClass.allFields) {
            val pkAnnotation = findPrimaryKeyAnnotation(field, psiClass)
            if (pkAnnotation != null) {
                val autoGenerate = AnnotationUtil.getBooleanAttributeValue(pkAnnotation, "autoGenerate")
                    ?: pkAnnotation.findAttributeValue("autoGenerate")?.text?.toBooleanStrictOrNull()
                    ?: false
                val columnAnnotation = AnnotationUtil.findAnnotation(field, RoomAnnotations.COLUMN_INFO)
                    ?: findAnnotationOnAccessors(field, psiClass, RoomAnnotations.COLUMN_INFO)
                val columnName = columnAnnotation?.let {
                    AnnotationUtil.getStringAttributeValue(it, "name")?.takeIf { n -> n.isNotEmpty() }
                } ?: field.name
                return PrimaryKeyInfo(columnNames = listOf(columnName), autoGenerate = autoGenerate)
            }
        }

        return PrimaryKeyInfo(columnNames = listOf("rowid"))
    }

    private fun findPrimaryKeyAnnotation(field: PsiField, psiClass: PsiClass): PsiAnnotation? {
        AnnotationUtil.findAnnotation(field, RoomAnnotations.PRIMARY_KEY)?.let { return it }
        return findAnnotationOnAccessors(field, psiClass, RoomAnnotations.PRIMARY_KEY)
    }

    private fun findAnnotationOnAccessors(field: PsiField, psiClass: PsiClass, annotationFqn: String): PsiAnnotation? {
        val getterName = "get${field.name.replaceFirstChar { it.uppercase() }}"
        val getter = psiClass.findMethodsByName(getterName, false).firstOrNull()
        getter?.let { AnnotationUtil.findAnnotation(it, annotationFqn)?.let { ann -> return ann } }

        for (method in psiClass.constructors) {
            for (param in method.parameterList.parameters) {
                if (param.name == field.name) {
                    AnnotationUtil.findAnnotation(param, annotationFqn)?.let { return it }
                }
            }
        }
        return null
    }

    private fun extractForeignKeys(entityAnnotation: PsiAnnotation): List<ForeignKeyInfo> {
        val fkAttr = entityAnnotation.findAttributeValue("foreignKeys") ?: return emptyList()
        val fkAnnotations = PsiAnnotationUtils.extractNestedAnnotations(fkAttr)

        return fkAnnotations.mapNotNull { fkAnnotation ->
            val entityRef = fkAnnotation.findAttributeValue("entity")
            val parentEntity = PsiAnnotationUtils.resolveClassReference(entityRef)?.name ?: return@mapNotNull null
            val parentColumns = PsiAnnotationUtils.extractStringArray(fkAnnotation.findAttributeValue("parentColumns"))
            val childColumns = PsiAnnotationUtils.extractStringArray(fkAnnotation.findAttributeValue("childColumns"))
            val onDelete = resolveOnAction(fkAnnotation.findAttributeValue("onDelete"))
            val onUpdate = resolveOnAction(fkAnnotation.findAttributeValue("onUpdate"))

            ForeignKeyInfo(
                parentEntity = parentEntity,
                parentColumns = parentColumns,
                childColumns = childColumns,
                onDelete = onDelete,
                onUpdate = onUpdate
            )
        }
    }

    private fun extractIndices(entityAnnotation: PsiAnnotation): List<IndexInfo> {
        val indicesAttr = entityAnnotation.findAttributeValue("indices") ?: return emptyList()
        val indexAnnotations = PsiAnnotationUtils.extractNestedAnnotations(indicesAttr)

        return indexAnnotations.mapNotNull { indexAnnotation ->
            val columnNames = PsiAnnotationUtils.extractStringArray(indexAnnotation.findAttributeValue("value"))
            if (columnNames.isEmpty()) return@mapNotNull null
            val isUnique = AnnotationUtil.getBooleanAttributeValue(indexAnnotation, "unique") ?: false
            val name = AnnotationUtil.getStringAttributeValue(indexAnnotation, "name") ?: ""

            IndexInfo(
                name = name.ifEmpty { "index_${columnNames.joinToString("_")}" },
                columnNames = columnNames,
                isUnique = isUnique
            )
        }
    }

    private fun resolveOnAction(value: PsiAnnotationMemberValue?): ForeignKeyAction {
        val text = value?.text ?: return ForeignKeyAction.NO_ACTION
        return when {
            text.contains("CASCADE") -> ForeignKeyAction.CASCADE
            text.contains("RESTRICT") -> ForeignKeyAction.RESTRICT
            text.contains("SET_NULL") -> ForeignKeyAction.SET_NULL
            text.contains("SET_DEFAULT") -> ForeignKeyAction.SET_DEFAULT
            else -> ForeignKeyAction.NO_ACTION
        }
    }
}
