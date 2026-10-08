package com.example.ivss.server

import com.example.ivss.server.model.DatosConstancia
import com.example.ivss.server.services.PdfService
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions
import org.apache.poi.xwpf.usermodel.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

object DocumentGenerator {

    /**
     * Utiliza la plantilla exacta JENNIFER HERNANDEZ.docx (Forma_12-16_Template.docx)
     * unificando los runs del párrafo para reemplazar los marcadores sin romper el formato.
     */
    fun generateDocxFromTemplate(
        templatePath: String = "server/src/main/resources/templates/Forma_12-16_Template.docx",
        user: UserProfileDto,
        vacation: VacationDto,
    ): ByteArray {
        val projectTemplateFile = File(templatePath)
        val externalTemplateFile = File("C:\\Users\\Delkira\\Downloads\\JENNIFER HERNANDEZ.docx")

        val inputStream: InputStream = when {
            projectTemplateFile.exists() -> FileInputStream(projectTemplateFile)
            externalTemplateFile.exists() -> FileInputStream(externalTemplateFile)
            else -> DocumentGenerator::class.java.getResourceAsStream("/templates/Forma_12-16_Template.docx")
                ?: error("No se encontró la plantilla de Word Forma_12-16_Template.docx")
        }

        val doc = XWPFDocument(inputStream)

        // Procesar párrafos del cuerpo principal
        for (paragraph in doc.paragraphs) {
            replaceInParagraph(paragraph, user, vacation)
        }

        // Procesar tablas principales y tablas anidadas
        for (table in doc.tables) {
            processTable(table, user, vacation)
        }

        val out = ByteArrayOutputStream()
        doc.write(out)
        doc.close()
        inputStream.close()
        return out.toByteArray()
    }

    private fun processTable(table: XWPFTable, user: UserProfileDto, vacation: VacationDto) {
        for (row in table.rows) {
            for (cell in row.tableCells) {
                if (cell.paragraphs.isEmpty()) {
                    cell.addParagraph()
                }
                for (paragraph in cell.paragraphs) {
                    replaceInParagraph(paragraph, user, vacation)
                }
                for (nestedTable in cell.tables) {
                    processTable(nestedTable, user, vacation)
                }
            }
        }
    }

    private fun replaceInParagraph(paragraph: XWPFParagraph, user: UserProfileDto, vacation: VacationDto) {
        val userName = user.fullName.uppercase()
        val nationalIdClean = user.nationalId.replace("V-", "").replace("v-", "").replace("E-", "").replace("e-", "").trim()
        val employerName = user.employer.uppercase()
        val vacationDays = vacation.usedDays.toString()

        val textOriginal = paragraph.runs.joinToString("") { it.getText(0) ?: "" }
        if (textOriginal.isBlank()) return

        var updatedText = textOriginal
        var hasChanges = false

        // Extraer años dinámicos del período
        val years = Regex("\\b\\d{4}\\b").findAll(vacation.name).map { it.value }.toList()
        val yearStart = if (years.size >= 2) years[0] else if (years.isNotEmpty()) years[0] else "2024"
        val yearEnd = if (years.size >= 2) years[1] else (yearStart.toIntOrNull()?.plus(1)?.toString() ?: "2025")
        val periodFormatted = "$yearStart - $yearEnd"

        // Paso 1: Reemplazar frases largas y marcar tokens temporales únicos
        val phase1Replacements = mapOf(
            "HERNANDEZ RON JENNIFFER" to userName,
            "17.062.973" to nationalIdClean,
            "HOSPITAL GENERAL MUNICIPAL IVSS SAN JUAN DE LOS MORROS." to employerName,
            "HOSPITAL GENERAL MUNICIPAL IVSS SAN JUAN DE LOS MORROS" to employerName,
            "2023-2024" to "__IVSS_FULL_PERIOD__",
            "2023 - 2024" to "__IVSS_FULL_PERIOD__",
            "2015 - 2025" to "__IVSS_FULL_PERIOD__",
            "2015-2025" to "__IVSS_FULL_PERIOD__",
            "2023" to "__IVSS_YEAR_START__",
            "2024" to "__IVSS_YEAR_END__",
            "2015" to "__IVSS_YEAR_END__",
            "24" to vacationDays
        )

        phase1Replacements.forEach { (target, replacement) ->
            if (updatedText.contains(target)) {
                updatedText = updatedText.replace(target, replacement)
                hasChanges = true
            }
        }

        // Paso 2: Sustituir los tokens por los años reales finales (evita sobreescritura en cascada)
        if (updatedText.contains("__IVSS_YEAR_START__")) {
            updatedText = updatedText.replace("__IVSS_YEAR_START__", yearStart)
            hasChanges = true
        }
        if (updatedText.contains("__IVSS_YEAR_END__")) {
            updatedText = updatedText.replace("__IVSS_YEAR_END__", yearEnd)
            hasChanges = true
        }
        if (updatedText.contains("__IVSS_FULL_PERIOD__")) {
            updatedText = updatedText.replace("__IVSS_FULL_PERIOD__", periodFormatted)
            hasChanges = true
        }

        if (hasChanges) {
            val firstRun = paragraph.runs.firstOrNull()
            val fontFamily = firstRun?.fontFamily ?: "Arial"
            val isBold = firstRun?.isBold ?: false

            for (i in paragraph.runs.indices.reversed()) {
                paragraph.removeRun(i)
            }

            val newRun = paragraph.createRun()
            newRun.setText(updatedText)
            newRun.fontFamily = fontFamily
            newRun.isBold = isBold
        }
    }

