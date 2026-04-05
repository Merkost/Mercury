package com.github.merkost.mercury.services

import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project

class MercuryDumbModeListener(private val project: Project) : DumbService.DumbModeListener {

    override fun exitDumbMode() {
        project.service<MercuryProjectService>().refreshSchema()
    }
}
