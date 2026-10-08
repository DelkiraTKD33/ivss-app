package com.example.ivss.platform

import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFTable
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

actual object NativeDocxGenerator {
    actual fun generateForma1216Docx(
        userName: String,
        userNationalId: String,
        employerName: String,
        vacationName: String,
        usedDays: Int
    ): ByteArray {
        val context = AppContextHolder.context
        val externalFile = File("C:\\Users\\Delkira\\Downloads\\JENNIFER HERNANDEZ.docx")

        val inputStream: InputStream = when {
            context != null -> {
                try {
                    context.assets.open("vacation_template.docx")
                } catch (e: Exception) {
                    if (externalFile.exists()) FileInputStream(externalFile) else error("No se encontró la plantilla de Word")
                }
            }
            externalFile.exists() -> FileInputStream(externalFile)
            else -> error("No se encontró la plantilla de Word Forma 12-16")
        }

        val doc = XWPFDocument(inputStream)

        val cleanCedula = userNationalId.replace("V-", "").replace("v-", "").replace("E-", "").replace("e-", "").trim()

        val years = Regex("\\b\\d{4}\\b").findAll(vacationName).map { it.value }.toList()
        val yearStart = if (years.size >= 2) years[0] else if (years.isNotEmpty()) years[0] else "2024"
        val yearEnd = if (years.size >= 2) years[1] else (yearStart.toIntOrNull()?.plus(1)?.toString() ?: "2025")
        val periodFormatted = "$yearStart - $yearEnd"

        val phase1Replacements = mapOf(
            "HERNANDEZ RON JENNIFFER" to userName.uppercase(),
            "17.062.973" to cleanCedula,
            "HOSPITAL GENERAL MUNICIPAL IVSS SAN JUAN DE LOS MORROS." to employerName.uppercase(),
            "HOSPITAL GENERAL MUNICIPAL IVSS SAN JUAN DE LOS MORROS" to employerName.uppercase(),
            "2023-2024" to "__IVSS_FULL_PERIOD__",
            "2023 - 2024" to "__IVSS_FULL_PERIOD__",
            "2015 - 2025" to "__IVSS_FULL_PERIOD__",
            "2015-2025" to "__IVSS_FULL_PERIOD__",
            "2023" to "__IVSS_YEAR_START__",
            "2024" to "__IVSS_YEAR_END__",
            "2015" to "__IVSS_YEAR_END__",
            "24" to usedDays.toString()
        )

        // Procesar párrafos del cuerpo principal
        for (paragraph in doc.paragraphs) {
            replaceInParagraph(paragraph, phase1Replacements, yearStart, yearEnd, periodFormatted)
        }

        // Procesar tablas principales y tablas anidadas
        for (table in doc.tables) {
            processTable(table, phase1Replacements, yearStart, yearEnd, periodFormatted)
        }

        val out = ByteArrayOutputStream()
        doc.write(out)
        doc.close()
        inputStream.close()
        return out.toByteArray()
    }

    private fun processTable(
        table: XWPFTable,
        replacements: Map<String, String>,
        yearStart: String,
        yearEnd: String,
        periodFormatted: String
    ) {
        for (row in table.rows) {
            for (cell in row.tableCells) {
                if (cell.paragraphs.isEmpty()) {
                    cell.addParagraph()
                }
                for (paragraph in cell.paragraphs) {
                    replaceInParagraph(paragraph, replacements, yearStart, yearEnd, periodFormatted)
                }
                for (nestedTable in cell.tables) {
                    processTable(nestedTable, replacements, yearStart, yearEnd, periodFormatted)
                }
            }
        }
    }

    private fun replaceInParagraph(
        paragraph: XWPFParagraph,
        replacements: Map<String, String>,
        yearStart: String,
        yearEnd: String,
        periodFormatted: String
    ) {
        val textOriginal = paragraph.runs.joinToString("") { it.getText(0) ?: "" }
        if (textOriginal.isBlank()) return

        var updatedText = textOriginal
        var hasChanges = false

        replacements.forEach { (target, replacement) ->
            if (updatedText.contains(target)) {
                updatedText = updatedText.replace(target, replacement)
                hasChanges = true
            }
        }

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
}
