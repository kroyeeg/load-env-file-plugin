package jp.kroyeeg.intellijenvfileplugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.DotenvEntry
import java.io.File

data class DotEnvFilePluginSettings(
    var envPath: String? = null,
    val variables: MutableSet<DotenvEntry> = mutableSetOf()
)

@State(
    name = "MyPluginSettings",
    storages = [Storage("MyPluginSettings.xml")]
)
@Service(Service.Level.PROJECT)
class DotEnvFilePluginService(private val project: Project) : PersistentStateComponent<DotEnvFilePluginSettings> {

    private var settings: DotEnvFilePluginSettings = DotEnvFilePluginSettings()

    override fun getState(): DotEnvFilePluginSettings {
        return settings
    }

    override fun loadState(state: DotEnvFilePluginSettings) {
        XmlSerializerUtil.copyBean(state, this.settings)
    }

    fun update(): DotEnvFilePluginService {
        // Clear previous variables to avoid accumulation across updates
        settings.variables.clear()

        val basePath = project.basePath
        val configuredPath = settings.envPath?.trim()?.takeIf { it.isNotEmpty() }
        val candidateFile: File? = when {
            configuredPath != null -> {
                val f = File(configuredPath)
                if (f.isDirectory) File(f, ".env") else f
            }
            basePath != null -> File(basePath, ".env")
            else -> null
        }

        if (candidateFile != null && candidateFile.isFile) {
            val dotenv = Dotenv.configure()
                .directory(candidateFile.parent)
                .filename(candidateFile.name)
                .load()
            settings.variables.addAll(dotenv.entries(Dotenv.Filter.DECLARED_IN_ENV_FILE))
        }
        return this

    }

    companion object {
        fun getInstance(project: Project): DotEnvFilePluginService {
            return project.getService(DotEnvFilePluginService::class.java)
        }
    }

}

