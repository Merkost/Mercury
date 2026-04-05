package com.github.merkost.mercury

import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.github.merkost.mercury.services.MercuryProjectService
import com.github.merkost.mercury.model.MercuryUiState

class MercuryPluginTest : BasePlatformTestCase() {

    fun testProjectServiceLoads() {
        val service = project.service<MercuryProjectService>()
        assertNotNull(service)
    }

    fun testInitialUiStateIsLoadingOrEmpty() {
        val service = project.service<MercuryProjectService>()
        val state = service.uiState.value
        assertTrue(
            state is MercuryUiState.Loading || state is MercuryUiState.Empty
        )
    }
}
