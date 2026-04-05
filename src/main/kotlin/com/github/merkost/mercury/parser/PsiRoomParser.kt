package com.github.merkost.mercury.parser

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.stubs.StubIndex
import com.github.merkost.mercury.kmp.SourceSetScanner
import com.github.merkost.mercury.model.*
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.idea.stubindex.KotlinAnnotationsIndex
import org.jetbrains.kotlin.idea.stubindex.KotlinFullClassNameIndex
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

class PsiRoomParser(private val project: Project) {

    private val log = Logger.getInstance(PsiRoomParser::class.java)
    private val sourceSetScanner = SourceSetScanner(project)
    private val entityParser = EntityParser(sourceSetScanner)
    private val daoParser = DaoParser()
    private val typeConverterParser = TypeConverterParser()
    private val databaseViewParser = DatabaseViewParser(sourceSetScanner)

    fun parseProject(): RoomSchema {
        ApplicationManager.getApplication().assertReadAccessAllowed()

        val scope = GlobalSearchScope.projectScope(project)
        val databases = findAndParseDatabases(scope)

        log.info("Mercury: parsed ${databases.size} database(s) in project '${project.name}'")
        return RoomSchema(databases = databases)
    }

    private fun findAndParseDatabases(scope: GlobalSearchScope): List<DatabaseInfo> {
        val javaPsiDatabases = findDatabaseClassesViaJavaPsi(scope)
        if (javaPsiDatabases.isNotEmpty()) {
            log.info("Mercury: found ${javaPsiDatabases.size} @Database class(es) via Java PSI")
            return javaPsiDatabases.mapNotNull { safeParse { parseDatabase(it) } }
        }

        val ktDatabases = findDatabaseKtClassesViaKotlinIndex(scope)
        if (ktDatabases.isNotEmpty()) {
            log.info("Mercury: found ${ktDatabases.size} @Database class(es) via Kotlin index")
            return ktDatabases.mapNotNull { safeParse { parseDatabaseFromKt(it, scope) } }
        }

        return emptyList()
    }

    private fun <T> safeParse(block: () -> T?): T? {
        return try {
            block()
        } catch (e: Exception) {
            log.info("Mercury: skipping element due to parse error: ${e.message}")
            null
        }
    }

    private fun findDatabaseClassesViaJavaPsi(scope: GlobalSearchScope): Collection<PsiClass> {
        val facade = JavaPsiFacade.getInstance(project)
        val annotationClass = facade.findClass(
            RoomAnnotations.DATABASE, GlobalSearchScope.allScope(project)
        ) ?: return emptyList()

        return AnnotatedElementsSearch.searchPsiClasses(annotationClass, scope).findAll()
    }

    private fun findDatabaseKtClassesViaKotlinIndex(scope: GlobalSearchScope): List<KtClassOrObject> {
        return try {
            val entries = StubIndex.getElements(
                KotlinAnnotationsIndex.Helper.indexKey,
                "Database",
                project,
                scope,
                KtAnnotationEntry::class.java
            )

            entries
                .mapNotNull { it.getStrictParentOfType<KtClassOrObject>() }
                .filter { ktClass ->
                    ktClass.annotationEntries.any { it.shortName?.asString() == "Database" }
                }
                .distinctBy { it.fqName?.asString() }
        } catch (e: Exception) {
            log.warn("Mercury: Kotlin annotation index lookup failed", e)
            emptyList()
        }
    }

    private fun parseDatabase(psiClass: PsiClass): DatabaseInfo? {
        val dbAnnotation = AnnotationUtil.findAnnotation(psiClass, RoomAnnotations.DATABASE)
            ?: return null

        val version = AnnotationUtil.getLongAttributeValue(dbAnnotation, "version")?.toInt() ?: 1
        val entities = resolveEntityClasses(dbAnnotation).mapNotNull { entityParser.parse(it) }
        val views = resolveViewClasses(dbAnnotation).mapNotNull { databaseViewParser.parse(it) }
        val typeConverters = extractTypeConverters(psiClass)
        val daos = findDaosForDatabase(psiClass).mapNotNull { daoParser.parse(it) }
        val sourceSet = sourceSetScanner.getSourceSetForElement(psiClass)

        return DatabaseInfo(
            name = psiClass.name ?: return null,
            qualifiedName = psiClass.qualifiedName ?: return null,
            version = version,
            entities = entities,
            views = views,
            typeConverters = typeConverters,
            daos = daos,
            sourceSet = sourceSet
        )
    }

