plugins {
    id("application")
    id("java")
    id("org.graalvm.buildtools.native") version "0.10.5"
    // For creating an uber Jar, TODO Can probably remove this? Its nice for testing
    //id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    implementation(project(":lib"))
    implementation("info.picocli:picocli:4.7.6")
    annotationProcessor("info.picocli:picocli-codegen:4.7.6")
}

tasks {
    compileJava {
        options.compilerArgs.add("-Aproject=${project.group}/${project.name}")
    }
//    shadowJar {
//        archiveClassifier = ""
//    }
}

application {
    mainClass = "nl.bluetainer.jbeancount.cli.BeancountCli"
}

graalvmNative {


}

//nativeBuild {
//    imageName.set("jbeancount")
//}
