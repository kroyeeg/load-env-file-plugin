package jp.kroyeeg.intellijenvfileplugin.activity

import jp.kroyeeg.intellijenvfileplugin.services.DotEnvFilePluginService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import io.github.cdimascio.dotenv.Dotenv

internal class ProjectOpenedActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val dotenv = Dotenv.configure().directory(project.basePath)
            .filename(".env")
            .load()
        DotEnvFilePluginService.getInstance(project).update(
            dotenv.entries(io.github.cdimascio.dotenv.Dotenv.Filter.DECLARED_IN_ENV_FILE)
        )
    }
}
