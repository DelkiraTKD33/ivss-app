package com.example.ivss.server.services

import com.example.ivss.server.model.DatosConstancia
import org.apache.poi.xwpf.usermodel.*
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object WordService {

    private val FECHA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val FECHA_DOC_FMT = DateTimeFormatter.ofPattern("ddMMyyyy")

    fun generarConstancia(datos: DatosConstancia): ByteArray {
        val doc = XWPFDocument()
        val out = ByteArrayOutputStream()

        parrafo(doc, "PARA: ${FormatoVacaciones.PARA}", bold = true, size = 9)
        parrafo(doc, "DE: ${FormatoVacaciones.DE}", bold = true, size = 9)
        parrafo(doc, "")

        val t1 = doc.createTable(2, 2)
        t1.setWidth("100%")
        setCell(t1.getRow(0).getCell(0), FormatoVacaciones.TITULO_APELLIDOS, titulo = true)
        setCell(t1.getRow(0).getCell(1), FormatoVacaciones.TITULO_CEDULA, titulo = true)
        setCell(t1.getRow(1).getCell(0), datos.apellidosNombres, bold = true)
        setCell(t1.getRow(1).getCell(1), datos.cedula, bold = true)

        parrafo(doc, "")

        val t2 = doc.createTable(2, 4)
        t2.setWidth("100%")
        setCell(t2.getRow(0).getCell(0), FormatoVacaciones.TITULO_DENOMINACION, titulo = true)
        setCell(t2.getRow(0).getCell(1), FormatoVacaciones.TITULO_NUMERO_CARGO, titulo = true)
        setCell(t2.getRow(0).getCell(2), FormatoVacaciones.TITULO_FECHA_INGRESO, titulo = true)
        setCell(t2.getRow(0).getCell(3), FormatoVacaciones.TITULO_COD_ORIGEN, titulo = true)

        setCell(t2.getRow(1).getCell(0), datos.denominacionCargo)
        setCell(t2.getRow(1).getCell(1), datos.numeroCargo)
        setCell(t2.getRow(1).getCell(2), formatearFecha(datos.fechaIngreso))
        setCell(t2.getRow(1).getCell(3), datos.codigoOrigenServicio)

        parrafo(doc, "")

        val t3 = doc.createTable(2, 3)
        t3.setWidth("100%")
        setCell(t3.getRow(0).getCell(0), FormatoVacaciones.TITULO_UNIDAD, titulo = true)
        setCell(t3.getRow(0).getCell(1), FormatoVacaciones.TITULO_LUGAR, titulo = true)
        setCell(t3.getRow(0).getCell(2), FormatoVacaciones.TITULO_HORARIO, titulo = true)

        setCell(t3.getRow(1).getCell(0), datos.unidadServicio)
        setCell(t3.getRow(1).getCell(1), datos.lugar)
        setCell(t3.getRow(1).getCell(2), datos.horario)

        parrafo(doc, "")

        parrafo(doc, FormatoVacaciones.TEXTO_TOMA_VACACIONES, bold = true, size = 9, centrado = true)

        parrafo(doc, FormatoVacaciones.TITULO_LAPSO, bold = true, size = 10, centrado = true)

        val t4 = doc.createTable(2, 4)
        t4.setWidth("100%")
        setCell(t4.getRow(0).getCell(0), FormatoVacaciones.TITULO_DESDE, titulo = true)
        setCell(t4.getRow(0).getCell(1), FormatoVacaciones.TITULO_HASTA, titulo = true)
        setCell(t4.getRow(0).getCell(2), FormatoVacaciones.TITULO_PERIODO, titulo = true)
        setCell(t4.getRow(0).getCell(3), FormatoVacaciones.TITULO_DIAS, titulo = true)

        setCell(t4.getRow(1).getCell(0), "${formatearFecha(datos.fechaDesde)} ${formatearFechaSinBarras(datos.fechaDesde)}")
        setCell(t4.getRow(1).getCell(1), "${formatearFecha(datos.fechaHasta)} ${formatearFechaSinBarras(datos.fechaHasta)}")
        setCell(t4.getRow(1).getCell(2), datos.periodo)
        setCell(t4.getRow(1).getCell(3), datos.numeroDias.toString())

        parrafo(doc, "")

        val t5 = doc.createTable(1, 2)
        t5.setWidth("100%")
        setCell(t5.getRow(0).getCell(0), FormatoVacaciones.TEXTO_REINTEGRO, bold = true)
        setCell(t5.getRow(0).getCell(1),
            "${formatearFecha(datos.fechaReintegro)} ${formatearFechaSinBarras(datos.fechaReintegro)}", bold = true)

        parrafo(doc, "")
        parrafo(doc, "")

        parrafo(doc, FormatoVacaciones.TITULO_OBSERVACIONES, bold = true, size = 9)
        if (!datos.observaciones.isNullOrBlank()) {
            parrafo(doc, datos.observaciones, size = 9)
        }
        parrafo(doc, "")
        parrafo(doc, FormatoVacaciones.TITULO_NOTA, bold = true, size = 9)
        val notaTexto = datos.nota
            ?: "EL TRABAJADOR SOLICITÓ DICHAS VACACIONES CON EXPOSICIÓN DE MOTIVO. CORRESPONDIENTES AL PERIODO ${datos.periodo}."
        parrafo(doc, notaTexto, size = 9)

        parrafo(doc, "")
        parrafo(doc, "")

        val t6 = doc.createTable(2, 2)
        t6.setWidth("100%")

        setCellFirma(t6.getRow(0).getCell(0), FormatoVacaciones.FIRMA_SUPERVISOR, datos.supervisorInmediato ?: "DR. WILLIAMS GONZALEZ")
        setCellFirma(t6.getRow(0).getCell(1), FormatoVacaciones.FIRMA_COORDINADOR, datos.coordinadorRRHH ?: "LCDA. MAYARI SOJO")
        setCellFirma(t6.getRow(1).getCell(0), FormatoVacaciones.FIRMA_MAXIMA, datos.maximaAutoridad ?: "DR. JULIO AQUINO")
        setCellFirma(t6.getRow(1).getCell(1), FormatoVacaciones.FIRMA_TRABAJADOR, datos.apellidosNombres)

        doc.write(out)
        doc.close()
        return out.toByteArray()
    }

    private fun parrafo(
        doc: XWPFDocument,
        texto: String,
        bold: Boolean = false,
        size: Int = 9,
        centrado: Boolean = false
    ) {
        val p = doc.createParagraph()
        p.alignment = if (centrado) ParagraphAlignment.CENTER else ParagraphAlignment.LEFT
        val run = p.createRun()
        run.setText(texto)
        run.isBold = bold
        run.fontSize = size
        run.fontFamily = "Arial"
    }

    private fun setCell(cell: XWPFTableCell, texto: String, titulo: Boolean = false, bold: Boolean = false) {
        cell.removeParagraph(0)
        val p = cell.addParagraph()
        p.alignment = ParagraphAlignment.CENTER
        val run = p.createRun()
        run.setText(texto)
        run.isBold = titulo || bold
        run.fontSize = if (titulo) 8 else 9
        run.fontFamily = "Arial"
        if (titulo) {
            cell.setColor("E6E6E6")
        }
    }

    private fun setCellFirma(cell: XWPFTableCell, titulo: String, nombre: String?) {
        cell.removeParagraph(0)

        val p1 = cell.addParagraph()
        p1.alignment = ParagraphAlignment.CENTER
        p1.createRun().apply {
            setText(titulo)
            isBold = true
            fontSize = 9
            fontFamily = "Arial"
        }

        val p2 = cell.addParagraph()
        p2.alignment = ParagraphAlignment.CENTER
        p2.createRun().apply { setText(""); fontSize = 9 }

        val p3 = cell.addParagraph()
        p3.alignment = ParagraphAlignment.CENTER
        p3.createRun().apply {
            setText(nombre ?: "")
            fontSize = 9
            fontFamily = "Arial"
        }

        val p4 = cell.addParagraph()
        p4.alignment = ParagraphAlignment.CENTER
        p4.createRun().apply {
            setText(FormatoVacaciones.LABEL_NOMBRE_APELLIDO)
            fontSize = 8
            fontFamily = "Arial"
        }

        val p5 = cell.addParagraph()
        p5.alignment = ParagraphAlignment.CENTER
        p5.createRun().apply { setText(""); fontSize = 9 }

        val p6 = cell.addParagraph()
        p6.alignment = ParagraphAlignment.CENTER
        p6.createRun().apply {
            setText(if (titulo == FormatoVacaciones.FIRMA_TRABAJADOR)
                FormatoVacaciones.LABEL_FIRMA else FormatoVacaciones.LABEL_FIRMA_SELLO)
            fontSize = 9
            fontFamily = "Arial"
        }
    }

    private fun formatearFecha(iso: String): String =
        try { LocalDate.parse(iso).format(FECHA_FMT) } catch (e: Exception) { iso }

    private fun formatearFechaSinBarras(iso: String): String =
        try { LocalDate.parse(iso).format(FECHA_DOC_FMT) } catch (e: Exception) { iso }
}
