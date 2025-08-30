package jp.kroyeeg.intellijenvfileplugin.run

import jp.kroyeeg.intellijenvfileplugin.services.EnvFileService
import com.intellij.execution.RunManagerListener
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.ModuleBasedConfiguration
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration

/**
 * Listens for run configuration changes and delegates environment variable application
 * to specialised [RunConfigurationEnvSetter] implementations.
 */
class EnvRunConfigurationListener : RunManagerListener {

    private val setterMapping: Map<Class<out RunConfiguration>, Class<out RunConfigurationEnvSetter>> = mapOf(
        ExternalSystemRunConfiguration::class.java to ExternalSystemRunConfigurationEnvSetter::class.java,
        ModuleBasedConfiguration::class.java to ModuleBasedRunConfigurationEnvSetter::class.java,
    )

    override fun runConfigurationAdded(settings: RunnerAndConfigurationSettings) {
        updateEnvironmentVariables(settings)
        super.runConfigurationAdded(settings)
    }

    override fun runConfigurationChanged(settings: RunnerAndConfigurationSettings) {
        updateEnvironmentVariables(settings)
        super.runConfigurationChanged(settings)
    }

    private fun updateEnvironmentVariables(settings: RunnerAndConfigurationSettings) {
        val env = EnvFileService.getInstance(settings.configuration.project)
            .update()
            .state.variables.associate { it.key to it.value }
        val expandedEnv = expandEnvironmentVariables(env)
        val configuration = settings.configuration
        val setterClass = setterMapping.entries
            .firstOrNull { (configClass, _) -> configClass.isInstance(configuration) }
            ?.value
        setterClass
            ?.getDeclaredConstructor()
            ?.newInstance()
            ?.setEnvironment(configuration, expandedEnv)
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
