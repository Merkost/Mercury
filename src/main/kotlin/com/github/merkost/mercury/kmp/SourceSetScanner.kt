package com.github.merkost.mercury.kmp

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

class SourceSetScanner(private val project: Project) {

    fun getAllModules(): Array<Module> =
        ModuleManager.getInstance(project).modules

    fun inferSourceSet(module: Module): String {
        val parts = module.name.split(".")
        return when {
            parts.size >= 2 -> {
                val last = parts.last()
                if (last == "main" || last == "test") {
                    parts.getOrNull(parts.size - 2) ?: "main"
                } else {
                    last
                }
            }
            else -> "main"
        }
    }

    fun getSourceSetForElement(element: PsiElement): String {
        val module = ModuleUtil.findModuleForPsiElement(element) ?: return "main"
        return inferSourceSet(module)
    }

    fun getSourceSetModules(): Map<String, List<Module>> =
        getAllModules().groupBy { inferSourceSet(it) }
}