    private fun parseDatabaseFromKt(
        ktClass: KtClassOrObject,
        scope: GlobalSearchScope
    ): DatabaseInfo? {
        val lightClass = ktClass.toLightClass()
        if (lightClass != null) {
            val result = parseDatabase(lightClass)
            if (result != null) {
                val supplemented = supplementForeignKeys(result, scope)
                return supplemented
            }
        }

        log.info("Mercury: light class unavailable for '${ktClass.fqName}', parsing from Kotlin PSI")
        return parseDatabaseFromKotlinPsi(ktClass, scope)
    }

    private fun supplementForeignKeys(db: DatabaseInfo, scope: GlobalSearchScope): DatabaseInfo {
        val needsSupplement = db.entities.any { it.foreignKeys.isEmpty() }
        if (!needsSupplement) return db

        val supplementedEntities = db.entities.map { entity ->
            if (entity.foreignKeys.isNotEmpty()) return@map entity

            val ktClass = resolveKtClassByFqName(entity.qualifiedName, scope) ?: return@map entity
            val annotation = ktClass.findAnnotation("Entity") ?: return@map entity
            val ktForeignKeys = extractForeignKeysFromKt(annotation)
            if (ktForeignKeys.isNotEmpty()) {
                log.info("Mercury: supplemented ${ktForeignKeys.size} FKs for '${entity.name}'")
                entity.copy(foreignKeys = ktForeignKeys)
            } else {
                entity
            }
        }
        return db.copy(entities = supplementedEntities)
    }

    private fun parseDatabaseFromKotlinPsi(
        ktClass: KtClassOrObject,
        scope: GlobalSearchScope
    ): DatabaseInfo? {
        val dbAnnotation = ktClass.findAnnotation("Database") ?: return null
        val version = dbAnnotation.extractIntArgument("version") ?: 1
        val entityFqNames = dbAnnotation.extractClassListArgument("entities")
        val viewFqNames = dbAnnotation.extractClassListArgument("views")

        val entities = entityFqNames.mapNotNull { fqName ->
            resolveKtClassByFqName(fqName, scope)?.let { parseEntityFromKt(it) }
        }

        val views = viewFqNames.mapNotNull { fqName ->
            resolveKtClassByFqName(fqName, scope)?.let { parseViewFromKt(it) }
        }

        val typeConverters = parseTypeConvertersFromKt(ktClass, scope)

        val daos = ktClass.declarations
            .filterIsInstance<KtNamedFunction>()
            .filter { it.hasModifier(org.jetbrains.kotlin.lexer.KtTokens.ABSTRACT_KEYWORD) }
            .mapNotNull { fn ->
                val returnTypeFqName = fn.typeReference?.text ?: return@mapNotNull null
                resolveKtClassByFqName(returnTypeFqName, scope)
                    ?: resolveKtClassByShortName(returnTypeFqName, ktClass, scope)
            }
            .mapNotNull { parseDaoFromKt(it) }

        val sourceSet = sourceSetScanner.getSourceSetForElement(ktClass)

        return DatabaseInfo(
            name = ktClass.name ?: return null,
            qualifiedName = ktClass.fqName?.asString() ?: return null,
            version = version,
            entities = entities,
            views = views,
            typeConverters = typeConverters,
            daos = daos,
            sourceSet = sourceSet
        )
    }

