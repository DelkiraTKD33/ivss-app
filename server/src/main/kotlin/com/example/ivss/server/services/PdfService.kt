package com.example.ivss.server.services

import com.example.ivss.server.model.DatosConstancia
import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object PdfService {

    private val FECHA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val FECHA_DOC_FMT = DateTimeFormatter.ofPattern("ddMMyyyy")

    private val FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f)
    private val FONT_VALOR = FontFactory.getFont(FontFactory.HELVETICA, 9f)
    private val FONT_VALOR_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f)
    private val FONT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f)
    private val FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 9f)

    fun generarConstancia(datos: DatosConstancia): ByteArray {
        val out = ByteArrayOutputStream()
        val doc = Document(PageSize.LETTER, 40f, 40f, 40f, 40f)
        PdfWriter.getInstance(doc, out)
        doc.open()

        doc.add(Paragraph("PARA: ${FormatoVacaciones.PARA}", FONT_VALOR_BOLD))
        doc.add(Paragraph("DE: ${FormatoVacaciones.DE}", FONT_VALOR_BOLD))
        doc.add(Paragraph(" "))

        val tabla1 = PdfPTable(floatArrayOf(3f, 2f)).apply {
            widthPercentage = 100f
        }
        tabla1.addCell(celdaTitulo(FormatoVacaciones.TITULO_APELLIDOS))
        tabla1.addCell(celdaTitulo(FormatoVacaciones.TITULO_CEDULA))
        tabla1.addCell(celdaValor(datos.apellidosNombres, bold = true))
        tabla1.addCell(celdaValor(datos.cedula, bold = true))
        doc.add(tabla1)

        val tabla2 = PdfPTable(floatArrayOf(3f, 1.2f, 1.8f, 2f)).apply {
            widthPercentage = 100f
        }
        tabla2.addCell(celdaTitulo(FormatoVacaciones.TITULO_DENOMINACION))
        tabla2.addCell(celdaTitulo(FormatoVacaciones.TITULO_NUMERO_CARGO))
        tabla2.addCell(celdaTitulo(FormatoVacaciones.TITULO_FECHA_INGRESO))
        tabla2.addCell(celdaTitulo(FormatoVacaciones.TITULO_COD_ORIGEN))

        tabla2.addCell(celdaValor(datos.denominacionCargo))
        tabla2.addCell(celdaValor(datos.numeroCargo))
        tabla2.addCell(celdaValor(formatearFecha(datos.fechaIngreso)))
        tabla2.addCell(celdaValor(datos.codigoOrigenServicio))
        doc.add(tabla2)

        val tabla3 = PdfPTable(floatArrayOf(3f, 2f, 2f)).apply {
            widthPercentage = 100f
        }
        tabla3.addCell(celdaTitulo(FormatoVacaciones.TITULO_UNIDAD))
        tabla3.addCell(celdaTitulo(FormatoVacaciones.TITULO_LUGAR))
        tabla3.addCell(celdaTitulo(FormatoVacaciones.TITULO_HORARIO))

        tabla3.addCell(celdaValor(datos.unidadServicio))
        tabla3.addCell(celdaValor(datos.lugar))
        tabla3.addCell(celdaValor(datos.horario))
        doc.add(tabla3)

        doc.add(Paragraph(" "))

        val pToma = Paragraph(FormatoVacaciones.TEXTO_TOMA_VACACIONES, FONT_VALOR_BOLD)
        pToma.alignment = Element.ALIGN_CENTER
        val tablaTexto = PdfPTable(1).apply { widthPercentage = 100f }
        tablaTexto.addCell(celdaTexto(pToma))
        doc.add(tablaTexto)

        val tablaLapso = PdfPTable(1).apply { widthPercentage = 100f }
        tablaLapso.addCell(celdaTituloCentrado(FormatoVacaciones.TITULO_LAPSO))
        doc.add(tablaLapso)

        val tablaFechas = PdfPTable(floatArrayOf(1f, 1f, 1f, 1f)).apply { widthPercentage = 100f }
        tablaFechas.addCell(celdaTitulo(FormatoVacaciones.TITULO_DESDE))
        tablaFechas.addCell(celdaTitulo(FormatoVacaciones.TITULO_HASTA))
        tablaFechas.addCell(celdaTitulo(FormatoVacaciones.TITULO_PERIODO))
        tablaFechas.addCell(celdaTitulo(FormatoVacaciones.TITULO_DIAS))

        tablaFechas.addCell(celdaValorCentrado(
            "${formatearFecha(datos.fechaDesde)} ${formatearFechaSinBarras(datos.fechaDesde)}"
        ))
        tablaFechas.addCell(celdaValorCentrado(
            "${formatearFecha(datos.fechaHasta)} ${formatearFechaSinBarras(datos.fechaHasta)}"
        ))
        tablaFechas.addCell(celdaValorCentrado(datos.periodo))
        tablaFechas.addCell(celdaValorCentrado(datos.numeroDias.toString()))
        doc.add(tablaFechas)

        val tablaReintegro = PdfPTable(floatArrayOf(2f, 2f)).apply { widthPercentage = 100f }
        tablaReintegro.addCell(celdaTexto(Paragraph(FormatoVacaciones.TEXTO_REINTEGRO, FONT_VALOR_BOLD)))
        tablaReintegro.addCell(celdaValorCentradoBold(
            "${formatearFecha(datos.fechaReintegro)} ${formatearFechaSinBarras(datos.fechaReintegro)}"
        ))
        doc.add(tablaReintegro)

        doc.add(Paragraph(" "))
        doc.add(Paragraph(" "))

        doc.add(Paragraph(FormatoVacaciones.TITULO_OBSERVACIONES, FONT_VALOR_BOLD))
        if (!datos.observaciones.isNullOrBlank()) {
            doc.add(Paragraph(datos.observaciones, FONT_NORMAL))
        }
        doc.add(Paragraph(" "))

        doc.add(Paragraph(FormatoVacaciones.TITULO_NOTA, FONT_VALOR_BOLD))
        val notaTexto = datos.nota ?: 
            "EL TRABAJADOR SOLICITÓ DICHAS VACACIONES CON EXPOSICIÓN DE MOTIVO CORRESPONDIENTES AL PERIODO ${datos.periodo}."
        doc.add(Paragraph(notaTexto, FONT_NORMAL))

        doc.add(Paragraph(" "))
        doc.add(Paragraph(" "))

        val tablaFirmas = PdfPTable(floatArrayOf(1f, 1f)).apply { widthPercentage = 100f }

        tablaFirmas.addCell(celdaFirma(
            titulo = FormatoVacaciones.FIRMA_SUPERVISOR,
            nombre = datos.supervisorInmediato ?: "DR. WILLIAMS GONZALEZ",
            labelNombre = FormatoVacaciones.LABEL_NOMBRE_APELLIDO,
            labelFirma = FormatoVacaciones.LABEL_FIRMA_SELLO
        ))
        tablaFirmas.addCell(celdaFirma(
            titulo = FormatoVacaciones.FIRMA_COORDINADOR,
            nombre = datos.coordinadorRRHH ?: "LCDA. MAYARI SOJO",
            labelNombre = FormatoVacaciones.LABEL_NOMBRE_APELLIDO,
            labelFirma = FormatoVacaciones.LABEL_FIRMA_SELLO
        ))
        tablaFirmas.addCell(celdaFirma(
            titulo = FormatoVacaciones.FIRMA_MAXIMA,
            nombre = datos.maximaAutoridad ?: "DR. JULIO AQUINO",
            labelNombre = FormatoVacaciones.LABEL_NOMBRE_APELLIDO,
            labelFirma = FormatoVacaciones.LABEL_FIRMA_SELLO
        ))
        tablaFirmas.addCell(celdaFirma(
            titulo = FormatoVacaciones.FIRMA_TRABAJADOR,
            nombre = datos.apellidosNombres,
            labelNombre = FormatoVacaciones.LABEL_NOMBRE_APELLIDO,
            labelFirma = FormatoVacaciones.LABEL_FIRMA
        ))
        doc.add(tablaFirmas)

        doc.close()
        return out.toByteArray()
    }

    private fun celdaTitulo(texto: String): PdfPCell {
        val p = Paragraph(texto, FONT_TITULO)
        p.alignment = Element.ALIGN_CENTER
        return PdfPCell(p).apply {
            horizontalAlignment = Element.ALIGN_CENTER
            verticalAlignment = Element.ALIGN_MIDDLE
            backgroundColor = java.awt.Color(230, 230, 230)
            setPadding(4f)
        }
    }

    private fun celdaTituloCentrado(texto: String): PdfPCell {
        val p = Paragraph(texto, FONT_HEADER)
        p.alignment = Element.ALIGN_CENTER
        return PdfPCell(p).apply {
            horizontalAlignment = Element.ALIGN_CENTER
            verticalAlignment = Element.ALIGN_MIDDLE
            backgroundColor = java.awt.Color(230, 230, 230)
            setPadding(6f)
        }
    }

    private fun celdaValor(texto: String, bold: Boolean = false): PdfPCell {
        val p = Paragraph(texto, if (bold) FONT_VALOR_BOLD else FONT_VALOR)
        p.alignment = Element.ALIGN_CENTER
        return PdfPCell(p).apply {
            horizontalAlignment = Element.ALIGN_CENTER
            verticalAlignment = Element.ALIGN_MIDDLE
            setPadding(4f)
        }
    }

    private fun celdaValorCentrado(texto: String): PdfPCell {
        val p = Paragraph(texto, FONT_VALOR)
        p.alignment = Element.ALIGN_CENTER
        return PdfPCell(p).apply {
            horizontalAlignment = Element.ALIGN_CENTER
            verticalAlignment = Element.ALIGN_MIDDLE
            setPadding(4f)
        }
    }

    private fun celdaValorCentradoBold(texto: String): PdfPCell {
        val p = Paragraph(texto, FONT_VALOR_BOLD)
        p.alignment = Element.ALIGN_CENTER
        return PdfPCell(p).apply {
            horizontalAlignment = Element.ALIGN_CENTER
            verticalAlignment = Element.ALIGN_MIDDLE
            setPadding(4f)
        }
    }

    private fun celdaTexto(p: Paragraph): PdfPCell {
        return PdfPCell(p).apply {
            horizontalAlignment = Element.ALIGN_CENTER
            verticalAlignment = Element.ALIGN_MIDDLE
            setPadding(6f)
        }
    }

    private fun celdaFirma(
        titulo: String,
        nombre: String?,
        labelNombre: String,
        labelFirma: String
    ): PdfPCell {
        val sb = StringBuilder()
        sb.append(titulo).append("\n\n")
        sb.append(nombre ?: "").append("\n")
        sb.append(labelNombre).append("\n\n\n")
        sb.append(labelFirma)

        val p = Paragraph(sb.toString(), FONT_NORMAL)
        p.alignment = Element.ALIGN_CENTER
        return PdfPCell(p).apply {
            horizontalAlignment = Element.ALIGN_CENTER
            verticalAlignment = Element.ALIGN_TOP
            setPadding(6f)
            minimumHeight = 80f
        }
    }

    private fun formatearFecha(iso: String): String {
        return try { LocalDate.parse(iso).format(FECHA_FMT) } catch (e: Exception) { iso }
    }

    private fun formatearFechaSinBarras(iso: String): String {
        return try { LocalDate.parse(iso).format(FECHA_DOC_FMT) } catch (e: Exception) { iso }
    }
}