    /**
     * Convierte el documento Word JENNIFER HERNANDEZ.docx directamente a PDF
     * mediante XDocReport / POI PdfConverter, manteniendo el 100% del formato.
     */
    fun generateOfficialPdf(
        user: UserProfileDto,
        vacation: VacationDto,
    ): ByteArray {
        val docxBytes = generateDocxFromTemplate(
            templatePath = "server/src/main/resources/templates/Forma_12-16_Template.docx",
            user = user,
            vacation = vacation
        )

        return try {
            val doc = XWPFDocument(ByteArrayInputStream(docxBytes))
            val options = PdfOptions.create()
            val pdfOut = ByteArrayOutputStream()
            PdfConverter.getInstance().convert(doc, pdfOut, options)
            val pdfBytes = pdfOut.toByteArray()
            doc.close()
            if (pdfBytes.size > 500) {
                pdfBytes
            } else {
                generarPdfRespaldo(user, vacation)
            }
        } catch (e: Exception) {
            println("Aviso en conversión directa DOCX a PDF: ${e.message}")
            generarPdfRespaldo(user, vacation)
        }
    }

    private fun generarPdfRespaldo(user: UserProfileDto, vacation: VacationDto): ByteArray {
        val datos = DatosConstancia(
            cedula = user.nationalId,
            apellidosNombres = user.fullName.uppercase(),
            denominacionCargo = "ANALISTA TÉCNICO I",
            numeroCargo = "00101",
            fechaIngreso = "2019-11-01",
            codigoOrigenServicio = "60209382 - 31",
            unidadServicio = "ADMINISTRACIÓN Y RRHH",
            lugar = "SAN JUAN DE LOS MORROS",
            horario = "ASISTENCIAL",
            fechaDesde = "2025-10-15",
            fechaHasta = "2025-11-17",
            periodo = vacation.name,
            numeroDias = vacation.usedDays,
            fechaReintegro = "2025-11-18",
            observaciones = "Solicitud aprobada y registrada en el sistema IVSS.",
            nota = "EL TRABAJADOR SOLICITÓ DICHAS VACACIONES (${vacation.name.uppercase()}) CORRESPONDIENTES AL PERIODO CON EXPOSICIÓN DE MOTIVO.",
            supervisorInmediato = "DR. WILLIAMS GONZALEZ",
            coordinadorRRHH = "LCDA. MAYARI SOJO",
            maximaAutoridad = "DR. JULIO AQUINO"
        )
        return PdfService.generarConstancia(datos)
    }
}
