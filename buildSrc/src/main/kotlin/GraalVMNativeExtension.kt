import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class GraalVMNativeExtension {
    abstract val imageName: Property<String>
    abstract val mainClass: Property<String>
    abstract val enableDebug: Property<Boolean>
    abstract val enableAgent: Property<Boolean>
    abstract val metadataDirectory: Property<String>
    abstract val additionalBuildArgs: ListProperty<String>
    abstract val javaVersion: Property<Int>
    abstract val verbose: Property<Boolean>
    abstract val toolchainDetection: Property<Boolean>

    fun buildArgs(vararg args: String) {
        additionalBuildArgs.addAll(*args)
    }

    fun buildArgs(args: List<String>) {
        additionalBuildArgs.addAll(args)
    }
}