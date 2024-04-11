import io.papermc.paperweight.PaperweightSourceGeneratorHelper
import io.papermc.paperweight.extension.PaperweightSourceGeneratorExt

plugins {
    java
}

plugins.apply(PaperweightSourceGeneratorHelper::class)

extensions.configure(PaperweightSourceGeneratorExt::class) {
    atFile.set(projectDir.toPath().resolve("wideners.at").toFile())
}

dependencies {
    implementation("com.squareup:javapoet:1.13.0")
    implementation(project(":paper-api"))
    implementation("io.github.classgraph:classgraph:4.8.47")
    implementation("org.jetbrains:annotations:24.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.register<JavaExec>("generate") {
    dependsOn(tasks.check)
    mainClass.set("io.papermc.generator.Main")
    classpath(sourceSets.main.map { it.runtimeClasspath })
    args(file("generated").toString(),
        project(":paper-api").sourceSets["main"].java.srcDirs.first().toString(),
        file("generatedServerTest").toString(),
        project(":paper-server").sourceSets["main"].java.srcDirs.first().toString())
}

tasks {
    test {
        useJUnitPlatform {
            if (false && System.getenv()["CI"]?.toBoolean() == true) {
                // the CI shouldn't run the test since it's not included by default but just in case this is moved to its own repo
                excludeTags("parser")
            } else {
                // excludeTags("parser") // comment this line while working on parser related things
            }
        }
        systemProperty("paper.generator.rewriter.container.api", file("generated").toString()) // todo move to the sourceset
        systemProperty("paper.generator.rewriter.container.server", file("generatedServerTest").toString()) // todo move to the sourceset
        inputs.dir("generated")
        inputs.dir("generatedServerTest")
    }

    compileTestJava {
        options.compilerArgs.add("-parameters")
    }
}

group = "io.papermc.paper"
version = "1.0-SNAPSHOT"
