package com.example.apuniliapp.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.apuniliapp.data.model.MemberProfile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilitaire pour générer un certificat PDF d'adhésion
 */
object CertificateGenerator {

    private const val PAGE_WIDTH = 792  // 8.5 inch en points
    private const val PAGE_HEIGHT = 612  // 11 inch en points

    /**
     * Génère un certificat PDF pour un membre
     */
    fun generateCertificate(
        context: Context,
        profile: MemberProfile,
        onSuccess: (filePath: String) -> Unit,
        onError: (errorMessage: String) -> Unit
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            val canvas = page.canvas

            // Dessiner le certificat
            drawCertificate(canvas, profile)

            pdfDocument.finishPage(page)

            // Sauvegarder le fichier
            val fileName = "Certificat_Adhesion_${profile.userId}.pdf"
            val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val certificateFile = File(documentsDir, fileName)

            FileOutputStream(certificateFile).use { fos ->
                pdfDocument.writeTo(fos)
            }

            pdfDocument.close()

            onSuccess(certificateFile.absolutePath)
        } catch (e: Exception) {
            onError("Erreur lors de la génération du certificat: ${e.message}")
        }
    }

    private fun drawCertificate(canvas: Canvas, profile: MemberProfile) {
        // Fond (optionnel)
        val bgPaint = Paint().apply {
            color = android.graphics.Color.WHITE
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

        // Bordure décorative
        val borderPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1976D2")
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }
        canvas.drawRect(20f, 20f, PAGE_WIDTH - 20f, PAGE_HEIGHT - 20f, borderPaint)

        // Titre
        val titlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1565C0")
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("CERTIFICAT D'ADHÉSION", (PAGE_WIDTH / 2).toFloat(), 80f, titlePaint)

        // Sous-titre
        val subtitlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#424242")
            textSize = 14f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("ASSOCIATION APUNILI ASBL", (PAGE_WIDTH / 2).toFloat(), 110f, subtitlePaint)

        // Ligne séparatrice
        val linePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#BDBDBD")
            strokeWidth = 2f
        }
        canvas.drawLine(50f, 130f, (PAGE_WIDTH - 50).toFloat(), 130f, linePaint)

        // Contenu principal
        val contentPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#212121")
            textSize = 12f
            isAntiAlias = true
        }

        var y = 170f
        val lineHeight = 25f

        // Texte introductif
        canvas.drawText(
            "Par la présente, nous certifions que:",
            50f,
            y,
            contentPaint
        )
        y += lineHeight * 1.5f

        // Nom du membre
        val namePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1565C0")
            textSize = 18f
            isAntiAlias = true
        }
        canvas.drawText(
            "${profile.firstname} ${profile.lastname}",
            50f,
            y,
            namePaint
        )
        y += lineHeight * 1.5f

        // Texte de confirmation
        canvas.drawText(
            "est membre à part entière de l'Association APUNILI ASBL",
            50f,
            y,
            contentPaint
        )
        y += lineHeight

        canvas.drawText(
            "et jouit de tous les droits et avantages qui en découlent.",
            50f,
            y,
            contentPaint
        )
        y += lineHeight * 2f

        // Dates
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("fr", "FR"))
        val approvalDate = if (profile.membershipApprovalDate > 0) {
            dateFormat.format(Date(profile.membershipApprovalDate))
        } else {
            dateFormat.format(Date())
        }

        canvas.drawText(
            "Date d'approbation: $approvalDate",
            50f,
            y,
            contentPaint
        )
        y += lineHeight * 2f

        // Signature
        val signaturePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#424242")
            textSize = 10f
            isAntiAlias = true
        }

        canvas.drawText(
            "Cachet et signature de l'Administration",
            (PAGE_WIDTH - 200).toFloat(),
            y,
            signaturePaint
        )

        // Pied de page
        val footerPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#757575")
            textSize = 8f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        canvas.drawText(
            "© 2026 ASBL Apunili - Tous droits réservés",
            (PAGE_WIDTH / 2).toFloat(),
            (PAGE_HEIGHT - 20).toFloat(),
            footerPaint
        )
    }
}

