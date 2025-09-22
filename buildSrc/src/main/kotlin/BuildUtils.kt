import org.gradle.api.Project
import java.util.Properties

fun Project.loadEnv(): Map<String, String> {
    val envFile = rootProject.file(".env")
    if (!envFile.exists()) return emptyMap()

    return runCatching {
        val props = Properties().apply {
            envFile.inputStream().use { load(it) }
        }
        props.entries.associate { it.key.toString() to it.value.toString() }
    }.getOrElse { exception ->
        println("Warning: Could not load environment: ${exception.message}")
        emptyMap()
    }
}