plugins {
    application
    id("java")
    id("org.graalvm.buildtools.native") version "0.10.5"
}

dependencies {
    implementation(project(":jbeancount-lib"))
    implementation("info.picocli:picocli:4.7.6")
    annotationProcessor("info.picocli:picocli-codegen:4.7.6")
}

tasks {
    compileJava {
        options.compilerArgs.add("-Aproject=${project.group}/${project.name}")
    }
}

application {
    mainClass.set("nl.bluetainer.jbeancount.cli.BeancountCli")
}

//nativeBuild {
//    imageName.set("jbeancount")
//}