    private fun parseEntityFromKt(ktClass: KtClassOrObject): EntityInfo? {
        val lightClass = ktClass.toLightClass()
        if (lightClass != null) {
            val result = entityParser.parse(lightClass)
            if (result != null && result.foreignKeys.isEmpty()) {
                val annotation = ktClass.findAnnotation("Entity")
                if (annotation != null) {
                    val ktForeignKeys = extractForeignKeysFromKt(annotation)
                    if (ktForeignKeys.isNotEmpty()) {
                        return result.copy(foreignKeys = ktForeignKeys)
                    }
                }
            }
            return result
        }

        val annotation = ktClass.findAnnotation("Entity") ?: return null
        val tableName = annotation.extractStringArgument("tableName")
            ?.takeIf { it.isNotEmpty() }
            ?: ktClass.name
            ?: return null

        val columns = extractColumnsFromKt(ktClass)
        val primaryKey = extractPrimaryKeyFromKt(ktClass, annotation)

        val foreignKeys = extractForeignKeysFromKt(annotation)

        return EntityInfo(
            name = ktClass.name ?: return null,
            tableName = tableName,
            qualifiedName = ktClass.fqName?.asString() ?: return null,
            columns = columns,
            primaryKey = primaryKey,
            foreignKeys = foreignKeys,
            indices = emptyList(),
            sourceSet = sourceSetScanner.getSourceSetForElement(ktClass)
        )
    }

    private fun extractColumnsFromKt(ktClass: KtClassOrObject): List<ColumnInfo> {
        val params = (ktClass as? KtClass)?.primaryConstructorParameters ?: return emptyList()
        return params.mapNotNull { param ->
            if (param.annotationEntries.any { it.shortName?.asString() == "Ignore" }) return@mapNotNull null

            val columnAnnotation = param.annotationEntries
                .find { it.shortName?.asString() == "ColumnInfo" }
            val columnName = columnAnnotation?.extractStringArgument("name")
                ?.takeIf { it.isNotEmpty() }
                ?: param.name
                ?: return@mapNotNull null

            val typeText = param.typeReference?.text ?: "Any"
            val isNullable = typeText.endsWith("?")

            ColumnInfo(
                name = param.name ?: return@mapNotNull null,
                columnName = columnName,
                type = typeText.removeSuffix("?"),
                isNullable = isNullable,
                defaultValue = columnAnnotation?.extractStringArgument("defaultValue")
            )
        }
    }

    private fun extractPrimaryKeyFromKt(
        ktClass: KtClassOrObject,
        entityAnnotation: KtAnnotationEntry
    ): PrimaryKeyInfo {
        val entityPkColumns = entityAnnotation.extractStringListArgument("primaryKeys")
        if (entityPkColumns.isNotEmpty()) {
            return PrimaryKeyInfo(columnNames = entityPkColumns, autoGenerate = false)
        }

        val params = (ktClass as? KtClass)?.primaryConstructorParameters ?: return PrimaryKeyInfo(listOf("rowid"))
        for (param in params) {
            val pkAnnotation = param.annotationEntries
                .find { it.shortName?.asString() == "PrimaryKey" }
            if (pkAnnotation != null) {
                val autoGenerate = pkAnnotation.extractBooleanArgument("autoGenerate") ?: false
                val columnAnnotation = param.annotationEntries
                    .find { it.shortName?.asString() == "ColumnInfo" }
                val columnName = columnAnnotation?.extractStringArgument("name")
                    ?.takeIf { it.isNotEmpty() }
                    ?: param.name
                    ?: continue
                return PrimaryKeyInfo(columnNames = listOf(columnName), autoGenerate = autoGenerate)
            }
        }
        return PrimaryKeyInfo(columnNames = listOf("rowid"))
    }

    private fun extractForeignKeysFromKt(entityAnnotation: KtAnnotationEntry): List<ForeignKeyInfo> {
        val fkArgs = entityAnnotation.valueArgumentList?.arguments
            ?.find { it.getArgumentName()?.asName?.asString() == "foreignKeys" }
            ?: return emptyList()

        val expr = fkArgs.getArgumentExpression() ?: return emptyList()

        val collector = expr as? KtCollectionLiteralExpression
        if (collector == null) {
            val innerExprs = expr.children.mapNotNull { it as? KtExpression }
            return innerExprs.mapNotNull { parseFkExpression(it) }
        }

        val inner = collector.getInnerExpressions()

        return inner.mapNotNull { fkExpr ->
            parseFkExpression(fkExpr)
        }
    }

