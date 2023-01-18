import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar
import java.nio.file.Paths
import java.nio.file.Files

val iaasPropertyName = "iaas"
val keystorePasswordPropertyName = "keystorePassword"
val defaultKeystorePassword = "fakepassword"
val initialMigrationFilename = "V1__Create_users.sql"

fun isDatabaseEngine(engine: String): Boolean {
    return Files.mismatch(
        Paths.get("src", "main", "resources", "db", engine, initialMigrationFilename),
        Paths.get("src", "main", "resources", "db", "migration", initialMigrationFilename)
    ) < 0
}


val isPostgres: Boolean = isDatabaseEngine("postgres")
val isMysql: Boolean = isDatabaseEngine("mysql")
val jarBasename: String = "${project.name}-${if (isMysql) "mysql" else "postgres"}"

// Manifest generation options
val isGCP: Boolean = isIaasPlatform("GCP")
val keystorePasswordValue: String = (getProjectProperty(keystorePasswordPropertyName) ?: defaultKeystorePassword)
val keystorePassword: String = if (isGCP) "JAVA_KEYSTORE_PASSWORD: '${keystorePasswordValue}'" else ""
val extraJavaOptions: String =
    if (isGCP) "-Djavax.net.ssl.keyStore=/app/META-INF/keystore.jks -Djavax.net.ssl.keyStorePassword=$keystorePasswordValue" else ""

fun isIaasPlatform(name: String): Boolean {
    val iaas = getProjectProperty(iaasPropertyName)
    return iaas != null && iaas.equals(name, true)
}

fun getProjectProperty(name: String): String? {
    return if (project.hasProperty(name)) project.properties[name] as? String else null
}



plugins {
    id("org.springframework.boot") version "3.0.1"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
    kotlin("plugin.jpa") version "1.7.22"
}

group = "org.cloudfoundry"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("io.pivotal.cfenv:java-cfenv-boot:2.4.1")

    if (isMysql) {
        implementation("org.flywaydb:flyway-mysql")
    }
    if (isPostgres) {
        runtimeOnly("org.postgresql:postgresql:42.5.1")
    }


    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<BootJar>("bootJar") {
    from(".profile") {
        into("")
    }
    archiveBaseName.set(jarBasename)
}

tasks.register<Copy>("configureForPostgres") {
    group = "build"
    description = "Configure artefacts specific to PostgreSQL"
    from("src/main/resources/db/postgres")
    into("src/main/resources/db/migration")
}

tasks.register<Copy>("configureForMysql") {
    group = "build"
    description = "Configure artefacts specific to MySQL"
    from("src/main/resources/db/mysql")
    into("src/main/resources/db/migration")
}

tasks.register<Copy>("deploymentManifest") {
    group = "Cloud Foundry"
    description = "Generate the deployment manifest"

    mustRunAfter(
        tasks.named("bootJar"),
        tasks.named("jar"),
        tasks.named("inspectClassesForKotlinIC")
    )

    from("manifest-template.yml")
    into("build")
    rename("manifest-template.yml", "manifest.yml")

    expand(
        Pair("version", version),
        Pair("basename", jarBasename),
        Pair("extraJavaOptions", extraJavaOptions),
        Pair("keystorePassword", keystorePassword),
    )
}

tasks.register<Exec>("deploy") {
    group = "Cloud Foundry"
    description = "Deploy or re-deploy the application"

    dependsOn(tasks.named("bootJar"))
    mustRunAfter(tasks.named("bootJar"))

    dependsOn(tasks.named("deploymentManifest"))
    mustRunAfter(tasks.named("deploymentManifest"))

    commandLine = arrayListOf("cf", "push", "--manifest", "build/manifest.yml")
}

tasks.register<Exec>("initialDeploy") {
    description = "Deploy the application without starting"
    group = "Cloud Foundry"

    dependsOn(tasks.named("bootJar"))
    mustRunAfter(tasks.named("bootJar"))

    dependsOn(tasks.named("deploymentManifest"))
    mustRunAfter(tasks.named("deploymentManifest"))

    commandLine = arrayListOf("cf", "push", "--no-start", "--manifest", "build/manifest.yml")
}