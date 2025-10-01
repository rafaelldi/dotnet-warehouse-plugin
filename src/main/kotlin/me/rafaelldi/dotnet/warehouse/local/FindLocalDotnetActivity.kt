package me.rafaelldi.dotnet.warehouse.local

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

internal class FindLocalDotnetActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        LocalDotnetService.getInstance(project).findLocalSdks()
        LocalDotnetService.getInstance(project).findLocalRuntimes()
    }
}