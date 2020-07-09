plugins {
    `java-library`
    `maven-publish`
    signing
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withJavadocJar()
    withSourcesJar()
}

version = "0.1.0"
group = "org.chenliang.oggus"

repositories {
    jcenter()
}

dependencies {
    implementation("com.google.guava:guava:29.0-jre")

    // Use JUnit Jupiter API for testing.
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.6.0")

    // Use JUnit Jupiter Engine for testing.
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

val test by tasks.getting(Test::class) {
    // Use junit platform for unit tests
    useJUnitPlatform()
}

tasks {
    jar {
        manifest {
            attributes(
                    mapOf("Implementation-Title" to project.name, "Implementation-Version" to project.version)
            )
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            pom {
                name.set("Oggus")
                description.set("Oggus is a Java library for reading and writing Ogg and Opus stream. Opus packet structure is supported.")
                url.set("https://github.com/leonfancy/oggus")
                licenses {
                    license {
                        name.set("WTFPL")
                        url.set("https://github.com/leonfancy/oggus/blob/master/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("slimhigh")
                        name.set("Liang Chen")
                        email.set("slimhigh@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/leonfancy/oggus.git")
                    developerConnection.set("scm:git:https://github.com/leonfancy/oggus.git")
                    url.set("https://github.com/leonfancy/oggus")
                }
            }
        }
    }
    repositories {
        maven {
            url = uri("$buildDir/repos/releases")
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}
