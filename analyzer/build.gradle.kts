
plugins {
    `java-library`
    `jacoco`
}

dependencies {
    implementation(project(":config"))
    implementation(project(":gitrawdata"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.4")
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.8.1.202007141445-r")
    testImplementation("junit:junit:4.+")
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}
tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}

jacoco {
    toolVersion = "0.8.7"
}

