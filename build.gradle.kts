plugins {
    `java-library`
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "space.qclid"
// Local builds are "<base>"; CI passes -PbuildNumber=N, giving "<base>.N" (see .github/workflows/release.yml).
val baseVersion = "1.12"
version = (findProperty("buildNumber") as String?)?.let { "$baseVersion.$it" } ?: baseVersion

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

// One jar for all versions: compile against the OLDEST supported API (1.21.1, Java 21 bytecode).
// Version-renamed constants go through space.qclid.arcanum.compat.Compat.
// Tested on 1.21.1, 1.21.11, 26.1.2, 26.3. Override with -PapiVersion=... -PjavaRelease=...
// Test a server version with: ./gradlew runServer -PmcVersion=26.3
val apiVersion = (findProperty("apiVersion") as String?) ?: "1.21.1-R0.1-SNAPSHOT"
val javaRelease = ((findProperty("javaRelease") as String?) ?: "21").toInt()

dependencies {
    compileOnly("io.papermc.paper:paper-api:$apiVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(26)) }
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.release.set(javaRelease)
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("paper-plugin.yml") { expand("version" to project.version) }
}

tasks.jar {
    archiveFileName.set("Arcanum-${project.version}.jar")
}

tasks.runServer {
    minecraftVersion((findProperty("mcVersion") as String?) ?: "26.3")
    jvmArgs("-Djoml.nounsafe", "--sun-misc-unsafe-memory-access=allow", "-Dcom.mojang.eula.agree=true")
}

tasks.test {
    useJUnitPlatform()
}
