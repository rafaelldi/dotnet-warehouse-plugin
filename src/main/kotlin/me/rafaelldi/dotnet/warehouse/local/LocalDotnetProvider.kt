@file:Suppress("UnstableApiUsage")

package me.rafaelldi.dotnet.warehouse.local

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.toNioPathOrNull
import com.intellij.platform.eel.EelExecApi
import com.intellij.platform.eel.ExecuteProcessOptionsBuilder
import com.intellij.platform.eel.provider.asEelPath
import com.intellij.platform.eel.provider.getEelDescriptor
import com.intellij.platform.eel.provider.utils.awaitProcessResult
import com.intellij.platform.eel.provider.utils.stdoutString
import com.intellij.util.SystemProperties
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.file.Path
import kotlin.io.path.exists

internal interface LocalDotnetProviderApi {
    val localSdkFlow: StateFlow<List<LocalSdk>>
    suspend fun reloadLocalSdks()
}

@Service(Service.Level.PROJECT)
internal class LocalDotnetProvider(private val project: Project) : LocalDotnetProviderApi {
    companion object {
        internal fun getInstance(project: Project): LocalDotnetProvider = project.service()

        private val LOG = logger<LocalDotnetProvider>()

        private const val LIST_SDKS_OPTION = "--list-sdks"
        private const val LIST_RUNTIMES_OPTION = "--list-runtimes"
    }

    private val localSdks: MutableStateFlow<List<LocalSdk>> = MutableStateFlow(emptyList())
    override val localSdkFlow: StateFlow<List<LocalSdk>> = localSdks.asStateFlow()

    override suspend fun reloadLocalSdks() {
        val sdks = findLocalSdks()
        localSdks.emit(sdks)
    }

    private suspend fun findLocalSdks(): List<LocalSdk> {
        val execApi = project.getEelDescriptor().toEelApi().exec
        val executablePaths = getDotnetExecutablePaths()
        return buildList {
            for (executablePath in executablePaths) {
                if (!executablePath.exists()) continue
                val sdks = findLocalSdks(execApi, executablePath)
                addAll(sdks)
            }
        }
    }

    private suspend fun findLocalSdks(execApi: EelExecApi, executablePath: Path): List<LocalSdk> {
        val executionResult = executeDotnetCommand(execApi, executablePath, LIST_SDKS_OPTION)
            ?: return emptyList()

        return buildList {
            for (line in executionResult.lines()) {
                val spaceIndex = line.indexOf(' ')
                if (spaceIndex <= 0 || spaceIndex >= line.length) continue

                val version = line.take(spaceIndex)
                val pathString = line.substring(spaceIndex + 2, line.length - 1)
                val sdk = LocalSdk(version, Path.of(pathString).resolve(version))

                add(sdk)
            }
        }
    }

    private suspend fun findLocalRuntimes(): List<LocalRuntime> {
        val execApi = project.getEelDescriptor().toEelApi().exec
        val executablePaths = getDotnetExecutablePaths()
        return buildList {
            for (executablePath in executablePaths) {
                if (!executablePath.exists()) continue
                val runtimes = findLocalRuntimes(execApi, executablePath)
                addAll(runtimes)
            }
        }
    }

    private suspend fun findLocalRuntimes(execApi: EelExecApi, executablePath: Path): List<LocalRuntime> {
        val executionResult = executeDotnetCommand(execApi, executablePath, LIST_RUNTIMES_OPTION)
            ?: return emptyList()

        return buildList {
            for (line in executionResult.lines()) {
                val firstSpaceIndex = line.indexOf(' ')
                val secondSpaceIndex = line.lastIndexOf(' ')
                if (firstSpaceIndex <= 0 || firstSpaceIndex >= line.length) continue
                if (secondSpaceIndex <= 0 || secondSpaceIndex >= line.length) continue

                val type = line.take(firstSpaceIndex)
                val version = line.substring(firstSpaceIndex + 1, secondSpaceIndex)
                val pathString = line.substring(secondSpaceIndex + 2, line.length - 1)
                val runtime = LocalRuntime(type, version, Path.of(pathString))

                add(runtime)
            }
        }
    }


    // https://learn.microsoft.com/en-us/dotnet/core/install/how-to-detect-installed-versions?pivots=os-linux#check-for-install-folders
    private fun getDotnetExecutablePaths(): List<Path> {
        if (SystemInfo.isLinux) {
            return buildList {
                val userHome = SystemProperties.getUserHome()
                add(Path.of(userHome, ".dotnet/dotnet"))
                add(Path.of("/usr/lib/dotnet/dotnet"))
                add(Path.of("/usr/share/dotnet/dotnet"))
                add(Path.of("/usr/lib64/dotnet/dotnet"))
            }
        } else if (SystemInfo.isMac) {
            return listOf(
                Path.of("/usr/local/share/dotnet/dotnet")
            )
        } else {
            return listOf(
                Path.of("C:\\Program Files\\dotnet\\dotnet.exe")
            )
        }
    }

    private suspend fun executeDotnetCommand(
        execApi: EelExecApi,
        executablePath: Path,
        parameters: String
    ): String? {
        try {
            val processOptions = ExecuteProcessOptionsBuilder(executablePath.toString())
                .args(parameters)
                .workingDirectory(project.basePath?.toNioPathOrNull()?.asEelPath())
                .env(
                    mapOf(
                        "DOTNET_SKIP_FIRST_TIME_EXPERIENCE" to "true",
                        "DOTNET_NOLOGO" to "true"
                    )
                )
                .build()

            val processResult = execApi
                .spawnProcess(processOptions)
                .awaitProcessResult()

            return if (processResult.exitCode == 0) processResult.stdoutString
            else null
        } catch (e: Exception) {
            LOG.warn("Unable to execute dotnet command", e)
            return null
        }
    }
}