package com.github.merkost.mercury.parser

import com.intellij.openapi.project.Project
import com.github.merkost.mercury.model.RoomSchema

class SchemaResolver(private val project: Project) {

    private val psiParser = PsiRoomParser(project)

    fun resolve(): RoomSchema = psiParser.parseProject()
}
