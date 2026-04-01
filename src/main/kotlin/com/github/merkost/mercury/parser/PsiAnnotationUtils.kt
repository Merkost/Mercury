package com.github.merkost.mercury.parser

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.psi.*

object PsiAnnotationUtils {

    fun extractStringArray(value: PsiAnnotationMemberValue?): List<String> {
        if (value == null) return emptyList()
        val values = AnnotationUtil.arrayAttributeValues(value)
        return values.mapNotNull { memberValue ->
            when (memberValue) {
                is PsiLiteralExpression -> memberValue.value as? String
                else -> memberValue.text.removeSurrounding("\"")
            }
        }
    }

    fun extractNestedAnnotations(value: PsiAnnotationMemberValue?): List<PsiAnnotation> {
        if (value == null) return emptyList()
        val values = AnnotationUtil.arrayAttributeValues(value)
        return values.filterIsInstance<PsiAnnotation>()
    }

    fun resolveClassReference(value: PsiAnnotationMemberValue?): PsiClass? {
        if (value == null) return null
        if (value is PsiClassObjectAccessExpression) {
            val type = value.operand.type as? PsiClassType ?: return null
            return type.resolve()
        }
        return null
    }
}
