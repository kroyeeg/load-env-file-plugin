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

//        val result = mutableMapOf<String, String>()
//        env.forEach { (key, value) ->
//            result[key] = regex.replace(value) { matchResult ->
//                val replaceKey = matchResult.groups[1]?.value ?: return@replace matchResult.value
//                env[replaceKey] ?: matchResult.value
//            }
//        }
//        return result.toMap()
        val result = mutableMapOf<String, String>()
        env.forEach { (key, value) ->
            result[key] = resolve(value)
        }
        return result.toMap()
    }
}
