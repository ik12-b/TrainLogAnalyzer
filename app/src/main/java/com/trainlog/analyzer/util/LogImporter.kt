package com.trainlog.analyzer.util

/**
 * Parser for real training logs:
 * - HuggingFace Trainer dict lines: {'loss': 2.115, 'learning_rate': ...}
 * - Tunix / custom: [loss-monitor] train/loss: step=553102 value=1.9185
 * - Qwen GaLore Kaggle-style config banners + progress bars
 */
data class ImportedLog(
    val trainLosses: List<Double>,
    val evalLosses: List<Double>,
    val steps: List<Int>,
    val lastStep: Int?,
    val totalSteps: Int?,
    val lastTrainLoss: Double?,
    val lastEvalLoss: Double?,
    val perplexity: Double?,
    val learningRates: List<Double>,
    val lastLr: Double?,
    val gradNorms: List<Double>,
    val lastGradNorm: Double?,
    val epochs: List<Double>,
    val lastEpoch: Double?,
    val globalBatch: Int?,
    val seqLen: Int?,
    val params: Long?,
    val modelPath: String?,
    val resumeFrom: String?,
    val galoreRank: Int?,
    val secPerStepEstimate: Double?,
    val rawPointCount: Int,
    val sourceHint: String
)

object LogImporter {

