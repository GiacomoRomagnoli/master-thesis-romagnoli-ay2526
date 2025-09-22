plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.graalvm.buildtools:native-gradle-plugin:0.11.0")
}

gradlePlugin {
    plugins {
        create("graalvm-native") {
            id = "graalvm-native"
            implementationClass = "GraalVMNativePlugin"
        }
    }
}
