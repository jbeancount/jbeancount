plugins {
    id("java")
    id("antlr")
    id("com.diffplug.spotless")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    antlr("org.antlr:antlr4:4.13.2")
    implementation("org.antlr:antlr4-runtime:4.13.2")
    api("com.graphql-java:graphql-java:18.2")
//    implementation("com.yuvalshavit:antlr-denter:1.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.12.0")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.12.0")
}

tasks {
    shadowJar {
        minimize {
            exclude(dependency("org.antlr:antlr4-runtime:4.*"))
        }
        relocate("graphql.util", "nl.jrdie.beancount.internal.s.graphql.util") {
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
        relocate("com.yuvalshavit.antlr4", "nl.jrdie.beancount.internal.s.altered.com.yuvalshavit.antlr4")
        relocate("org.antlr.v4.runtime", "nl.jrdie.beancount.internal.s.org.antlr.v4.runtime")
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

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

spotless {
    antlr4 {
        antlr4Formatter()
        target("src/*/antlr/**/*.g4")
    }
}
