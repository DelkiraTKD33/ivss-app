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

    // Librerías externas para lectura y creación de documentos Word (.docx) y PDF (.pdf)
    implementation("org.apache.poi:poi-ooxml:5.2.5")
    implementation("com.github.librepdf:openpdf:1.3.30")

    testImplementation(libs.kotlin.test)
}
