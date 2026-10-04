package com.example.ivss.server.services

import com.example.ivss.server.model.DatosConstancia
import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import java.awt.Color
import java.io.ByteArrayOutputStream

object PdfService {

    private val FONT_MINI = FontFactory.getFont(FontFactory.HELVETICA, 6.5f, Font.NORMAL, Color.BLACK)
    private val FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, Font.BOLD, Color.BLACK)
    private val FONT_VALOR = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Font.NORMAL, Color.BLACK)
    private val FONT_VALOR_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, Font.BOLD, Color.BLACK)
    private val FONT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Font.BOLD, Color.BLACK)
    private val FONT_BANNER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f, Font.BOLD, Color.WHITE)
    private val FONT_TYPEWRITER = FontFactory.getFont(FontFactory.COURIER_BOLD, 10f, Font.BOLD, Color.BLACK)

    fun generarConstancia(datos: DatosConstancia): ByteArray {
        val out = ByteArrayOutputStream()

        Document(PageSize.LETTER, 20f, 20f, 20f, 20f).use { doc ->
            val writer = PdfWriter.getInstance(doc, out)
            doc.open()

            // 1. ENCABEZADO INSTITUCIONAL
            val headerTable = PdfPTable(2).apply {
                widthPercentage = 100f
                setWidths(floatArrayOf(82f, 18f))
            }

            val pHeader = Paragraph().apply {
                add(Chunk("MINISTERIO DEL PODER POPULAR PARA EL PROCESO SOCIAL DE TRABAJO\n", FONT_HEADER))
                add(Chunk("INSTITUTO VENEZOLANO DE LOS SEGUROS SOCIALES\n", FONT_HEADER))
                add(Chunk("DIRECCIÓN GENERAL DE RECURSOS HUMANOS Y ADMINISTRACIÓN DE PERSONAL", FONT_MINI))
            }
            val cellHeaderLeft = PdfPCell(pHeader).apply {
                border = PdfPCell.NO_BORDER
                verticalAlignment = Element.ALIGN_MIDDLE
            }
            headerTable.addCell(cellHeaderLeft)

            val cellHeaderRight = PdfPCell(Phrase("Forma: 12-16", FONT_HEADER)).apply {
                border = PdfPCell.NO_BORDER
                horizontalAlignment = Element.ALIGN_RIGHT
                verticalAlignment = Element.ALIGN_TOP
            }
            headerTable.addCell(cellHeaderRight)

            doc.add(headerTable)
            doc.add(Paragraph(" ", FONT_MINI))

            // 2. BANNER DE TÍTULO Y FECHA DE ELABORACIÓN
            val titleTable = PdfPTable(3).apply {
                widthPercentage = 100f
                setWidths(floatArrayOf(58f, 27f, 15f))
            }

            val titleCell = PdfPCell(Phrase("AUTORIZACIÓN DE VACACIONES", FONT_BANNER)).apply {
                backgroundColor = Color.BLACK
                horizontalAlignment = Element.ALIGN_CENTER
                verticalAlignment = Element.ALIGN_MIDDLE
                setPadding(6f)
            }
            titleTable.addCell(titleCell)

            val pDate = Paragraph().apply {
                alignment = Element.ALIGN_CENTER
                add(Chunk("FECHA DE ELABORACIÓN\n", FONT_MINI))
                add(Chunk("DÍA  |  MES  |  AÑO\n", FONT_MINI))
                add(Chunk("24  |   09   |  2025", FONT_VALOR_BOLD))
            }
            val dateCell = PdfPCell(pDate).apply {
                horizontalAlignment = Element.ALIGN_CENTER
                verticalAlignment = Element.ALIGN_MIDDLE
                setPadding(4f)
            }
            titleTable.addCell(dateCell)

            val pNo = Paragraph().apply {
                alignment = Element.ALIGN_CENTER
                add(Chunk("Nº:\n", FONT_TITULO))
                add(Chunk("025", FONT_VALOR_BOLD))
            }
            val noCell = PdfPCell(pNo).apply {
                horizontalAlignment = Element.ALIGN_CENTER
                verticalAlignment = Element.ALIGN_MIDDLE
                setPadding(4f)
            }
            titleTable.addCell(noCell)

            doc.add(titleTable)
            doc.add(Paragraph(" ", FONT_MINI))

            // 3. SECCIÓN PARA / DE
            val paraDeTable = PdfPTable(1).apply { widthPercentage = 100f }
            val pParaDe = Paragraph().apply {
                add(Chunk("PARA:  ${FormatoVacaciones.PARA}\n", FONT_VALOR_BOLD))
                add(Chunk("DE:  ${datos.unidadServicio.ifBlank { FormatoVacaciones.DE }}", FONT_VALOR_BOLD))
            }
            val paraDeCell = PdfPCell(pParaDe).apply {
                setPadding(6f)
            }
            paraDeTable.addCell(paraDeCell)
            doc.add(paraDeTable)

            doc.add(Paragraph(" ", FONT_MINI))

            // 4. TABLA PRINCIPAL DE DATOS DEL TRABAJADOR O TRABAJADORA
            val workerTable = PdfPTable(5).apply {
                widthPercentage = 100f
                setWidths(floatArrayOf(5f, 33f, 21f, 21f, 20f))
            }

            // Celda vertical izquierda
            val verticalCell = PdfPCell(Phrase("DATOS DEL TRABAJADOR O TRABAJADORA", FONT_MINI)).apply {
                rotation = 270
                backgroundColor = Color(230, 230, 230)
                horizontalAlignment = Element.ALIGN_CENTER
                verticalAlignment = Element.ALIGN_MIDDLE
                rowspan = 5
            }
            workerTable.addCell(verticalCell)

            // Fila 1: Apellidos y Cédula
            workerTable.addCell(celdaLabelValor(FormatoVacaciones.TITULO_APELLIDOS, datos.apellidosNombres, colSpan = 2, boldVal = true))
            workerTable.addCell(celdaLabelValor(FormatoVacaciones.TITULO_CEDULA, datos.cedula, colSpan = 2, boldVal = true))

            // Fila 2: Cargo, N°, Fecha Ingreso, Cod Origen
            workerTable.addCell(celdaLabelValor(FormatoVacaciones.TITULO_DENOMINACION, datos.denominacionCargo, colSpan = 1))
            workerTable.addCell(celdaLabelValor(FormatoVacaciones.TITULO_NUMERO_CARGO, datos.numeroCargo, colSpan = 1))
            workerTable.addCell(celdaLabelValor(FormatoVacaciones.TITULO_FECHA_INGRESO, "01 / 11 / 2019", colSpan = 1))
            workerTable.addCell(celdaLabelValor(FormatoVacaciones.TITULO_COD_ORIGEN, datos.codigoOrigenServicio, colSpan = 1))

            // Fila 3: Unidad, Lugar, Horario, Días Semana
            workerTable.addCell(celdaLabelValor(FormatoVacaciones.TITULO_UNIDAD, datos.unidadServicio, colSpan = 1))
            workerTable.addCell(celdaLabelValor(FormatoVacaciones.TITULO_LUGAR, datos.lugar, colSpan = 1))
            workerTable.addCell(celdaLabelValor(FormatoVacaciones.TITULO_HORARIO, datos.horario, colSpan = 1))
            workerTable.addCell(celdaLabelValor("DÍAS A LA SEMANA", "5", colSpan = 1))

            // Fila 4: Lapso de disfrute
            val pToma = Paragraph(FormatoVacaciones.TEXTO_TOMA_VACACIONES, FONT_TITULO).apply { alignment = Element.ALIGN_CENTER }
            val tomaCell = PdfPCell(pToma).apply {
                colspan = 2
                horizontalAlignment = Element.ALIGN_CENTER
                verticalAlignment = Element.ALIGN_MIDDLE
                setPadding(4f)
            }
            workerTable.addCell(tomaCell)

            val pLapsoHeader = Paragraph().apply {
                alignment = Element.ALIGN_CENTER
                add(Chunk("LAPSO DE DISFRUTE DE VACACIONES\n", FONT_TITULO))
                add(Chunk("DESDE: 15/10/2025   HASTA: 17/11/2025", FONT_VALOR_BOLD))
            }
            val lapsoCell = PdfPCell(pLapsoHeader).apply {
                colspan = 2
                horizontalAlignment = Element.ALIGN_CENTER
                verticalAlignment = Element.ALIGN_MIDDLE
                setPadding(4f)
            }
            workerTable.addCell(lapsoCell)

            // Fila 5: Periodo y Reintegro
            workerTable.addCell(celdaLabelValor(FormatoVacaciones.TITULO_PERIODO, datos.periodo, colSpan = 1))
            workerTable.addCell(celdaLabelValor(FormatoVacaciones.TITULO_DIAS, "${datos.numeroDias} DÍAS", colSpan = 1, boldVal = true))

            val pReintegro = Paragraph().apply {
                alignment = Element.ALIGN_CENTER
                add(Chunk("HÁBILES, DEBERÁ REINTEGRARSE EL DÍA:\n", FONT_TITULO))
                add(Chunk("18 / 11 / 2025", FONT_VALOR_BOLD))
            }
            val reintegroCell = PdfPCell(pReintegro).apply {
                colspan = 2
                horizontalAlignment = Element.ALIGN_CENTER
                verticalAlignment = Element.ALIGN_MIDDLE
                setPadding(4f)
            }
            workerTable.addCell(reintegroCell)

            doc.add(workerTable)
            doc.add(Paragraph(" ", FONT_MINI))

            // 5. OBSERVACIONES Y NOTA
            val obsTable = PdfPTable(1).apply { widthPercentage = 100f }
            val pObs = Paragraph().apply {
                add(Chunk("OBSERVACIONES:\n", FONT_VALOR_BOLD))
                add(Chunk("NOTA:\n", FONT_VALOR_BOLD))
                add(Chunk("EL TRABAJADOR SOLICITO DICHO VACACIONES CON EXPOSICION DE MOTIVO. CORRESPONDIENTES AL PERIODO ${datos.periodo}.", FONT_VALOR))
            }
            val obsCell = PdfPCell(pObs).apply { setPadding(6f) }
            obsTable.addCell(obsCell)
            doc.add(obsTable)

            doc.add(Paragraph(" ", FONT_MINI))

            // 6. CUADRO DE FIRMAS Y SELLOS 2x2
            val sigTable = PdfPTable(2).apply {
                widthPercentage = 100f
                setWidths(floatArrayOf(50f, 50f))
            }

            sigTable.addCell(celdaSignatureBox("SUPERVISOR INMEDIATO:", datos.supervisorInmediato ?: "DR. WILLIAMS GONZALEZ", "NOMBRE Y APELLIDO / FIRMA Y SELLO"))
            sigTable.addCell(celdaSignatureBox("COORDINADOR DE RECURSOS HUMANOS:", datos.coordinadorRRHH ?: "LCDA. MAYARI SOJO", "NOMBRE Y APELLIDO / FIRMA Y SELLO"))
            sigTable.addCell(celdaSignatureBox("MÁXIMA AUTORIDAD - DIRECTOR/DIRECTORA:", datos.maximaAutoridad ?: "DR. JULIO AQUINO", "NOMBRE Y APELLIDO / FIRMA Y SELLO"))
            sigTable.addCell(celdaSignatureBox("TRABAJADOR O TRABAJADORA:", datos.apellidosNombres, "NOMBRE Y APELLIDO / FIRMA", isTypewriter = true))

            doc.add(sigTable)

            doc.close()
            writer.flush()
            writer.close()
        }

        val bytes = out.toByteArray()
        out.close()

        validarPdf(bytes)
        return bytes
    }

    private fun validarPdf(bytes: ByteArray) {
        require(bytes.size > 500) { "PDF generado incompleto (${bytes.size} bytes)." }
        val header = String(bytes, 0, 5, Charsets.ISO_8859_1)
        require(header == "%PDF-") { "Cabecera PDF no válida: '$header'" }
    }

    private fun celdaLabelValor(label: String, valor: String, colSpan: Int = 1, boldVal: Boolean = false): PdfPCell {
        val p = Paragraph().apply {
            alignment = Element.ALIGN_LEFT
            add(Chunk("$label\n", FONT_MINI))
            add(Chunk(valor, if (boldVal) FONT_VALOR_BOLD else FONT_VALOR))
        }
        return PdfPCell(p).apply {
            colspan = colSpan
            verticalAlignment = Element.ALIGN_MIDDLE
            setPadding(4f)
        }
    }

    private fun celdaSignatureBox(header: String, nombre: String, footer: String, isTypewriter: Boolean = false): PdfPCell {
        val p = Paragraph().apply {
            alignment = Element.ALIGN_CENTER
            add(Chunk("$header\n\n", FONT_TITULO))
            add(Chunk("$nombre\n\n\n", if (isTypewriter) FONT_TYPEWRITER else FONT_VALOR_BOLD))
            add(Chunk(footer, FONT_MINI))
        }
        return PdfPCell(p).apply {
            horizontalAlignment = Element.ALIGN_CENTER
            verticalAlignment = Element.ALIGN_TOP
            setPadding(6f)
            minimumHeight = 65f
        }
    }
}