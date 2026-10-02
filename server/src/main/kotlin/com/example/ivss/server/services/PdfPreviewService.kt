package com.example.ivss.server.services

import org.apache.pdfbox.Loader
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

object PdfPreviewService {

    init {
        System.setProperty("java.awt.headless", "true")
    }

    /**
     * Convierte un PDF en bytes a una imagen PNG de alta calidad.
     */
    fun pdfAPng(
        pdfBytes: ByteArray,
        pagina: Int = 0,
        dpi: Int = 150
    ): ByteArray {
        Loader.loadPDF(pdfBytes).use { documento ->
            require(pagina in 0 until documento.numberOfPages) {
                "Página $pagina fuera de rango"
            }

            val renderer = PDFRenderer(documento)
            val imagen: BufferedImage = renderer.renderImageWithDPI(
                pagina,
                dpi.toFloat(),
                ImageType.RGB
            )

            val out = ByteArrayOutputStream()
            ImageIO.write(imagen, "PNG", out)
            return out.toByteArray()
        }
    }

    /**
     * Genera una miniatura pequeña para tarjetas y listados.
     */
    fun generarThumbnail(pdfBytes: ByteArray, anchoMaximo: Int = 300): ByteArray {
        Loader.loadPDF(pdfBytes).use { documento ->
            val renderer = PDFRenderer(documento)
            val imagen = renderer.renderImage(0, 1f)
            val escalada = escalarImagen(imagen, anchoMaximo)
            val out = ByteArrayOutputStream()
            ImageIO.write(escalada, "PNG", out)
            return out.toByteArray()
        }
    }

    private fun escalarImagen(original: BufferedImage, anchoMax: Int): BufferedImage {
        val ratio = anchoMax.toDouble() / original.width
        val nuevoAncho = anchoMax
        val nuevoAlto = (original.height * ratio).toInt()
        val escalada = BufferedImage(nuevoAncho, nuevoAlto, BufferedImage.TYPE_INT_RGB)
        val g = escalada.createGraphics()
        g.drawImage(original, 0, 0, nuevoAncho, nuevoAlto, null)
        g.dispose()
        return escalada
    }
}
