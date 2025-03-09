import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin

plugins {
    id("com.diffplug.spotless") version "7.0.2" apply false
    id("com.github.ben-manes.versions") version "0.52.0"
}

group = "nl.jrdie.beancount.parser"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply<JavaPlugin>()
    apply<SpotlessPlugin>()
    dependencies {
        val compileOnly by configurations
        compileOnly("org.jetbrains:annotations:26.0.2")
    }
    configure<JavaPluginExtension>() {
        toolchain {
            languageVersion = JavaLanguageVersion.of(23)
            vendor = JvmVendorSpec.GRAAL_VM
        }
    }
    configure<SpotlessExtension>() {
        java {
            googleJavaFormat()
            targetExclude("**/build/generated-src/**")
        }
    }
}
