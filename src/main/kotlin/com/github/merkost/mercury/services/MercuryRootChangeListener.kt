package com.github.merkost.mercury.services

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootEvent
import com.intellij.openapi.roots.ModuleRootListener

class MercuryRootChangeListener(private val project: Project) : ModuleRootListener {

    override fun rootsChanged(event: ModuleRootEvent) {
        project.service<MercuryProjectService>().refreshSchema()
    }
}
