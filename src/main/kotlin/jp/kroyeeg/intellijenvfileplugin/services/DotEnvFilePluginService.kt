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

data class DotEnvFilePluginSettings(val variables: MutableSet<DotenvEntry> = mutableSetOf())

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
        if (File(project.basePath + "/.env").isFile) {
            val dotenv = Dotenv.configure().directory(project.basePath)
                .filename(".env")
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

