package com.github.rinchinov.ijdbtplugin.extensions

import com.github.rinchinov.ijdbtplugin.artifactsServices.ManifestService
import com.github.rinchinov.ijdbtplugin.services.EventLoggerManager
import com.github.rinchinov.ijdbtplugin.services.ProjectConfigurations
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener


class ToolMainWindow : ToolWindowFactory {
    override fun shouldBeAvailable(project: Project) = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        val projectInfo = ProjectInformationPanel(toolWindow)
        val docs = DocumentationViewPanel(toolWindow)
        val queryRun = QueryRunResultsPanel(toolWindow)
        val loggingPanel = LoggingPanel()
        project.service<EventLoggerManager>().addLogger(loggingPanel)
        toolWindow.contentManager.addContent(
            contentFactory.createContent(queryRun.getContent(), "Query Run Results", false)
        )
        toolWindow.contentManager.addContent(
            contentFactory.createContent(projectInfo.getContent(), "Project Information", false)
        )
        toolWindow.contentManager.addContent(
            contentFactory.createContent(docs.getContent(), "Documentation", false)
        )
        toolWindow.contentManager.addContent(
            contentFactory.createContent(loggingPanel, "Logs", false)
        )
        toolWindow.contentManager.addContentManagerListener(
            object : ContentManagerListener {
                override fun selectionChanged(event: ContentManagerEvent) {
                    if (event.operation == ContentManagerEvent.ContentOperation.add && event.content.tabName == "Project Information") {
                        projectInfo.onManifestChanged(project.service<ManifestService>())
                        projectInfo.onProjectConfigurationsChanged(project.service<ProjectConfigurations>())
                    }
                }
            }
        )
    }
}
