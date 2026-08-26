package com.trainlog.analyzer.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.trainlog.analyzer.data.model.TrainingRun
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

object PdfExporter {

    fun exportAndShare(context: Context, run: TrainingRun) {
        val pdfFile = createPdf(context, run)
        sharePdf(context, pdfFile, run.name)
    }

    private fun createPdf(context: Context, run: TrainingRun): File {
        val document = PdfDocument()
        val pageW = 595 // A4 points
        val pageH = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var pageIndex = 1

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 20f
            isFakeBoldText = true
            color = 0xFF0F766E.toInt() // teal
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
            color = 0xFF64748B.toInt()
        }
        val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 13f
            isFakeBoldText = true
            color = 0xFF0F172A.toInt()
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
            color = 0xFF334155.toInt()
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 10f
            isFakeBoldText = true
            color = 0xFF64748B.toInt()
        }
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
            isFakeBoldText = true
            color = 0xFF0F766E.toInt()
        }
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xFFCCFBF1.toInt()
        }
        val badgeText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 10f
            isFakeBoldText = true
            color = 0xFF0F766E.toInt()
        }

        var y = 48f
        val left = 40f
        val right = pageW - 40f
        val maxWidth = right - left
        val lineHeight = 15f
        val bottomLimit = pageH - 48f

        fun newPage() {
            document.finishPage(page)
            pageIndex++
            val info = PdfDocument.PageInfo.Builder(pageW, pageH, pageIndex).create()
            page = document.startPage(info)
            canvas = page.canvas
            y = 48f
            // footer-ish page number later
        }

        fun ensure(space: Float = lineHeight) {
            if (y + space > bottomLimit) newPage()
        }

        fun drawWrapped(text: String, paint: Paint = bodyPaint) {
            val words = text.split(" ")
            var line = ""
            for (word in words) {
                val test = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(test) > maxWidth) {
                    ensure()
                    canvas.drawText(line, left, y, paint)
                    y += lineHeight
                    line = word
                } else {
                    line = test
                }
            }
            if (line.isNotEmpty()) {
                ensure()
                canvas.drawText(line, left, y, paint)
                y += lineHeight
            }
        }

        fun section(title: String) {
            y += 10f
            ensure(24f)
            canvas.drawText(title, left, y, headingPaint)
            y += 6f
            val rule = Paint().apply {
                color = 0xFFCCFBF1.toInt()
                strokeWidth = 2f
            }
            canvas.drawLine(left, y, right, y, rule)
            y += 16f
        }

        fun kv(label: String, value: String) {
            ensure()
            canvas.drawText(label, left, y, labelPaint)
            canvas.drawText(value.ifBlank { "—" }, left + 130f, y, bodyPaint)
            y += lineHeight
        }

        // ——— Header ———
        canvas.drawText(run.name.ifBlank { "Training Run" }, left, y, titlePaint)
        y += 18f
        canvas.drawText(
            "TrainLog report · ${run.date.ifBlank { "—" }}",
            left,
            y,
            subtitlePaint
        )
        y += 14f

        // Status badge
        val status = if (run.isPlateau) "PLATEAU" else "TRAINING"
        val statusColor = if (run.isPlateau) 0xFFFEE2E2.toInt() else 0xFFCCFBF1.toInt()
        val statusTextColor = if (run.isPlateau) 0xFFB91C1C.toInt() else 0xFF0F766E.toInt()
        val sw = badgeText.measureText(status) + 20f
        badgePaint.color = statusColor
        badgeText.color = statusTextColor
        canvas.drawRoundRect(left, y - 12f, left + sw, y + 6f, 8f, 8f, badgePaint)
        canvas.drawText(status, left + 10f, y, badgeText)
        y += 22f

        // ——— Metrics ———
        section("Ringkasan metrik")
        kv("Steps", run.totalSteps)
        kv("Resume", run.resumeFrom)
        kv("Train loss", run.finalTrainLoss)
        kv("Eval loss", run.finalEvalLoss)
        kv("Best eval", run.bestEvalLoss)
        kv("Rel. improvement", run.relativeImprovement.let {
            if (it.isBlank()) "—" else "$it %"
        })
        kv("Train–eval gap", run.trainEvalGap)
        kv("Perplexity", run.perplexity)
        kv("Tokens est.", run.tokensEstimate)
        kv("FLOPs est.", run.flopsEstimate)
        kv("Sec / step", run.secPerStep)

        // ——— Chart ———
        val train = if (run.lossSeries.isNotBlank()) Calc.parseSeries(run.lossSeries) else emptyList()
        val eval = if (run.evalSeries.isNotBlank()) Calc.parseSeries(run.evalSeries) else emptyList()

        if (train.isNotEmpty() || eval.isNotEmpty()) {
            section("Kurva loss")
            val chartH = 200f
            ensure(chartH + 40f)
            y = drawLossChart(
                canvas = canvas,
                train = train,
                eval = eval,
                left = left,
                top = y,
                width = maxWidth,
                height = chartH
            )
            y += 12f
            // legend
            val legTrain = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF0F766E.toInt(); strokeWidth = 3f
            }
            val legEval = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF7C3AED.toInt(); strokeWidth = 3f
            }
            val legEma = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFEA580C.toInt(); strokeWidth = 2f
                pathEffect = android.graphics.DashPathEffect(floatArrayOf(8f, 6f), 0f)
            }
            canvas.drawLine(left, y, left + 18f, y, legTrain)
            canvas.drawText("Train", left + 24f, y + 4f, bodyPaint)
            canvas.drawLine(left + 80f, y, left + 98f, y, legEval)
            canvas.drawText("Eval", left + 104f, y + 4f, bodyPaint)
            canvas.drawLine(left + 155f, y, left + 173f, y, legEma)
            canvas.drawText("EMA", left + 179f, y + 4f, bodyPaint)
            y += 20f

            // stats under chart
            val last = train.lastOrNull()
            val minV = train.minOrNull()
            val maxV = train.maxOrNull()
            val mean20 = train.takeLast(minOf(20, train.size)).let {
                if (it.isEmpty()) null else it.average()
            }
            canvas.drawText(
                "Last ${last?.let { "%.4f".format(it) } ?: "—"}   " +
                    "Min ${minV?.let { "%.4f".format(it) } ?: "—"}   " +
                    "Max ${maxV?.let { "%.4f".format(it) } ?: "—"}   " +
                    "μ20 ${mean20?.let { "%.4f".format(it) } ?: "—"}   " +
                    "(${train.size} points)",
                left,
                y,
                subtitlePaint
            )
            y += 18f
        } else {
            section("Kurva loss")
            drawWrapped("Tidak ada deret loss tersimpan. Import log dulu agar grafik muncul di PDF.")
        }

        // ——— Downstream ———
        section("Downstream & diagnosis")
        if (run.task1Name.isNotBlank()) kv(run.task1Name, run.task1Result)
        if (run.task2Name.isNotBlank()) kv(run.task2Name, run.task2Result)
        if (run.task3Name.isNotBlank()) kv(run.task3Name, run.task3Result)
        kv("Forgetting", if (run.hasForgetting) "Ya — ${run.forgettingNote}" else "Aman")
        val diagnoses = buildList {
            if (run.diagnosisFlat) add("Train & eval flat")
            if (run.diagnosisOverfit) add("Overfitting")
            if (run.diagnosisStillLearning) add("Masih belajar")
            if (run.diagnosisNoisy) add("Noisy loss")
            if (run.diagnosisDownstreamBad) add("Downstream jelek")
        }
        kv("Diagnosis", diagnoses.joinToString(", ").ifBlank { "—" })
        if (run.rootCause.isNotBlank()) {
            drawWrapped("Akar masalah: ${run.rootCause}")
        }

        section("Checkpoint & keputusan")
        kv("Final ckpt", run.finalCheckpoint)
        kv("Best ckpt", run.bestCheckpoint)
        if (run.checkpointNotes.isNotBlank()) {
            drawWrapped("Notes: ${run.checkpointNotes}")
        }
        drawWrapped("Keputusan: ${run.decision.ifBlank { "—" }}", accentPaint)

        section("Eksperimen")
        if (run.parentRunName.isNotBlank()) kv("Parent run", run.parentRunName)
        if (run.architecturePreset.isNotBlank()) kv("Preset", run.architecturePreset)
        drawWrapped("Hipotesis: ${run.hypothesis.ifBlank { "—" }}")
        drawWrapped("Perubahan: ${run.whatChanged.ifBlank { "—" }}")
        kv("Priority", run.priority)
        drawWrapped("Kesimpulan: ${run.conclusion.ifBlank { "—" }}")

        // Footer on last page
        y = max(y + 20f, bottomLimit - 10f)
        ensure(12f)
        canvas.drawText(
            "Generated by TrainLog · page $pageIndex",
            left,
            bottomLimit + 20f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 9f
                color = 0xFF94A3B8.toInt()
            }
        )

        document.finishPage(page)

        val safeName = run.name.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(40)
        val fileName = "TrainLog_${safeName}_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { out -> document.writeTo(out) }
        document.close()
        return file
    }

    /**
     * Draws a polished loss chart onto the PDF canvas. Returns the y coordinate
     * just below the chart box.
     */
    private fun drawLossChart(
        canvas: Canvas,
        train: List<Double>,
        eval: List<Double>,
        left: Float,
        top: Float,
        width: Float,
        height: Float
    ): Float {
        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = 0xFFE2E8F0.toInt()
            strokeWidth = 1.5f
        }
        val fillBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xFFF8FAFC.toInt()
        }
        canvas.drawRoundRect(left, top, left + width, top + height, 10f, 10f, fillBg)
        canvas.drawRoundRect(left, top, left + width, top + height, 10f, 10f, boxPaint)

        val padL = 42f
        val padR = 14f
        val padT = 16f
        val padB = 24f
        val plotL = left + padL
        val plotT = top + padT
        val plotW = width - padL - padR
        val plotH = height - padT - padB

        val ema = if (train.size >= 3) Calc.ema(train, 0.15) else emptyList()
        val all = train + eval + ema
        if (all.isEmpty()) return top + height

        val minY = all.minOrNull()!!
        val maxY = all.maxOrNull()!!
        val yPad = max((maxY - minY) * 0.08, 1e-6)
        val yMin = minY - yPad
        val yMax = maxY + yPad
        val span = max(yMax - yMin, 1e-12)

        fun mapX(i: Int, n: Int): Float {
            if (n <= 1) return plotL + plotW / 2
            return plotL + plotW * i / (n - 1)
        }
        fun mapY(v: Double): Float {
            val t = ((v - yMin) / span).toFloat()
            return plotT + plotH * (1f - t)
        }

        // grid + y labels
        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE2E8F0.toInt()
            strokeWidth = 1f
        }
        val axisLabel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f
            color = 0xFF64748B.toInt()
        }
        for (i in 0..4) {
            val frac = i / 4f
            val gy = plotT + plotH * frac
            canvas.drawLine(plotL, gy, plotL + plotW, gy, gridPaint)
            val value = yMax - span * frac
            val label = "%.2f".format(value)
            canvas.drawText(label, left + 4f, gy + 3f, axisLabel)
        }

        // axes
        val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF94A3B8.toInt()
            strokeWidth = 1.5f
        }
        canvas.drawLine(plotL, plotT, plotL, plotT + plotH, axisPaint)
        canvas.drawLine(plotL, plotT + plotH, plotL + plotW, plotT + plotH, axisPaint)

        fun drawArea(series: List<Double>, color: Int) {
            if (series.size < 2) return
            val path = Path()
            series.forEachIndexed { i, v ->
                val x = mapX(i, series.size)
                val yy = mapY(v)
                if (i == 0) path.moveTo(x, yy) else path.lineTo(x, yy)
            }
            path.lineTo(mapX(series.lastIndex, series.size), plotT + plotH)
            path.lineTo(mapX(0, series.size), plotT + plotH)
            path.close()
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                shader = LinearGradient(
                    0f, plotT, 0f, plotT + plotH,
                    color and 0x00FFFFFF or 0x38000000, // ~22% alpha
                    color and 0x00FFFFFF or 0x05000000,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawPath(path, paint)
        }

        fun drawSeries(series: List<Double>, color: Int, stroke: Float, dashed: Boolean = false) {
            if (series.isEmpty()) return
            val path = Path()
            series.forEachIndexed { i, v ->
                val x = mapX(i, series.size)
                val yy = mapY(v)
                if (i == 0) path.moveTo(x, yy) else path.lineTo(x, yy)
            }
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                this.color = color
                strokeWidth = stroke
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                if (dashed) {
                    pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 8f), 0f)
                }
            }
            canvas.drawPath(path, paint)
            // last point
            val lx = mapX(series.lastIndex, series.size)
            val ly = mapY(series.last())
            canvas.drawCircle(lx, ly, 4.5f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                this.color = color
            })
            canvas.drawCircle(lx, ly, 2f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                this.color = 0xFFFFFFFF.toInt()
            })
        }

        val trainColor = 0xFF0F766E.toInt()
        val evalColor = 0xFF7C3AED.toInt()
        val emaColor = 0xFFEA580C.toInt()

        if (train.isNotEmpty()) drawArea(train, trainColor)
        if (ema.isNotEmpty()) drawSeries(ema, emaColor, 2f, dashed = true)
        drawSeries(train, trainColor, 2.8f)
        drawSeries(eval, evalColor, 2.2f)

        // x labels: first / mid / last index
        if (train.size >= 2) {
            canvas.drawText("0", plotL, plotT + plotH + 14f, axisLabel)
            canvas.drawText(
                "${train.size / 2}",
                plotL + plotW / 2 - 8f,
                plotT + plotH + 14f,
                axisLabel
            )
            canvas.drawText(
                "${train.lastIndex}",
                plotL + plotW - 16f,
                plotT + plotH + 14f,
                axisLabel
            )
        }

        return top + height
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
