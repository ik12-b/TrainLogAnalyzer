package com.trainlog.analyzer.util

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.trainlog.analyzer.data.model.TrainingRun
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

    fun exportAndShare(context: Context, run: TrainingRun) {
        val pdfFile = createPdf(context, run)
        sharePdf(context, pdfFile, run.name)
    }

    private fun createPdf(context: Context, run: TrainingRun): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
            color = 0xFF1565C0.toInt()
        }
        val headingPaint = Paint().apply {
            textSize = 13f
            isFakeBoldText = true
            color = 0xFF212121.toInt()
        }
        val bodyPaint = Paint().apply {
            textSize = 11f
            color = 0xFF424242.toInt()
        }
        val labelPaint = Paint().apply {
            textSize = 11f
            isFakeBoldText = true
            color = 0xFF616161.toInt()
        }

        var y = 50f
        val left = 40f
        val maxWidth = 515f
        val lineHeight = 16f
        val pageHeight = 800f

        fun checkNewPage() {
            if (y > pageHeight) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
            }
        }

        fun drawLine(text: String, paint: Paint = bodyPaint) {
            checkNewPage()
            // Simple word wrap
            val words = text.split(" ")
            var line = ""
            for (word in words) {
                val test = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(test) > maxWidth) {
                    canvas.drawText(line, left, y, paint)
                    y += lineHeight
                    checkNewPage()
                    line = word
                } else {
                    line = test
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, left, y, paint)
                y += lineHeight
            }
        }

        fun drawRow(label: String, value: String) {
            if (value.isBlank()) return
            checkNewPage()
            canvas.drawText("$label:", left, y, labelPaint)
            canvas.drawText(value, left + 160f, y, bodyPaint)
            y += lineHeight
        }

        fun section(title: String) {
            y += 10f
            checkNewPage()
            canvas.drawText(title, left, y, headingPaint)
            y += lineHeight + 4f
        }

        // Title
        drawLine("TrainLog — Training Run Analysis", titlePaint)
        y += 8f
        drawLine(run.name.ifBlank { "Untitled Run" }, headingPaint)
        y += 4f
        drawLine("${run.date}  |  ${run.totalSteps} steps", bodyPaint)
        if (run.resumeFrom.isNotBlank()) {
            drawLine("Resume from: ${run.resumeFrom}", bodyPaint)
        }
        y += 6f

        // Status
        val status = if (run.isPlateau) "Status: Plateau" else "Status: Masih belajar"
        drawLine(status, headingPaint)

        section("1. Learning Curve")
        drawRow("Train Loss", run.finalTrainLoss)
        drawRow("Eval Loss", run.finalEvalLoss)
        drawRow("Best Eval Loss", run.bestEvalLoss)
        drawRow("Rel. Improvement", if (run.relativeImprovement.isNotBlank()) "${run.relativeImprovement}%" else "")
        drawRow("Train–Eval Gap", run.trainEvalGap)
        drawRow("Noise Level", run.noiseLevel)

        section("2. Downstream & Generalization")
        drawRow("Perplexity", run.perplexity)
        if (run.task1Name.isNotBlank()) drawRow(run.task1Name, run.task1Result)
        if (run.task2Name.isNotBlank()) drawRow(run.task2Name, run.task2Result)
        drawRow("Catastrophic Forgetting", if (run.hasForgetting) "Ada degradasi" else "Aman")

        section("3. Diagnosis")
        val diagnoses = buildList {
            if (run.diagnosisFlat) add("Train & Eval flat")
            if (run.diagnosisOverfit) add("Overfitting")
            if (run.diagnosisStillLearning) add("Masih ada ruang belajar")
            if (run.diagnosisNoisy) add("Sangat noisy")
            if (run.diagnosisDownstreamBad) add("Downstream jelek")
        }
        if (diagnoses.isEmpty()) {
            drawLine("-")
        } else {
            diagnoses.forEach { drawLine("• $it") }
        }
        if (run.rootCause.isNotBlank()) {
            y += 4f
            drawLine("Akar masalah:", labelPaint)
            drawLine(run.rootCause)
        }

        section("4. Keputusan")
        drawLine(run.decision.ifBlank { "-" })
        drawRow("Checkpoint final", run.finalCheckpoint)

        section("5. Next Experiment Plan")
        if (run.hypothesis.isNotBlank()) {
            drawLine("Hipotesis:", labelPaint)
            drawLine(run.hypothesis)
            y += 4f
        }
        if (run.whatChanged.isNotBlank()) {
            drawLine("Yang diubah:", labelPaint)
            drawLine(run.whatChanged)
            y += 4f
        }
        drawRow("Priority", run.priority)

        if (run.conclusion.isNotBlank()) {
            section("6. Kesimpulan")
            drawLine(run.conclusion)
        }

        y += 20f
        checkNewPage()
        canvas.drawText("Generated by TrainLog Analyzer", left, y, Paint().apply {
            textSize = 9f
            color = 0xFF9E9E9E.toInt()
        })

        document.finishPage(page)

        val safeName = run.name.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(40)
        val fileName = "TrainLog_${safeName}_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    private fun sharePdf(context: Context, file: File, title: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "TrainLog: $title")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export PDF"))
    }
}
