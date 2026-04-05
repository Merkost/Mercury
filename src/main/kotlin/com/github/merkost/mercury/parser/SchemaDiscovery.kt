package com.github.merkost.mercury.parser

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.github.merkost.mercury.model.DatabaseInfo

data class SchemaVersion(
    val databaseQualifiedName: String,
    val version: Int,
    val file: VirtualFile
)

object SchemaDiscovery {

    fun findSchemaVersions(project: Project): List<SchemaVersion> {
        val projectDir = LocalFileSystem.getInstance().findFileByPath(project.basePath ?: return emptyList())
            ?: return emptyList()

        val versions = mutableListOf<SchemaVersion>()
        VfsUtilCore.iterateChildrenRecursively(projectDir, { file ->
            !file.path.contains("/build/") && !file.path.contains("/.gradle/")
        }) { file ->
            if (file.isDirectory && file.name == "schemas") {
                collectVersionsFromSchemasDir(file, versions)
            }
            true
        }

        return versions.sortedWith(compareBy({ it.databaseQualifiedName }, { it.version }))
    }

    fun loadVersion(version: SchemaVersion): DatabaseInfo? {
        val content = try {
            String(version.file.contentsToByteArray())
        } catch (e: Exception) {
            return null
        }
        val db = JsonSchemaParser.parse(content) ?: return null
        return db.copy(
            name = "v${version.version}",
            qualifiedName = version.databaseQualifiedName
        )
    }

    private fun collectVersionsFromSchemasDir(schemasDir: VirtualFile, versions: MutableList<SchemaVersion>) {
        for (dbDir in schemasDir.children) {
            if (!dbDir.isDirectory) continue
            val dbQualifiedName = dbDir.name

            for (jsonFile in dbDir.children) {
                if (!jsonFile.name.endsWith(".json")) continue
                val versionNumber = jsonFile.nameWithoutExtension.toIntOrNull() ?: continue
                versions.add(SchemaVersion(dbQualifiedName, versionNumber, jsonFile))
            }
        }
    }
}
