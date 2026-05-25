plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "space.qclid"
version = "1.3"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("26.1.2.build.+")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(25)
    }

    runServer {
        minecraftVersion("26.1.2")
        // Suppress sun.misc.Unsafe warnings from JOML (JEP 471)
        jvmArgs("-Djoml.nounsafe", "--sun-misc-unsafe-memory-access=allow")
    }

    register("runclient") {
        dependsOn("runServer")
        group = "verification"
        description = "Starts a Paper server with the plugin installed."
    }
}

// Ensure the EULA is accepted for run-paper
// The run-paper plugin usually looks for a property or file.
// We can use a task to create the eula.txt in the run directory.
val createEula by tasks.registering {
    doLast {
        val runDir = layout.projectDirectory.dir("run")
        if (!runDir.asFile.exists()) runDir.asFile.mkdirs()
        runDir.file("eula.txt").asFile.writeText("eula=true")
    }
}

tasks.runServer {
    dependsOn(createEula)
}

tasks.withType<Jar> {
    archiveFileName.set("Dashboard-${project.version}.jar")
}
