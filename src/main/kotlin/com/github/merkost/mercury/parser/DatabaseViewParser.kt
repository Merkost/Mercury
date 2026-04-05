package com.github.merkost.mercury.parser

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.psi.PsiClass
import com.github.merkost.mercury.kmp.SourceSetScanner
import com.github.merkost.mercury.model.DatabaseViewInfo

class DatabaseViewParser(private val sourceSetScanner: SourceSetScanner) {

    fun parse(psiClass: PsiClass): DatabaseViewInfo? {
        val viewAnnotation = AnnotationUtil.findAnnotation(psiClass, RoomAnnotations.DATABASE_VIEW)
            ?: return null

        val query = AnnotationUtil.getStringAttributeValue(viewAnnotation, "value") ?: return null
        val viewName = AnnotationUtil.getStringAttributeValue(viewAnnotation, "viewName")
            ?.takeIf { it.isNotEmpty() }
            ?: psiClass.name
            ?: return null

        return DatabaseViewInfo(
            name = psiClass.name ?: return null,
            viewName = viewName,
            qualifiedName = psiClass.qualifiedName ?: return null,
            query = query
        )
    }
}
