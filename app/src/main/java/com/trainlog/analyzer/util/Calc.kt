package com.trainlog.analyzer.util

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object Calc {

    fun parseNum(v: String): Double? =
        v.trim().replace(",", "").toDoubleOrNull()

    fun parseSeries(text: String): List<Double> =
        text.split(Regex("[\\s,;]+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { it.replace(",", "").toDoubleOrNull() }

    fun parseTrainerLosses(text: String): List<Double> {
        val out = mutableListOf<Double>()
        Regex("""['"]loss['"]\s*:\s*['"]?([0-9.eE+-]+)""").findAll(text).forEach {
            it.groupValues[1].toDoubleOrNull()?.let(out::add)
        }
        if (out.isEmpty()) {
            Regex("""train/loss:\s*step=\d+\s+value=([0-9.eE+-]+)""").findAll(text).forEach {
                it.groupValues[1].toDoubleOrNull()?.let(out::add)
            }
        }
        return if (out.isEmpty()) parseSeries(text) else out
    }

    /** (L_old - L_new) / L_old */
    fun relativeImprovement(oldLoss: Double, newLoss: Double): Pair<Double, Double>? {
        if (oldLoss == 0.0) return null
        val frac = (oldLoss - newLoss) / oldLoss
        return frac to frac * 100
    }

    fun perplexityFromLoss(loss: Double) = exp(loss)

    fun lossFromPerplexity(ppl: Double): Double? =
        if (ppl <= 0) null else ln(ppl)

    fun ema(series: List<Double>, alpha: Double): List<Double> {
        if (series.isEmpty()) return emptyList()
        val a = alpha.coerceIn(0.0, 1.0)
        val out = mutableListOf(series[0])
        for (i in 1 until series.size) {
            out.add(a * series[i] + (1 - a) * out[i - 1])
        }
        return out
    }

    fun linearSlope(series: List<Double>): Double? {
        val n = series.size
        if (n < 2) return null
        var sumX = 0.0
        var sumY = 0.0
        var sumXY = 0.0
        var sumXX = 0.0
        for (i in 0 until n) {
            sumX += i
            sumY += series[i]
            sumXY += i * series[i]
            sumXX += i * i.toDouble()
        }
        val den = n * sumXX - sumX * sumX
        if (den == 0.0) return 0.0
        return (n * sumXY - sumX * sumY) / den
    }

    data class PlateauResult(
        val oldL: Double,
        val newL: Double,
        val window: Int,
        val relPct: Double,
        val slope: Double?,
        val isPlateau: Boolean,
        val thresholdPct: Double
    )

    fun plateauCheck(
        series: List<Double>,
        window: Int,
        thresholdPct: Double
    ): PlateauResult? {
        if (series.size < 2) return null
        val w = min(max(1, window), series.size - 1)
        val oldL = series[series.size - 1 - w]
        val newL = series.last()
        val rel = relativeImprovement(oldL, newL) ?: return null
        val slope = linearSlope(series.takeLast(w + 1))
        return PlateauResult(
            oldL = oldL,
            newL = newL,
            window = w,
            relPct = rel.second,
            slope = slope,
            isPlateau = rel.second < thresholdPct,
            thresholdPct = thresholdPct
        )
    }

    fun effectiveBatch(micro: Double, accum: Double, gpus: Double) =
        micro * accum * gpus

    fun tokensSeen(steps: Double, globalBatch: Double, seqLen: Double) =
        steps * globalBatch * seqLen

    fun cosineLr(
        t: Double,
        T: Double,
        lrMax: Double,
        lrMin: Double,
        warmup: Double = 0.0
    ): Double {
        if (T <= 0) return lrMax
        if (t <= 0) return if (warmup > 0) 0.0 else lrMax
        if (warmup > 0 && t < warmup) return lrMax * t / warmup
        val span = max(1.0, T - warmup)
        val progress = ((t - warmup) / span).coerceIn(0.0, 1.0)
        return lrMin + 0.5 * (lrMax - lrMin) * (1 + cos(PI * progress))
    }

    fun scaleLr(
        lrOld: Double,
        batchOld: Double,
        batchNew: Double,
        sqrtMode: Boolean
    ): Double? {
        if (batchOld <= 0) return null
        val r = batchNew / batchOld
        return if (sqrtMode) lrOld * sqrt(r) else lrOld * r
    }

    /** Chinchilla / Kaplan training FLOPs ≈ 6 * N * D */
    fun trainingFlops(params: Double, tokens: Double) = 6.0 * params * tokens

    fun formatFlops(flops: Double): String {
        val abs = kotlin.math.abs(flops)
        return when {
            abs >= 1e24 -> "%.2f YFLOPs".format(flops / 1e24)
            abs >= 1e21 -> "%.2f ZFLOPs".format(flops / 1e21)
            abs >= 1e18 -> "%.2f EFLOPs".format(flops / 1e18)
            abs >= 1e15 -> "%.2f PFLOPs".format(flops / 1e15)
            abs >= 1e12 -> "%.2f TFLOPs".format(flops / 1e12)
            abs >= 1e9 -> "%.2f GFLOPs".format(flops / 1e9)
            else -> "%.2e FLOPs".format(flops)
        }
    }

    fun formatTokens(n: Double): String {
        val abs = kotlin.math.abs(n)
        return when {
            abs >= 1e12 -> "%.2f T".format(n / 1e12)
            abs >= 1e9 -> "%.2f B".format(n / 1e9)
            abs >= 1e6 -> "%.2f M".format(n / 1e6)
            abs >= 1e3 -> "%.2f K".format(n / 1e3)
            else -> "%.0f".format(n)
        }
    }

    fun formatDuration(seconds: Double): String {
        if (!seconds.isFinite() || seconds < 0) return "—"
        val s = seconds.toLong()
        val d = s / 86400
        val h = (s % 86400) / 3600
        val m = (s % 3600) / 60
        val sec = s % 60
        val parts = mutableListOf<String>()
        if (d > 0) parts += "${d}h"
        if (h > 0) parts += "${h}j"
        if (m > 0) parts += "${m}m"
        if (d == 0L && h == 0L) parts += "${sec}s"
        return parts.joinToString(" ")
    }

    fun packingEfficiency(used: Double, padded: Double): Double? =
        if (padded <= 0) null else used / padded

    fun trainEvalGap(train: Double, eval: Double) = eval - train

    fun suggestedWindow(totalSteps: Double): Int {
        if (totalSteps <= 0) return 500
        return max(50, (totalSteps * 0.03).toInt())
    }

    /** Linear decay after warmup */
    fun linearLr(
        t: Double,
        T: Double,
        lrMax: Double,
        lrMin: Double,
        warmup: Double = 0.0
    ): Double {
        if (T <= 0) return lrMax
        if (warmup > 0 && t < warmup) return if (warmup == 0.0) lrMax else lrMax * t / warmup
        val span = max(1.0, T - warmup)
        val progress = ((t - warmup) / span).coerceIn(0.0, 1.0)
        return lrMax + (lrMin - lrMax) * progress
    }

    /**
     * Warmup–Stable–Decay (WSD): linear warmup, constant lrMax until decayStart fraction,
     * then cosine to lrMin.
     */
    fun wsdLr(
        t: Double,
        T: Double,
        lrMax: Double,
        lrMin: Double,
        warmup: Double,
        decayStartFrac: Double = 0.8
    ): Double {
        if (T <= 0) return lrMax
        if (t < warmup) return if (warmup <= 0) lrMax else lrMax * t / warmup
        val decayStart = T * decayStartFrac.coerceIn(0.0, 1.0)
        if (t < decayStart) return lrMax
        val span = max(1.0, T - decayStart)
        val progress = ((t - decayStart) / span).coerceIn(0.0, 1.0)
        return lrMin + 0.5 * (lrMax - lrMin) * (1 + cos(PI * progress))
    }

    fun cosineWithRestarts(
        t: Double,
        cycleLen: Double,
        lrMax: Double,
        lrMin: Double
    ): Double {
        if (cycleLen <= 0) return lrMax
        val pos = t % cycleLen
        val progress = (pos / cycleLen).coerceIn(0.0, 1.0)
        return lrMin + 0.5 * (lrMax - lrMin) * (1 + cos(PI * progress))
    }

    fun mixtureTokens(weights: List<Double>, totalTokens: Double): List<Double> {
        val sum = weights.sum().let { if (it <= 0) 1.0 else it }
        return weights.map { totalTokens * it / sum }
    }

    fun chinchillaRatio(tokens: Double, params: Double): Double? {
        if (params <= 0) return null
        return tokens / params
    }

    fun gpuCost(hours: Double, rate: Double) = hours * rate

    fun bestCheckpoint(notes: String): Pair<String, Double>? {
        // format "name:loss" or "step:loss" per line / semicolon
        val parts = notes.split(Regex("[;\n]+")).map { it.trim() }.filter { it.isNotEmpty() }
        var best: Pair<String, Double>? = null
        for (p in parts) {
            val bits = p.split(":")
            if (bits.size < 2) continue
            val name = bits[0].trim()
            val loss = bits.last().trim().toDoubleOrNull() ?: continue
            if (best == null || loss < best.second) best = name to loss
        }
        return best
    }
}
