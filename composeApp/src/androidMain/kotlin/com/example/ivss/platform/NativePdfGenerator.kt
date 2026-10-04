package com.example.ivss.platform

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.ByteArrayOutputStream

actual object NativePdfGenerator {
    actual fun generateForma1216Pdf(
        userName: String,
        userNationalId: String,
        employerName: String,
        vacationName: String,
        usedDays: Int
    ): ByteArray {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(612, 792, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paintText = Paint().apply {
            color = Color.BLACK
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val paintBold = Paint().apply {
            color = Color.BLACK
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val paintHeader = Paint().apply {
            color = Color.BLACK
            textSize = 8f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val paintTitle = Paint().apply {
            color = Color.WHITE
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val paintBox = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val paintFillBlack = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }

        val paintFillGray = Paint().apply {
            color = Color.rgb(230, 230, 230)
            style = Paint.Style.FILL
        }

        var y = 35f

        // Encabezado Institucional exacto
        canvas.drawText("MINISTERIO DEL PODER POPULAR PARA EL PROCESO SOCIAL DE TRABAJO", 35f, y, paintHeader)
        canvas.drawText("Forma: 12-16", 510f, y, paintHeader)
        y += 12f
        canvas.drawText("INSTITUTO VENEZOLANO DE LOS SEGUROS SOCIALES", 35f, y, paintHeader)
        y += 12f
        canvas.drawText("DIRECCIÓN GENERAL DE RECURSOS HUMANOS Y ADMINISTRACIÓN DE PERSONAL", 35f, y, paintText)
        y += 18f

        // Banner Título
        canvas.drawRect(35f, y, 577f, y + 22f, paintFillBlack)
        canvas.drawText("AUTORIZACIÓN DE VACACIONES", 45f, y + 15f, paintTitle)
        canvas.drawText("FECHA DE ELABORACIÓN: 24/09/2025 | Nº 025", 360f, y + 15f, paintTitle)
        y += 30f

        // PARA / DE
        canvas.drawRect(35f, y, 577f, y + 32f, paintBox)
        canvas.drawText("PARA: DIRECCIÓN GENERAL DE RECURSOS HUMANOS Y ADMINISTRACIÓN DE PERSONAL", 42f, y + 13f, paintBold)
        canvas.drawText("DE: ${employerName.uppercase()}", 42f, y + 26f, paintBold)
        y += 40f

        // DATOS DEL TRABAJADOR O TRABAJADORA
        canvas.drawRect(35f, y, 577f, y + 16f, paintFillGray)
        canvas.drawRect(35f, y, 577f, y + 16f, paintBox)
        canvas.drawText("DATOS DEL TRABAJADOR O TRABAJADORA", 190f, y + 12f, paintBold)
        y += 16f

        // Fila 1: Apellidos y Cédula
        canvas.drawRect(35f, y, 577f, y + 28f, paintBox)
        canvas.drawLine(380f, y, 380f, y + 28f, paintBox)
        canvas.drawText("APELLIDOS Y NOMBRES", 42f, y + 10f, paintText)
        canvas.drawText(userName.uppercase(), 42f, y + 22f, paintBold)
        canvas.drawText("CÉDULA DE IDENTIDAD N°", 386f, y + 10f, paintText)
        canvas.drawText(userNationalId, 386f, y + 22f, paintBold)
        y += 28f

        // Fila 2: Cargo, Nº Cargo, Fecha Ingreso, Cod Origen
        canvas.drawRect(35f, y, 577f, y + 28f, paintBox)
        canvas.drawLine(240f, y, 240f, y + 28f, paintBox)
        canvas.drawLine(310f, y, 310f, y + 28f, paintBox)
        canvas.drawLine(420f, y, 420f, y + 28f, paintBox)

        canvas.drawText("DENOMINACIÓN DEL CARGO", 42f, y + 10f, paintText)
        canvas.drawText("MEDICO ADJUNTO I", 42f, y + 22f, paintBold)

        canvas.drawText("N° CARGO", 246f, y + 10f, paintText)
        canvas.drawText("00101", 246f, y + 22f, paintBold)

        canvas.drawText("FECHA INGRESO", 316f, y + 10f, paintText)
        canvas.drawText("01/11/2019", 316f, y + 22f, paintBold)

        canvas.drawText("COD. ORIGEN Y SERV.", 426f, y + 10f, paintText)
        canvas.drawText("60209382 - 31", 426f, y + 22f, paintBold)
        y += 28f

        // Fila 3: Unidad, Lugar, Horario
        canvas.drawRect(35f, y, 577f, y + 28f, paintBox)
        canvas.drawLine(260f, y, 260f, y + 28f, paintBox)
        canvas.drawLine(420f, y, 420f, y + 28f, paintBox)

        canvas.drawText("UNIDAD O SERVICIO", 42f, y + 10f, paintText)
        canvas.drawText("CIRUGIA", 42f, y + 22f, paintBold)

        canvas.drawText("LUGAR", 266f, y + 10f, paintText)
        canvas.drawText("S.J. M", 266f, y + 22f, paintBold)

        canvas.drawText("HORARIO", 426f, y + 10f, paintText)
        canvas.drawText("ASISTENCIAL", 426f, y + 22f, paintBold)
        y += 36f

        // Texto toma de vacaciones
        canvas.drawRect(35f, y, 577f, y + 18f, paintBox)
        canvas.drawText("EL TRABAJADOR O TRABAJADORA, TOMARÁ SUS VACACIONES EN EL LAPSO SEÑALADO", 90f, y + 13f, paintBold)
        y += 18f

        // LAPSO DE DISFRUTE
        canvas.drawRect(35f, y, 577f, y + 16f, paintFillGray)
        canvas.drawRect(35f, y, 577f, y + 16f, paintBox)
        canvas.drawText("LAPSO DE DISFRUTE DE VACACIONES", 210f, y + 12f, paintBold)
        y += 16f

        canvas.drawRect(35f, y, 577f, y + 28f, paintBox)
        canvas.drawLine(170f, y, 170f, y + 28f, paintBox)
        canvas.drawLine(305f, y, 305f, y + 28f, paintBox)
        canvas.drawLine(440f, y, 440f, y + 28f, paintBox)

        canvas.drawText("DESDE", 85f, y + 10f, paintText)
        canvas.drawText("15/10/2025", 65f, y + 22f, paintBold)

        canvas.drawText("HASTA", 220f, y + 10f, paintText)
        canvas.drawText("17/11/2025", 200f, y + 22f, paintBold)

        canvas.drawText("PERIODO", 350f, y + 10f, paintText)
        canvas.drawText("2023-2024", 340f, y + 22f, paintBold)

        canvas.drawText("N° DE DÍAS", 480f, y + 10f, paintText)
        canvas.drawText("$usedDays DÍAS HÁBILES", 460f, y + 22f, paintBold)
        y += 28f

        // Reintegro
        canvas.drawRect(35f, y, 577f, y + 20f, paintBox)
        canvas.drawText("HÁBILES, DEBERÁ REINTEGRARSE EL DÍA: 18/11/2025", 140f, y + 14f, paintBold)
        y += 28f

        // OBSERVACIONES Y NOTA
        canvas.drawRect(35f, y, 577f, y + 42f, paintBox)
        canvas.drawText("OBSERVACIONES:", 42f, y + 12f, paintBold)
        canvas.drawText("NOTA: EL TRABAJADOR SOLICITÓ DICHAS VACACIONES CON EXPOSICIÓN DE MOTIVO CORRESPONDIENTES AL PERIODO 2023-2024.", 42f, y + 28f, paintText)
        y += 50f

        // FIRMAS 2x2
        canvas.drawRect(35f, y, 306f, y + 55f, paintBox)
        canvas.drawText("SUPERVISOR INMEDIATO:", 42f, y + 12f, paintBold)
        canvas.drawText("DR. WILLIAMS GONZALEZ", 80f, y + 30f, paintBold)
        canvas.drawText("NOMBRE Y APELLIDO / FIRMA Y SELLO", 70f, y + 46f, paintText)

        canvas.drawRect(306f, y, 577f, y + 55f, paintBox)
        canvas.drawText("COORDINADOR DE RECURSOS HUMANOS:", 312f, y + 12f, paintBold)
        canvas.drawText("LCDA. MAYARI SOJO", 360f, y + 30f, paintBold)
        canvas.drawText("NOMBRE Y APELLIDO / FIRMA Y SELLO", 350f, y + 46f, paintText)
        y += 55f

        canvas.drawRect(35f, y, 306f, y + 55f, paintBox)
        canvas.drawText("MÁXIMA AUTORIDAD - DIRECTOR/DIRECTORA:", 42f, y + 12f, paintBold)
        canvas.drawText("DR. JULIO AQUINO", 80f, y + 30f, paintBold)
        canvas.drawText("NOMBRE Y APELLIDO / FIRMA Y SELLO", 70f, y + 46f, paintText)

        canvas.drawRect(306f, y, 577f, y + 55f, paintBox)
        canvas.drawText("TRABAJADOR O TRABAJADORA:", 312f, y + 12f, paintBold)
        canvas.drawText(userName.uppercase(), 350f, y + 30f, paintBold)
        canvas.drawText("NOMBRE Y APELLIDO / FIRMA", 360f, y + 46f, paintText)

        pdfDocument.finishPage(page)

        val out = ByteArrayOutputStream()
        pdfDocument.writeTo(out)
        pdfDocument.close()

        return out.toByteArray()
    }
}
