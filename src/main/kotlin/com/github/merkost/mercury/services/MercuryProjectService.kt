package com.github.merkost.mercury.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.github.merkost.mercury.model.RoomSchema

@Service(Service.Level.PROJECT)
class MercuryProjectService(private val project: Project) {

    @Volatile
    var schema: RoomSchema = RoomSchema(databases = emptyList())
        private set

    fun updateSchema(newSchema: RoomSchema) {
        schema = newSchema
    }
}