    private fun parseFkExpression(fkExpr: KtExpression): ForeignKeyInfo? {
        val args = when (fkExpr) {
            is KtCallExpression -> fkExpr.valueArgumentList?.arguments
            is KtAnnotationEntry -> fkExpr.valueArgumentList?.arguments
            else -> null
        } ?: return null

        val entityRef = args.find { it.getArgumentName()?.asName?.asString() == "entity" }
            ?.getArgumentExpression()
        val parentEntity = if (entityRef != null) {
            extractSingleClassReference(entityRef)?.substringAfterLast(".")
                ?: entityRef.text?.removeSuffix("::class")?.substringAfterLast(".")
        } else null
        if (parentEntity == null) return null

        val parentColumns = args.find { it.getArgumentName()?.asName?.asString() == "parentColumns" }
            ?.getArgumentExpression()?.let { extractStringListFromExpression(it) }
            ?: return null

        val childColumns = args.find { it.getArgumentName()?.asName?.asString() == "childColumns" }
            ?.getArgumentExpression()?.let { extractStringListFromExpression(it) }
            ?: return null

        val onDelete = args.find { it.getArgumentName()?.asName?.asString() == "onDelete" }
            ?.getArgumentExpression()?.text?.let { resolveKtForeignKeyAction(it) }
            ?: ForeignKeyAction.NO_ACTION

        val onUpdate = args.find { it.getArgumentName()?.asName?.asString() == "onUpdate" }
            ?.getArgumentExpression()?.text?.let { resolveKtForeignKeyAction(it) }
            ?: ForeignKeyAction.NO_ACTION

        return ForeignKeyInfo(
            parentEntity = parentEntity,
            parentColumns = parentColumns,
            childColumns = childColumns,
            onDelete = onDelete,
            onUpdate = onUpdate
        )
    }

    private fun extractStringListFromExpression(expr: KtExpression): List<String> {
        val collector = expr as? KtCollectionLiteralExpression ?: return emptyList()
        return collector.getInnerExpressions().mapNotNull { inner ->
            when (inner) {
                is KtStringTemplateExpression -> inner.entries.joinToString("") {
                    (it as? KtLiteralStringTemplateEntry)?.text ?: ""
                }
                else -> inner.text.removeSurrounding("\"")
            }
        }
    }

    private fun resolveKtForeignKeyAction(text: String): ForeignKeyAction = when {
        text.contains("CASCADE") -> ForeignKeyAction.CASCADE
        text.contains("RESTRICT") -> ForeignKeyAction.RESTRICT
        text.contains("SET_NULL") -> ForeignKeyAction.SET_NULL
        text.contains("SET_DEFAULT") -> ForeignKeyAction.SET_DEFAULT
        else -> ForeignKeyAction.NO_ACTION
    }

    private fun parseViewFromKt(ktClass: KtClassOrObject): DatabaseViewInfo? {
        val lightClass = ktClass.toLightClass()
        if (lightClass != null) return databaseViewParser.parse(lightClass)

        val annotation = ktClass.findAnnotation("DatabaseView") ?: return null
        val query = annotation.extractStringArgument("value") ?: return null
        val viewName = annotation.extractStringArgument("viewName")
            ?.takeIf { it.isNotEmpty() }
            ?: ktClass.name
            ?: return null

        return DatabaseViewInfo(
            name = ktClass.name ?: return null,
            viewName = viewName,
            qualifiedName = ktClass.fqName?.asString() ?: return null,
            query = query
        )
    }

    private fun parseDaoFromKt(ktClass: KtClassOrObject): DaoInfo? {
        val lightClass = ktClass.toLightClass()
        if (lightClass != null) return daoParser.parse(lightClass)

        if (ktClass.findAnnotation("Dao") == null) return null
        val methods = ktClass.declarations
            .filterIsInstance<KtNamedFunction>()
            .mapNotNull { parseDaoMethodFromKt(it) }

        return DaoInfo(
            name = ktClass.name ?: return null,
            qualifiedName = ktClass.fqName?.asString() ?: return null,
            methods = methods
        )
    }

