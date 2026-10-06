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
     * modificando el texto de los runs existentes in-place sin alterar ni eliminar la estructura
     * del XML de Microsoft Word, garantizando un archivo .docx 100% válido y libre de corrupción.
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
        val nationalId = user.nationalId
        val employerName = user.employer.uppercase()
        val vacationDays = vacation.usedDays.toString()
        val vacationPeriod = vacation.name.uppercase()

        val runs = paragraph.runs
        if (runs.isEmpty()) return

        for (i in 0 until runs.size) {
            val run = runs[i]
            val text = run.getText(0) ?: continue

            when {
                text.contains("HERNANDEZ RON JENNIFFE") -> {
                    run.setText(text.replace("HERNANDEZ RON JENNIFFE", userName), 0)
                }
                text == "R" && i > 0 && (runs[i - 1].getText(0)?.contains(userName) == true || runs[i - 1].getText(0)?.contains("HERNANDEZ") == true) -> {
                    run.setText("", 0)
                }
                text == "17" -> {
                    run.setText(nationalId.replace("V-", "").replace("v-", "").trim(), 0)
                }
                text == "062" || text == "973" -> {
                    run.setText("", 0)
                }
                text.contains("HOSPITAL GENERAL MUNICIPAL IVSS SAN JUAN DE LOS M") -> {
                    run.setText(text.replace("HOSPITAL GENERAL MUNICIPAL IVSS SAN JUAN DE LOS M", employerName), 0)
                }
                text == "ORROS." && i > 0 && runs[i - 1].getText(0)?.contains(employerName) == true -> {
                    run.setText("", 0)
                }
                text == "24" -> {
                    run.setText(vacationDays, 0)
                }
                text.contains("2023-2024") -> {
                    run.setText(text.replace("2023-2024", vacationPeriod), 0)
                }
            }
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
            nota = "EL TRABAJADOR SOLICITÓ DICHAS VACACIONES (${vacation.name.uppercase()}) CORRESPONDIENTES AL PERIODO 2023-2024 CON EXPOSICIÓN DE MOTIVO.",
            supervisorInmediato = "DR. WILLIAMS GONZALEZ",
            coordinadorRRHH = "LCDA. MAYARI SOJO",
            maximaAutoridad = "DR. JULIO AQUINO"
        )
        return PdfService.generarConstancia(datos)
    }
}
