package com.trainlog.analyzer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Full researcher lab notebook for one training run.
 * Covers: identity, model, data, optim, curve, compute, eval, ckpt, decision, failures.
 */
@Entity(tableName = "training_runs")
data class TrainingRun(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    // ——— 1. Identity / reproducibility ———
    val name: String = "",
    val date: String = "",
    val gitCommit: String = "",
    val seed: String = "",
    val hostname: String = "",
    val hardware: String = "",          // e.g. "2x T4" / "8x TPU v5e"
    val frameworkVersions: String = "", // torch/jax/transformers
    val configYaml: String = "",
    val parentRunId: Long? = null,
    val parentRunName: String = "",

    // ——— 2. Model & init ———
    val architecturePreset: String = "",
    val numLayers: String = "",
    val embedDim: String = "",
    val numHeads: String = "",
    val vocabSize: String = "",
    val totalParams: String = "",
    val trainableParams: String = "",
    val precision: String = "bf16",
    val attnImpl: String = "",
    val resumeFrom: String = "",
    val initNotes: String = "",         // vocab resize, missing keys, etc.
    val galoreRank: String = "",
    val galoreGap: String = "",
    val loraRank: String = "",

    // ——— 3. Data ———
    val dataSources: String = "",
    val seqLen: String = "",
    val globalBatch: String = "",
    val microBatch: String = "",
    val gradAccum: String = "",
    val numGpus: String = "",
    val packingEfficiency: String = "",
    val tokensEstimate: String = "",
    val tokensPerDomain: String = "",   // "news:40B, web:30B"
    val mixtureWeights: String = "",
    val heldOutSize: String = "",
    val corpusUniqueTokens: String = "",
    val epochEquivalent: String = "",

    // ——— 4. Optim & schedule ———
    val optimizer: String = "AdamW",
    val lrMax: String = "",
    val lrMin: String = "",
    val warmupSteps: String = "",
    val scheduleType: String = "cosine", // cosine / linear / wsd / restarts
    val weightDecay: String = "",
    val gradClip: String = "",
    val lastLr: String = "",
    val lastGradNorm: String = "",

    // ——— 5. Learning curve ———
    val totalSteps: String = "",
    val finalTrainLoss: String = "",
    val finalEvalLoss: String = "",
    val bestEvalLoss: String = "",
    val relativeImprovement: String = "",
    val isPlateau: Boolean = false,
    val trainEvalGap: String = "",
    val noiseLevel: String = "Sedang",
    val perplexity: String = "",
    val lossSeries: String = "",
    val evalSeries: String = "",
    val plateauAlert: Boolean = false,

    // ——— 6. Compute budget ———
    val flopsEstimate: String = "",
    val secPerStep: String = "",
    val tokensPerSec: String = "",
    val mfuPercent: String = "",
    val wallClockHours: String = "",
    val gpuHours: String = "",
    val estimatedCost: String = "",
    val peakFlopsDevice: String = "",   // theoretical peak for MFU

    // ——— 7. Downstream eval ———
    val task1Name: String = "",
    val task1Result: String = "",
    val task2Name: String = "",
    val task2Result: String = "",
    val task3Name: String = "",
    val task3Result: String = "",
    val hasForgetting: Boolean = false,
    val forgettingNote: String = "",
    val evalHarness: String = "",
    val sampleNotes: String = "",

    // ——— 8. Checkpoints ———
    val finalCheckpoint: String = "",
    val bestCheckpoint: String = "",
    val checkpointNotes: String = "",

    // ——— 9. Diagnosis & decision ———
    val diagnosisFlat: Boolean = false,
    val diagnosisOverfit: Boolean = false,
    val diagnosisStillLearning: Boolean = false,
    val diagnosisNoisy: Boolean = false,
    val diagnosisDownstreamBad: Boolean = false,
    val rootCause: String = "",
    val decision: String = "",
    val hypothesis: String = "",
    val whatChanged: String = "",
    val priority: String = "Sedang",
    val conclusion: String = "",

    // ——— 10. Failures / notes ———
    val failureNotes: String = "",      // NaN, OOM, missing keys, slow save
    val createdAt: Long = System.currentTimeMillis()
)

object ArchPresets {
    data class Preset(
        val name: String,
        val params: String,
        val seq: String,
        val batchHint: String,
        val notes: String
    )

    val ALL = listOf(
        Preset("0.5B / 500M", "5e8", "1024", "micro 8–16", "Eksperimen cepat"),
        Preset("1–2B", "1.5e9", "2048", "micro 2–4", "Sweet spot lab kecil"),
        Preset("7B dense", "7e9", "2048", "micro 1–2, accum tinggi", "CPT umum"),
        Preset("13B dense", "13e9", "2048", "micro 1, accum tinggi", "Multi-GPU"),
        Preset("GaLore low-rank", "—", "1024", "rank 128 gap 200–400", "Hemat memori"),
        Preset("Custom", "", "1024", "", "Isi manual")
    )
}

object DownstreamTemplates {
    val TASKS = listOf(
        "Held-out perplexity",
        "ArabicQA / open QA",
        "Translation (AR↔EN)",
        "Summarization",
        "Instruction following",
        "Code completion",
        "Base model probe (forgetting)"
    )
}

object ScheduleTypes {
    val ALL = listOf("cosine", "linear", "wsd", "restarts", "constant")
}

object PrecisionOptions {
    val ALL = listOf("bf16", "fp16", "fp32", "fp8")
}
