package com.github.rinchinov.ijdbtplugin.services

import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.yaml.snakeyaml.Yaml
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


@Service(Service.Level.PROJECT)
class ProjectConfigurations(private val project: Project) {
    private val settings = project.service<ProjectSettings>()
    private val dbtNotifications = project.service<Notifications>()
    val dbtProjectConfig = DbtProjectConfig(
        "",
        1,
        "",
        "",
        "target"
    )
    data class DbtProjectConfig(
        var name: String,
        var configVersion: Int,
        var version: String,
        var profile: String,
        var targetPath: String
    )
    fun reloadDbtProjectSettings(){
        val filePath = dbtProjectPath().absolutePath.toString()
        try {
            val inputStream: InputStream = Files.newInputStream(Paths.get(filePath))
            val projectSettingRaw = Yaml().load(inputStream) as Map<String, Any>?
            if (projectSettingRaw != null) {
                dbtProjectConfig.name = projectSettingRaw["name"] as String
                dbtProjectConfig.configVersion = projectSettingRaw["config-version"] as Int
                dbtProjectConfig.version = projectSettingRaw["version"] as String
                dbtProjectConfig.profile = projectSettingRaw["profile"] as String
                dbtProjectConfig.targetPath = projectSettingRaw["target-path"] as String
                dbtNotifications.sendNotification("Load project configs", dbtProjectConfig.toString(), NotificationType.INFORMATION)
            }
            else {
                dbtNotifications.sendNotification("Load project failed", filePath, NotificationType.ERROR)
            }
        } catch (e: FileNotFoundException) {
            dbtNotifications.sendNotification("File not found", "$filePath\n TBD Instruction and link to the doc", NotificationType.ERROR)
        } catch (e: Exception) {
            dbtNotifications.sendNotification("Error loading YAML file", ": ${e.message}\nTBD Instruction and link to the doc", NotificationType.ERROR)
        }
    }
    class SettingPath(relativePath: String, basePath: String?) {
        var absolutePath: Path = Paths.get(basePath.toString(), relativePath)
        var absoluteDir: Path = absolutePath.parent
        constructor(relativePath: String, dir: String, basePath: String?) : this(
            relativePath,
            Paths.get(basePath.toString(), dir).toString()
        )
    }
    fun sdkPath(): SettingPath {
        return SettingPath(settings.getDbtInterpreterPath(), "")
    }
    fun dbtProjectPath(): SettingPath {
        return SettingPath(settings.getDbtProjectPath(), project.basePath)
    }
    fun logPath(): SettingPath {
        return SettingPath("dbt.log", dbtProjectConfig.targetPath, dbtProjectPath().absoluteDir.toString())
    }
    fun manifestPath(): SettingPath {
        return SettingPath("manifest.json", dbtProjectConfig.targetPath, dbtProjectPath().absoluteDir.toString())
    }
    fun semanticManifestPath(): SettingPath {
        return SettingPath("semantic_manifest.json", dbtProjectConfig.targetPath, dbtProjectPath().absoluteDir.toString())
    }
    fun runResultsPath(): SettingPath {
        return SettingPath("run_results.json", dbtProjectConfig.targetPath, project.basePath)
    }
}
