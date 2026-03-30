package com.github.merkost.mercury.ui.toolwindow

import androidx.compose.runtime.*
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.github.merkost.mercury.services.MercuryProjectService
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.bridge.addComposeTab

class MercuryToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val service = project.service<MercuryProjectService>()

        toolWindow.addComposeTab("Mercury") {
            MercuryTheme(isDark = true) {
                val schema = service.schema
                val databaseNames = schema.databases.map { it.name }
                var selectedDatabase by remember {
                    mutableStateOf(databaseNames.firstOrNull() ?: "")
                }

                MercuryToolWindowPanel(
                    databaseNames = databaseNames,
                    selectedDatabase = selectedDatabase,
                    onDatabaseSelected = { selectedDatabase = it }
                )
            }
        }
    }

    override fun shouldBeAvailable(project: Project) = true
}
