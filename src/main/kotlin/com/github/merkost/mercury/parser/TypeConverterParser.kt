package com.github.merkost.mercury.parser

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.psi.PsiAnnotationMemberValue
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassObjectAccessExpression
import com.intellij.psi.PsiClassType
import com.github.merkost.mercury.model.TypeConverterInfo

class TypeConverterParser {

    fun parse(psiClass: PsiClass): List<TypeConverterInfo> {
        return psiClass.allMethods
            .filter { AnnotationUtil.isAnnotated(it, RoomAnnotations.TYPE_CONVERTER, 0) }
            .mapNotNull { method ->
                val params = method.parameterList.parameters
                if (params.isEmpty()) return@mapNotNull null

                val fromType = params.first().type.presentableText
                val toType = method.returnType?.presentableText ?: return@mapNotNull null

                TypeConverterInfo(
                    name = method.name,
                    qualifiedName = "${psiClass.qualifiedName}.${method.name}",
                    fromType = fromType,
                    toType = toType
                )
            }
    }

    fun findConverterClasses(psiClass: PsiClass): List<PsiClass> {
        val typeConvertersAnnotation = AnnotationUtil.findAnnotation(
            psiClass, RoomAnnotations.TYPE_CONVERTERS
        ) ?: return emptyList()

        val valueAttr = typeConvertersAnnotation.findAttributeValue("value") ?: return emptyList()
        val values = AnnotationUtil.arrayAttributeValues(valueAttr)

        return values.mapNotNull { memberValue ->
            resolveClassFromAnnotationValue(memberValue)
        }
    }

    private fun resolveClassFromAnnotationValue(value: PsiAnnotationMemberValue): PsiClass? {
        if (value is PsiClassObjectAccessExpression) {
            val type = value.operand.type as? PsiClassType ?: return null
            return type.resolve()
        }
        return null
    }
}
