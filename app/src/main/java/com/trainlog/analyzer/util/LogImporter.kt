package com.trainlog.analyzer.util

data class ImportedLog(
    val trainLosses: List<Double>,
    val evalLosses: List<Double>,
    val lastStep: Int?,
    val lastTrainLoss: Double?,
    val lastEvalLoss: Double?,
    val perplexity: Double?,
    val rawPointCount: Int
)

object LogImporter {

    fun import(text: String): ImportedLog {
        val train = mutableListOf<Double>()
        val eval = mutableListOf<Double>()
        var lastStep: Int? = null

        // HF Trainer dict-style: {'loss': 2.11, 'epoch': 1.2}
        Regex("""\{[^{}]*['"]loss['"]\s*:\s*([0-9.eE+-]+)[^{}]*\}""").findAll(text).forEach { m ->
            m.groupValues[1].toDoubleOrNull()?.let { train.add(it) }
        }

        // train/loss: step=123 value=2.11
        Regex("""train/loss:\s*step=(\d+)\s+value=([0-9.eE+-]+)""").findAll(text).forEach { m ->
            m.groupValues[1].toIntOrNull()?.let { lastStep = it }
            m.groupValues[2].toDoubleOrNull()?.let { train.add(it) }
        }

        // eval_loss: 2.12 or 'eval_loss': 2.12
        Regex("""['"]?eval_loss['"]?\s*[:=]\s*([0-9.eE+-]+)""").findAll(text).forEach { m ->
            m.groupValues[1].toDoubleOrNull()?.let { eval.add(it) }
        }
        Regex("""eval/loss:\s*step=\d+\s+value=([0-9.eE+-]+)""").findAll(text).forEach { m ->
            m.groupValues[1].toDoubleOrNull()?.let { eval.add(it) }
        }

        // generic loss: lines if still empty
        if (train.isEmpty()) {
            train.addAll(Calc.parseSeries(text))
        }

        // step markers elsewhere
        Regex("""\bstep[s]?\s*[=:]?\s*(\d{2,})""", RegexOption.IGNORE_CASE).findAll(text).forEach { m ->
            m.groupValues[1].toIntOrNull()?.let { s ->
                if (lastStep == null || s > lastStep!!) lastStep = s
            }
        }

        val lastTrain = train.lastOrNull()
        val lastEval = eval.lastOrNull()
        val ppl = lastEval?.let { Calc.perplexityFromLoss(it) }
            ?: lastTrain?.let { Calc.perplexityFromLoss(it) }

        return ImportedLog(
            trainLosses = train,
            evalLosses = eval,
            lastStep = lastStep,
            lastTrainLoss = lastTrain,
            lastEvalLoss = lastEval,
            perplexity = ppl,
            rawPointCount = train.size + eval.size
        )
    }

    fun seriesToText(series: List<Double>): String =
        series.joinToString("\n") { "%.6f".format(it).trimEnd('0').trimEnd('.') }
}
