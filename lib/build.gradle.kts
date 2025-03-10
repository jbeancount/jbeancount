plugins {
    id("java")
    id("idea")
    id("antlr")
    id("com.diffplug.spotless")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    antlr("org.antlr:antlr4:4.13.2")
    implementation("org.antlr:antlr4-runtime:4.13.2")
    api("com.graphql-java:graphql-java:18.2")
//    implementation("com.yuvalshavit:antlr-denter:1.1")
    testImplementation(platform("org.junit:junit-bom:5.12.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    shadowJar {
        minimize {
            exclude(dependency("org.antlr:antlr4-runtime:4.*"))
        }
        relocate("graphql.util", "nl.bluetainer.jbeancount.internal.s.graphql.util") {
            val transformerClasses: Set<String> = setOf(
                "Breadcrumb",
                "DefaultTraverserContext",
                "FpKit",
                "NodeAdapter",
                "NodeLocation",
                "NodeMultiZipper",
                "NodeZipper",
                "TraversalControl",
                "Traverser",
                "TraverserContext",
                "TraverserResult",
                "TraverserState",
                "TraverserVisitor",
                "TraverserVisitorStub",
                "TreeTransformer",
                "TreeTransformerUtil"
            )
            transformerClasses.forEach { include("graphql.util.${it}") }
        }
        relocate("com.yuvalshavit.antlr4", "nl.bluetainer.jbeancount.internal.s.altered.com.yuvalshavit.antlr4")
        relocate("org.antlr.v4.runtime", "nl.bluetainer.jbeancount.internal.s.org.antlr.v4.runtime")
        val excludeFiles: Set<String> = setOf(
            "LICENSE",
            "GraphqlCommon.g4",
            "GraphqlSDL.g4",
            "LICENSE.md",
            "GraphqlOperation.g4",
            "Graphql.g4",
            "META-INF/maven/"
        )
        excludeFiles.forEach { exclude("/${it}") }
//        exclude("Graphql.g4")
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}

spotless {
    antlr4 {
        antlr4Formatter()
        target("src/*/antlr/**/*.g4")
    }
}
