package com.github.merkost.mercury.ui.navigation

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.GlobalSearchScope
import com.github.merkost.mercury.ui.tree.SchemaTreeNode

object PsiNavigationBridge {

    private val log = Logger.getInstance(PsiNavigationBridge::class.java)

    fun navigateToNode(project: Project, node: SchemaTreeNode) {
        when (node) {
            is SchemaTreeNode.Database -> navigateToClass(project, node.info.qualifiedName)
            is SchemaTreeNode.Entity -> navigateToClass(project, node.info.qualifiedName)
            is SchemaTreeNode.Column -> navigateToField(project, node.parentQualifiedName, node.info.name)
            is SchemaTreeNode.Dao -> navigateToClass(project, node.info.qualifiedName)
            is SchemaTreeNode.DaoMethodNode -> navigateToMethod(project, node.parentQualifiedName, node.method.name)
            is SchemaTreeNode.View -> navigateToClass(project, node.info.qualifiedName)
            is SchemaTreeNode.TypeConverter -> navigateToClass(project, node.info.qualifiedName.substringBeforeLast('.'))
            is SchemaTreeNode.Index -> navigateToClass(project, node.parentQualifiedName)
            is SchemaTreeNode.ForeignKey -> navigateToClass(project, node.parentQualifiedName)
            is SchemaTreeNode.SectionHeader -> {}
            is SchemaTreeNode.SectionDivider -> {}
        }
    }

    fun navigateToClass(project: Project, qualifiedName: String) {
        ReadAction.run<Throwable> {
            val psiClass = JavaPsiFacade.getInstance(project)
                .findClass(qualifiedName, GlobalSearchScope.projectScope(project))
            if (psiClass != null) {
                psiClass.navigate(true)
            } else {
                log.warn("Class not found for navigation: $qualifiedName")
            }
        }
    }

    private fun navigateToField(project: Project, classQualifiedName: String, fieldName: String) {
        ReadAction.run<Throwable> {
            val psiClass = JavaPsiFacade.getInstance(project)
                .findClass(classQualifiedName, GlobalSearchScope.projectScope(project))
            val field = psiClass?.findFieldByName(fieldName, false)
            val target: Navigatable = field ?: psiClass ?: return@run
            target.navigate(true)
        }
    }

    fun navigateToMethod(project: Project, classQualifiedName: String, methodName: String) {
        ReadAction.run<Throwable> {
            val psiClass = JavaPsiFacade.getInstance(project)
                .findClass(classQualifiedName, GlobalSearchScope.projectScope(project))
            val method = psiClass?.findMethodsByName(methodName, false)?.firstOrNull()
            val target: Navigatable = method ?: psiClass ?: return@run
            target.navigate(true)
        }
    }
}
