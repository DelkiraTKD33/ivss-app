package com.example.ivss.server

import com.lowagie.text.Document
import com.lowagie.text.Element
import com.lowagie.text.Font
import com.lowagie.text.FontFactory
import com.lowagie.text.PageSize
import com.lowagie.text.Paragraph
import com.lowagie.text.Phrase
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import org.apache.poi.xwpf.usermodel.*
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

object DocumentGenerator {

    /**
     * Utiliza Apache POI (librería externa) para cargar el documento JENNIFER HERNANDEZ.docx,
     * reemplazar todos los campos dinámicos con los datos del usuario activo y generar el .docx actualizado.
     */
    fun generateDocxFromTemplate(
        templatePath: String,
        user: UserProfileDto,
        vacation: VacationDto,
    ): ByteArray {
        val templateFile = File(templatePath)
        val doc: XWPFDocument = if (templateFile.exists()) {
            XWPFDocument(FileInputStream(templateFile))
        } else {
            XWPFDocument()
        }

        val userName = user.fullName.uppercase()
        val nationalId = user.nationalId
        val employerName = user.employer.uppercase()

        // Reemplazar campos en párrafos
        for (paragraph in doc.paragraphs) {
            replaceTextInParagraph(paragraph, "HERNANDEZ RON JENNIFFER", userName)
            replaceTextInParagraph(paragraph, "17.062.973", nationalId)
            replaceTextInParagraph(paragraph, "HOSPITAL GENERAL MUNICIPAL IVSS SAN JUAN DE LOS MORROS", employerName)
        }

        // Reemplazar campos en las tablas del documento Word
        for (table in doc.tables) {
            for (row in table.rows) {
                for (cell in row.tableCells) {
                    for (paragraph in cell.paragraphs) {
                        replaceTextInParagraph(paragraph, "HERNANDEZ RON JENNIFFER", userName)
                        replaceTextInParagraph(paragraph, "17.062.973", nationalId)
                        replaceTextInParagraph(paragraph, "HOSPITAL GENERAL MUNICIPAL IVSS SAN JUAN DE LOS MORROS", employerName)
                    }
                }
            }
        }

        val out = ByteArrayOutputStream()
        doc.write(out)
        doc.close()
        return out.toByteArray()
    }

    private fun replaceTextInParagraph(paragraph: XWPFParagraph, target: String, replacement: String) {
        val text = paragraph.text
        if (text.contains(target)) {
            val updatedText = text.replace(target, replacement)
            while (paragraph.runs.isNotEmpty()) {
                paragraph.removeRun(0)
            }
            val newRun = paragraph.createRun()
            newRun.setText(updatedText)
            newRun.fontFamily = "Arial"
            newRun.fontSize = 9
        }
    }

    /**
     * Utiliza OpenPDF (librería externa) para generar dinámicamente el documento PDF oficial
     * con los datos dinámicos del usuario activo en el servidor Ktor.
     */
    fun generateOfficialPdf(
        user: UserProfileDto,
        vacation: VacationDto,
    ): ByteArray {
        val out = ByteArrayOutputStream()
        val document = Document(PageSize.LETTER, 20f, 20f, 20f, 20f)
        PdfWriter.getInstance(document, out)
        document.open()

        val fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f, Font.BOLD, Color.WHITE)
        val fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7f, Font.BOLD, Color.BLACK)
        val fontValue = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Font.BOLD, Color.BLACK)
        val fontLabel = FontFactory.getFont(FontFactory.HELVETICA, 6f, Font.NORMAL, Color.DARK_GRAY)

        // Encabezado
        val headerTable = PdfPTable(2)
        headerTable.widthPercentage = 100f
        headerTable.setWidths(floatArrayOf(80f, 20f))

        val cellLeft = PdfPCell(Phrase("MINISTERIO DEL PODER POPULAR PARA EL PROCESO SOCIAL DE TRABAJO\nINSTITUTO VENEZOLANO DE LOS SEGUROS SOCIALES\nDIRECCIÓN GENERAL DE RECURSOS HUMANOS Y ADMINISTRACIÓN DE PERSONAL", fontHeader))
        cellLeft.border = PdfPCell.NO_BORDER
        headerTable.addCell(cellLeft)

        val cellRight = PdfPCell(Phrase("Forma: 12-16", fontHeader))
        cellRight.border = PdfPCell.NO_BORDER
        cellRight.horizontalAlignment = Element.ALIGN_RIGHT
        headerTable.addCell(cellRight)

        document.add(headerTable)
        document.add(Paragraph(" "))

        // Título Banner
        val titleTable = PdfPTable(1)
        titleTable.widthPercentage = 100f
        val titleCell = PdfPCell(Phrase("AUTORIZACIÓN DE VACACIONES", fontTitle))
        titleCell.backgroundColor = Color.BLACK
        titleCell.horizontalAlignment = Element.ALIGN_CENTER
        titleCell.setPadding(6f)
        titleTable.addCell(titleCell)
        document.add(titleTable)

        // Tabla Datos del Trabajador
        val dataTable = PdfPTable(2)
        dataTable.widthPercentage = 100f
        dataTable.setWidths(floatArrayOf(65f, 35f))

        val cellName = PdfPCell()
        cellName.addElement(Phrase("APELLIDOS Y NOMBRES", fontLabel))
        cellName.addElement(Phrase(user.fullName.uppercase(), fontValue))
        dataTable.addCell(cellName)

        val cellId = PdfPCell()
        cellId.addElement(Phrase("CÉDULA DE IDENTIDAD Nº", fontLabel))
        cellId.addElement(Phrase(user.nationalId, fontValue))
        dataTable.addCell(cellId)

        document.add(dataTable)

        document.close()
        return out.toByteArray()
    }
}
