rootProject.name = "jbeancount"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

include("cli")
include("lib")