    fun import(text: String): ImportedLog {
        val train = mutableListOf<Double>()
        val eval = mutableListOf<Double>()
        val steps = mutableListOf<Int>()
        val lrs = mutableListOf<Double>()
        val grads = mutableListOf<Double>()
        val epochs = mutableListOf<Double>()

        // 1) Tunix / loss-monitor
        Regex("""train/loss:\s*step=(\d+)\s+value=([0-9.eE+-]+)""")
            .findAll(text)
            .forEach { m ->
                m.groupValues[1].toIntOrNull()?.let(steps::add)
                m.groupValues[2].toDoubleOrNull()?.let(train::add)
            }

        // 2) HF Trainer Python-dict style (single or double quotes)
        Regex(
            """\{[^{}]*['"]loss['"]\s*:\s*['"]?([0-9.eE+-]+)['"]?[^{}]*\}"""
        ).findAll(text).forEach { m ->
            m.groupValues[1].toDoubleOrNull()?.let(train::add)
            val block = m.value
            Regex("""['"]learning_rate['"]\s*:\s*['"]?([0-9.eE+-]+)""")
                .find(block)?.groupValues?.get(1)?.toDoubleOrNull()?.let(lrs::add)
            Regex("""['"]grad_norm['"]\s*:\s*['"]?([0-9.eE+-]+)""")
                .find(block)?.groupValues?.get(1)?.toDoubleOrNull()?.let(grads::add)
            Regex("""['"]epoch['"]\s*:\s*['"]?([0-9.eE+-]+)""")
                .find(block)?.groupValues?.get(1)?.toDoubleOrNull()?.let(epochs::add)
        }

        // 3) eval_loss
        Regex("""['"]?eval_loss['"]?\s*[:=]\s*['"]?([0-9.eE+-]+)""")
            .findAll(text)
            .forEach { m -> m.groupValues[1].toDoubleOrNull()?.let(eval::add) }
        Regex("""eval/loss:\s*step=\d+\s+value=([0-9.eE+-]+)""")
            .findAll(text)
            .forEach { m -> m.groupValues[1].toDoubleOrNull()?.let(eval::add) }

        // 4) Fallback: plain series if still empty
        if (train.isEmpty()) {
            train.addAll(Calc.parseSeries(text))
        }

        // Progress bar: | 8600/9066 [
        var lastStep: Int? = steps.lastOrNull()
        var totalSteps: Int? = null
        Regex("""\|\s*(\d+)/(\d+)\s*\[""").findAll(text).forEach { m ->
            m.groupValues[1].toIntOrNull()?.let { s ->
                if (lastStep == null || s > lastStep!!) lastStep = s
            }
            m.groupValues[2].toIntOrNull()?.let { t -> totalSteps = t }
        }
        if (lastStep == null) {
            Regex("""\bstep[s]?\s*[=:]?\s*(\d{3,})""", RegexOption.IGNORE_CASE)
                .findAll(text)
                .mapNotNull { it.groupValues[1].toIntOrNull() }
                .maxOrNull()
                ?.let { lastStep = it }
        }

        // Config banners (Qwen GaLore notebook style)
        val globalBatch = Regex("""Global batch(?:\s*\(2 GPU\))?\s*:\s*(\d+)""", RegexOption.IGNORE_CASE)
            .find(text)?.groupValues?.get(1)?.toIntOrNull()
            ?: Regex("""Global batch\s*:\s*(\d+)""").find(text)?.groupValues?.get(1)?.toIntOrNull()
        val seqLen = Regex("""Seq len\s*:\s*(\d+)""", RegexOption.IGNORE_CASE)
            .find(text)?.groupValues?.get(1)?.toIntOrNull()
        val paramsStr = Regex("""Total params\s*:\s*([\d,]+)""")
            .find(text)?.groupValues?.get(1)?.replace(",", "")
        val params = paramsStr?.toLongOrNull()
        val modelPath = Regex("""Model sumber\s*:\s*(\S+)""")
            .find(text)?.groupValues?.get(1)
            ?: Regex("""Memuat model:\s*(\S+)""").find(text)?.groupValues?.get(1)
        val resumeFrom = Regex("""Resume from ckpt\s*:\s*(\S+)""")
            .find(text)?.groupValues?.get(1)
            ?: modelPath
        val galoreRank = Regex("""GaLore rank\s*:\s*(\d+)""")
            .find(text)?.groupValues?.get(1)?.toIntOrNull()
            ?: Regex("""GaLore rank/gap/scale:\s*(\d+)""")
                .find(text)?.groupValues?.get(1)?.toIntOrNull()

        // Throughput: "32.76s/it" near the end
        val secPerStep = Regex("""(\d+\.?\d*)s/it""")
            .findAll(text)
            .mapNotNull { it.groupValues[1].toDoubleOrNull() }
            .lastOrNull()

        val sourceHint = when {
            text.contains("[loss-monitor]") -> "Tunix / loss-monitor"
            text.contains("GaLore") || text.contains("{'loss'") || text.contains("\"loss\"") ->
                "HuggingFace / GaLore"
            train.isNotEmpty() -> "Generic loss series"
            else -> "Unknown / empty"
        }

        val lastTrain = train.lastOrNull()
        val lastEval = eval.lastOrNull()
        val ppl = lastEval?.let { Calc.perplexityFromLoss(it) }
            ?: lastTrain?.let { Calc.perplexityFromLoss(it) }

        return ImportedLog(
            trainLosses = train,
            evalLosses = eval,
            steps = steps,
            lastStep = lastStep,
            totalSteps = totalSteps,
            lastTrainLoss = lastTrain,
            lastEvalLoss = lastEval,
            perplexity = ppl,
            learningRates = lrs,
            lastLr = lrs.lastOrNull(),
            gradNorms = grads,
            lastGradNorm = grads.lastOrNull(),
            epochs = epochs,
            lastEpoch = epochs.lastOrNull(),
            globalBatch = globalBatch,
            seqLen = seqLen,
            params = params,
            modelPath = modelPath,
            resumeFrom = resumeFrom,
            galoreRank = galoreRank,
            secPerStepEstimate = secPerStep,
            rawPointCount = train.size + eval.size,
            sourceHint = sourceHint
        )
    }

    fun seriesToText(series: List<Double>): String =
        series.joinToString("\n") { v ->
            "%.6f".format(v).trimEnd('0').trimEnd('.')
        }

    /** Tokens seen ≈ steps × global_batch × seq_len */
    fun estimateTokens(imp: ImportedLog): Double? {
        val st = imp.lastStep?.toDouble() ?: return null
        val b = imp.globalBatch?.toDouble() ?: return null
        val s = imp.seqLen?.toDouble() ?: return null
        return st * b * s
    }

    fun estimateFlops(imp: ImportedLog): Double? {
        val n = imp.params?.toDouble() ?: return null
        val d = estimateTokens(imp) ?: return null
        return Calc.trainingFlops(n, d)
    }
}
