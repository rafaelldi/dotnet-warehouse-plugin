package me.rafaelldi.dotnet.warehouse.local

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import kotlinx.io.files.Path
import java.nio.charset.Charset
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
internal class LocalDotnetService(private val project: Project) {
    companion object {
        internal fun getInstance(project: Project): LocalDotnetService = project.service()

        private val LOG = logger<LocalDotnetService>()

        private const val LIST_SDKS_OPTION = "--list-sdks"
        private const val LIST_RUNTIMES_OPTION = "--list-runtimes"
    }

    private val sdks = ConcurrentHashMap<String, LocalSdk>()
    private val runtimes = ConcurrentHashMap<String, LocalRuntime>()

    internal fun findLocalSdks() {
        val cmd = createDotnetCommandLine(LIST_SDKS_OPTION)
        val executionResult = ExecUtil.execAndGetOutput(cmd)

        if (executionResult.exitCode != 0 || executionResult.isTimeout) {
            LOG.info("Unable to find any dotnet sdks")
            return
        }

        for (line in executionResult.stdoutLines) {
            val spaceIndex = line.indexOf(' ')
            if (spaceIndex <= 0 || spaceIndex >= line.length) continue

            val version = line.take(spaceIndex)
            val pathString = line.substring(spaceIndex + 2, line.length - 1)
            val sdk = LocalSdk(version, Path(pathString))

            sdks[version] = sdk
        }
    }

    internal fun findLocalRuntimes() {
        val cmd = createDotnetCommandLine(LIST_RUNTIMES_OPTION)
        val executionResult = ExecUtil.execAndGetOutput(cmd)

        if (executionResult.exitCode != 0 || executionResult.isTimeout) {
            LOG.info("Unable to find any dotnet runtimes")
            return
        }

        for (line in executionResult.stdoutLines) {
            val firstSpaceIndex = line.indexOf(' ')
            val secondSpaceIndex = line.lastIndexOf(' ')
            if (firstSpaceIndex <= 0 || firstSpaceIndex >= line.length) continue
            if (secondSpaceIndex <= 0 || secondSpaceIndex >= line.length) continue

            val type = line.take(firstSpaceIndex)
            val version = line.substring(firstSpaceIndex + 1, secondSpaceIndex)
            val pathString = line.substring(secondSpaceIndex + 2, line.length - 1)
            val runtime = LocalRuntime(type, version, Path(pathString))

            runtimes[version] = runtime
        }
    }

    private fun createDotnetCommandLine(parameters: String) = GeneralCommandLine()
        .withExePath("dotnet")
        .withParameters(parameters)
        .withWorkDirectory(project.basePath)
        .withCharset(Charset.forName("UTF-8"))
        .withEnvironment("DOTNET_SKIP_FIRST_TIME_EXPERIENCE", "true")
        .withEnvironment("DOTNET_NOLOGO", "true")
}