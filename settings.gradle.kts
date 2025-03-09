rootProject.name = "beancount-parser"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

include("jbeancount")
include("jbeancount-cli")
