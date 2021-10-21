/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 */

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.3.72"

    id("com.github.johnrengelman.shadow") version "4.0.4"

    java
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.5"
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()

    maven ("https://kotlin.bintray.com/kotlinx" )
}

val versionNumber = "1.1"
val artifactVersion = "$versionNumber"
val artifactDesc = "Kotlin tool to compare table data in diff style"
val githubRepo = "honza-toegel/TableTool"
val githubUrl = "https://github.com/$githubRepo.git"
val githubReadme = "README.md"

val pomUrl = "https://github.com/$githubRepo"
val pomScmUrl = pomUrl
val pomIssueUrl = "$pomUrl/issues"
val pomDesc = pomUrl

val pomLicenseName = "Apache-2.0"
val pomLicenseUrl = "https://opensource.org/licenses/apache2.0.php"
val pomLicenseDist = "repo"

val pomDeveloperId = "honza.toegel"
val pomDeveloperName = "Jan Toegel"

group = "org.jto.tabletool"
version = artifactVersion

bintray {
    user = "honza-toegel"
    key = "70e841d49cebd32f64d026f6638a3f4b9b215735"
    setPublications( "MyPublication" )
    with (pkg) {
        repo = "TableTool"
        name = "TableTool"
        setLicenses(pomLicenseName)
        vcsUrl = githubUrl
        with(version) {
            name = versionNumber
            desc = artifactDesc
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("MyPublication") {
            shadow.component(this)

            pom.withXml {
                asNode().apply {
                    appendNode("description", pomDesc)
                    appendNode("name", rootProject.name)
                    appendNode("url", pomUrl)
                    appendNode("licenses").appendNode("license").apply {
                        appendNode("name", pomLicenseName)
                        appendNode("url", pomLicenseUrl)
                        appendNode("distribution", pomLicenseDist)
                    }
                    appendNode("developers").appendNode("developer").apply {
                        appendNode("id", pomDeveloperId)
                        appendNode("name", pomDeveloperName)
                    }
                    appendNode("scm").apply {
                        appendNode("url", pomScmUrl)
                    }
                }
            }
        }
    }
}

dependencies {
    // Align versions of all Kotlin components
    implementation("com.beust:klaxon:5.5")

    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation( "org.jetbrains.kotlinx:kotlinx-cli:0.3" )

    // Tinkerpop - Core
    implementation("org.apache.tinkerpop:tinkergraph-gremlin:3.4.7")
    //Apache poi for excel files
    implementation(  "org.apache.poi:poi-ooxml:4.1.2")
    //Google Diff
    implementation ("org.bitbucket.cowwoc:diff-match-patch:1.2")

    implementation( "org.slf4j:slf4j-api:1.7.30")
    implementation( "ch.qos.logback:logback-classic:1.2.3")
    implementation( "ch.qos.logback:logback-core:1.2.3")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}


tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveBaseName.set("tableTool")
        mergeServiceFiles()
        manifest {
            attributes(
                "Implementation-Title" to "TableTool-all",
                "Implementation-Version" to archiveVersion,
                "Main-Class" to "org.jto.tabletool.MainKt"
            )
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

