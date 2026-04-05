package com.github.merkost.mercury.ui.toolwindow

import androidx.compose.runtime.*
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBColor
import com.github.merkost.mercury.services.MercuryProjectService
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.bridge.addComposeTab

class MercuryToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val service = project.service<MercuryProjectService>()
        service.refreshSchema()

        toolWindow.addComposeTab {
            val isDark = !JBColor.isBright()
            val uiState by service.uiState

            MercuryTheme(isDark = isDark) {
                MercuryToolWindowPanel(
                    project = project,
                    uiState = uiState,
                    onRefresh = { service.refreshSchema() }
                )
            }
        }
    }

    override fun shouldBeAvailable(project: Project) = true
}
