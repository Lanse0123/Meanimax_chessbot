plugins {
    id("java")
}

group = "lanse.meanimax"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // reflection stuff
    implementation("org.reflections:reflections:0.10.2")

    // Json for storage and stuff
    implementation("org.json:json:20240303")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("dev.dirs:directories:26")

    // for move history (not needed but like cool i guess)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.2")
}

tasks.test {
    useJUnitPlatform()
}