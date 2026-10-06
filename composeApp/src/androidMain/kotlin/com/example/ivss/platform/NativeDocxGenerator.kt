package com.example.ivss.platform

import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.ByteArrayOutputStream

actual object NativeDocxGenerator {
    actual fun generateForma1216Docx(
        userName: String,
        userNationalId: String,
        employerName: String,
        vacationName: String,
        usedDays: Int
    ): ByteArray {
        val doc = XWPFDocument()

        val p1 = doc.createParagraph()
        val r1 = p1.createRun()
        r1.setText("INSTITUTO VENEZOLANO DE LOS SEGUROS SOCIALES")
        r1.isBold = true
        r1.fontSize = 11

        val p2 = doc.createParagraph()
        val r2 = p2.createRun()
        r2.setText("FORMA 12-16: AUTORIZACIÓN DE VACACIONES")
        r2.isBold = true
        r2.fontSize = 12

        val table = doc.createTable(4, 2)
        table.setWidth("100%")

        table.getRow(0).getCell(0).setText("APELLIDOS Y NOMBRES:")
        table.getRow(0).getCell(1).setText(userName)

        table.getRow(1).getCell(0).setText("CÉDULA DE IDENTIDAD:")
        table.getRow(1).getCell(1).setText(userNationalId)

        table.getRow(2).getCell(0).setText("PATRONO / INSTITUCIÓN:")
        table.getRow(2).getCell(1).setText(employerName)

        table.getRow(3).getCell(0).setText("LAPSO SOLICITADO:")
        table.getRow(3).getCell(1).setText("$vacationName ($usedDays DÍAS HÁBILES)")

        val out = ByteArrayOutputStream()
        doc.write(out)
        doc.close()
        return out.toByteArray()
    }
}
