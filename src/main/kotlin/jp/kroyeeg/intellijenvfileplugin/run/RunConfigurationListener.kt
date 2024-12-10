package jp.kroyeeg.intellijenvfileplugin.run

import jp.kroyeeg.intellijenvfileplugin.services.DotEnvFilePluginService
import com.intellij.execution.RunManagerListener
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration

class RunConfigurationListener : RunManagerListener {

    override fun runConfigurationAdded(settings: RunnerAndConfigurationSettings) {
        updateEnvironmentVariables(settings)
        super.runConfigurationAdded(settings)
    }

    override fun runConfigurationChanged(settings: RunnerAndConfigurationSettings) {
        updateEnvironmentVariables(settings)
        super.runConfigurationChanged(settings)
    }

    private fun updateEnvironmentVariables(settings: RunnerAndConfigurationSettings) {
        val env = DotEnvFilePluginService.getInstance(settings.configuration.project)
            .state.variables.associate { it.key to it.value }
        val expandedEnv = expandEnvironmentVariables(env)
        settings.configuration.let { config ->
            if (config is ExternalSystemRunConfiguration) {
                val mySettings = config.settings
                val newEnv = mySettings.env + expandedEnv
                mySettings.env = newEnv
            }
        }
    }

    private fun expandEnvironmentVariables(env: Map<String, String>): MutableMap<String, String> {
        val regex = """\$\{([^}]+)}""".toRegex()

        fun resolve(value: String, seenKeys: Set<String> = emptySet()): String {
            return regex.replace(value) { matchResult ->
                val replaceKey = matchResult.groups[1]?.value ?: return@replace matchResult.value
                if (replaceKey in seenKeys) {
                    matchResult.value
                } else {
                    val replacement = env[replaceKey]?.let { resolve(it, seenKeys + replaceKey) }
                    replacement ?: matchResult.value
                }
            }
        }

        val result = mutableMapOf<String, String>()
        env.forEach { (key, value) ->
            result[key] = resolve(value)
        }
        return result
    }
}