    private fun parseDaoMethodFromKt(fn: KtNamedFunction): DaoMethod? {
        val annotations = fn.annotationEntries
        val returnType = fn.typeReference?.text ?: "Unit"
        val parameters = fn.valueParameters.map { param ->
            DaoParameter(name = param.name ?: "_", type = param.typeReference?.text ?: "Any")
        }

        annotations.find { it.shortName?.asString() == "Query" }?.let { ann ->
            val query = ann.extractStringArgument("value") ?: ""
            return DaoMethod(
                name = fn.name ?: return null,
                type = DaoMethodType.QUERY,
                query = query,
                returnType = returnType,
                parameters = parameters,
                touchedEntities = emptyList()
            )
        }

        val typeMap = mapOf(
            "Insert" to DaoMethodType.INSERT,
            "Update" to DaoMethodType.UPDATE,
            "Delete" to DaoMethodType.DELETE,
            "Upsert" to DaoMethodType.UPSERT,
            "RawQuery" to DaoMethodType.RAW_QUERY
        )
        for ((annotationName, methodType) in typeMap) {
            if (annotations.any { it.shortName?.asString() == annotationName }) {
                return DaoMethod(
                    name = fn.name ?: return null,
                    type = methodType,
                    returnType = returnType,
                    parameters = parameters
                )
            }
        }
        return null
    }

    private fun parseTypeConvertersFromKt(
        dbClass: KtClassOrObject,
        scope: GlobalSearchScope
    ): List<TypeConverterInfo> {
        val lightClass = dbClass.toLightClass()
        if (lightClass != null) {
            val converterClasses = typeConverterParser.findConverterClasses(lightClass)
            if (converterClasses.isNotEmpty()) {
                return converterClasses.flatMap { typeConverterParser.parse(it) }
            }
        }

        val tcAnnotation = dbClass.findAnnotation("TypeConverters") ?: return emptyList()
        val classFqNames = tcAnnotation.extractClassListArgument("value")
        return classFqNames.flatMap { fqName ->
            val ktConverter = resolveKtClassByFqName(fqName, scope) ?: return@flatMap emptyList()
            val lightConverter = ktConverter.toLightClass()
            if (lightConverter != null) {
                typeConverterParser.parse(lightConverter)
            } else {
                emptyList()
            }
        }
    }

    private fun resolveEntityClasses(dbAnnotation: PsiAnnotation): List<PsiClass> {
        val entitiesAttr = dbAnnotation.findAttributeValue("entities") ?: return emptyList()
        return AnnotationUtil.arrayAttributeValues(entitiesAttr)
            .mapNotNull { PsiAnnotationUtils.resolveClassReference(it) }
    }

    private fun resolveViewClasses(dbAnnotation: PsiAnnotation): List<PsiClass> {
        val viewsAttr = dbAnnotation.findAttributeValue("views") ?: return emptyList()
        return AnnotationUtil.arrayAttributeValues(viewsAttr)
            .mapNotNull { PsiAnnotationUtils.resolveClassReference(it) }
    }

    private fun extractTypeConverters(dbClass: PsiClass): List<TypeConverterInfo> {
        val converterClasses = typeConverterParser.findConverterClasses(dbClass)
        return converterClasses.flatMap { typeConverterParser.parse(it) }
    }

    private fun findDaosForDatabase(dbClass: PsiClass): List<PsiClass> {
        return dbClass.allMethods
            .filter { it.hasModifierProperty(PsiModifier.ABSTRACT) }
            .mapNotNull { method ->
                val returnType = method.returnType as? PsiClassType ?: return@mapNotNull null
                val resolved = returnType.resolve() ?: return@mapNotNull null
                if (AnnotationUtil.isAnnotated(resolved, RoomAnnotations.DAO, 0)) resolved else null
            }
    }

    private fun resolveKtClassByFqName(fqName: String, scope: GlobalSearchScope): KtClassOrObject? {
        val results = KotlinFullClassNameIndex.get(fqName, project, scope)
        return results.firstOrNull()
    }

