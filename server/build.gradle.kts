plugins {
    alias(libs.plugins.kotlinxSerialization)
    kotlin("jvm")
    application
}

application {
    mainClass.set("com.example.ivss.server.ServerAppKt")
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)

    // Ktor Security & JWT
    implementation("io.ktor:ktor-server-auth:2.3.11")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.11")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("org.mindrot:jbcrypt:0.4")

    // Exposed ORM & Database SQLite
    implementation("org.jetbrains.exposed:exposed-core:0.48.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.48.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.48.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.48.0")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")

    // Apache POI para procesamiento de archivos Excel (.xlsx / .xls) y Word (.docx)
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")

    // OpenPDF para generación de documentos PDF
    implementation("com.github.librepdf:openpdf:1.3.30")

    // PDFBox para renderizado de previsualización de documentos PDF a PNG
    implementation("org.apache.pdfbox:pdfbox:3.0.3")

    testImplementation(libs.kotlin.test)
}
