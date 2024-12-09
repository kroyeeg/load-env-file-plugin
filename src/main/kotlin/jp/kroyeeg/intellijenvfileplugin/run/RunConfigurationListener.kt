package jp.kroyeeg.intellijenvfileplugin.run

import jp.kroyeeg.intellijenvfileplugin.services.DotEnvFilePluginService
import com.intellij.execution.RunManagerListener
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration

class RunConfigurationListener : RunManagerListener {

    override fun runConfigurationAdded(settings: RunnerAndConfigurationSettings) {
        val env = DotEnvFilePluginService.getInstance(settings.configuration.project)
            .state.variables.associate { it.key to it.value }
        val expandedEnv = expandEnvironmentVariables(env)
        settings.configuration.let { config ->
            if (config is ExternalSystemRunConfiguration) {
                val mySettings = config.settings
                mySettings.env.putAll(expandedEnv)
            }
        }
        super.runConfigurationAdded(settings)
    }

    private fun expandEnvironmentVariables(env: Map<String, String>): Map<String, String> {
        val regex = """\$\{([^}]+)}""".toRegex()

        val result = mutableMapOf<String, String>()
        env.forEach { (key, value) ->
            result[key] = regex.replace(value) { matchResult ->
                val replaceKey = matchResult.groups[1]?.value ?: return@replace matchResult.value
                env[replaceKey] ?: matchResult.value
            }
        }
        return result.toMap()
    }
}