    private fun resolveKtClassByShortName(
        shortName: String,
        context: KtClassOrObject,
        scope: GlobalSearchScope
    ): KtClassOrObject? {
        val containingFile = context.containingKtFile
        val importedFqName = containingFile.importDirectives
            .mapNotNull { it.importedFqName?.asString() }
            .find { it.endsWith(".$shortName") }

        if (importedFqName != null) {
            return resolveKtClassByFqName(importedFqName, scope)
        }

        val packageFqName = containingFile.packageFqName.asString()
        if (packageFqName.isNotEmpty()) {
            return resolveKtClassByFqName("$packageFqName.$shortName", scope)
        }
        return null
    }
}

private fun KtClassOrObject.findAnnotation(shortName: String): KtAnnotationEntry? =
    annotationEntries.find { it.shortName?.asString() == shortName }

private fun KtAnnotationEntry.extractStringArgument(name: String): String? {
    val args = valueArgumentList?.arguments ?: return null
    val arg = args.find { it.getArgumentName()?.asName?.asString() == name }
        ?: if (name == "value") args.firstOrNull() else null
    val expr = arg?.getArgumentExpression() ?: return null
    return when (expr) {
        is KtStringTemplateExpression -> expr.entries.joinToString("") {
            (it as? KtLiteralStringTemplateEntry)?.text ?: ""
        }
        else -> expr.text.removeSurrounding("\"")
    }
}

private fun KtAnnotationEntry.extractIntArgument(name: String): Int? {
    val args = valueArgumentList?.arguments ?: return null
    val arg = args.find { it.getArgumentName()?.asName?.asString() == name } ?: return null
    return arg.getArgumentExpression()?.text?.toIntOrNull()
}

private fun KtAnnotationEntry.extractBooleanArgument(name: String): Boolean? {
    val args = valueArgumentList?.arguments ?: return null
    val arg = args.find { it.getArgumentName()?.asName?.asString() == name } ?: return null
    return arg.getArgumentExpression()?.text?.toBooleanStrictOrNull()
}

private fun KtAnnotationEntry.extractClassListArgument(name: String): List<String> {
    val args = valueArgumentList?.arguments ?: return emptyList()
    val arg = args.find { it.getArgumentName()?.asName?.asString() == name }
        ?: if (name == "value") args.firstOrNull() else null
    val expr = arg?.getArgumentExpression() ?: return emptyList()
    return extractClassReferences(expr)
}

private fun KtAnnotationEntry.extractStringListArgument(name: String): List<String> {
    val args = valueArgumentList?.arguments ?: return emptyList()
    val arg = args.find { it.getArgumentName()?.asName?.asString() == name } ?: return emptyList()
    val expr = arg.getArgumentExpression() ?: return emptyList()

    val collector = expr as? KtCollectionLiteralExpression ?: return emptyList()
    return collector.getInnerExpressions().mapNotNull { inner ->
        when (inner) {
            is KtStringTemplateExpression -> inner.entries.joinToString("") {
                (it as? KtLiteralStringTemplateEntry)?.text ?: ""
            }
            else -> inner.text.removeSurrounding("\"")
        }
    }
}

private fun extractClassReferences(expr: KtExpression): List<String> {
    return when (expr) {
        is KtCollectionLiteralExpression -> {
            expr.getInnerExpressions().mapNotNull { inner ->
                extractSingleClassReference(inner)
            }
        }
        is KtClassLiteralExpression -> {
            listOfNotNull(extractSingleClassReference(expr))
        }
        else -> emptyList()
    }
}

private fun extractSingleClassReference(expr: KtExpression): String? {
    if (expr !is KtClassLiteralExpression) return null
    val typeRef = expr.receiverExpression ?: return null
    val shortName = typeRef.text

    val containingFile = expr.containingKtFile
    val importedFqName = containingFile.importDirectives
        .mapNotNull { it.importedFqName?.asString() }
        .find { it.endsWith(".$shortName") }

    return importedFqName ?: run {
        val pkg = containingFile.packageFqName.asString()
        if (pkg.isNotEmpty()) "$pkg.$shortName" else shortName
    }
}
