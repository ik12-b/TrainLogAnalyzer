package com.trainlog.analyzer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_runs")
data class TrainingRun(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val date: String = "",
    val totalSteps: String = "",
    val resumeFrom: String = "",
    val finalTrainLoss: String = "",
    val finalEvalLoss: String = "",
    val bestEvalLoss: String = "",
    val relativeImprovement: String = "",
    val isPlateau: Boolean = false,
    val trainEvalGap: String = "",
    val noiseLevel: String = "Sedang",
    val perplexity: String = "",
    val task1Name: String = "",
    val task1Result: String = "",
    val task2Name: String = "",
    val task2Result: String = "",
    val task3Name: String = "",
    val task3Result: String = "",
    val hasForgetting: Boolean = false,
    val forgettingNote: String = "",
    val diagnosisFlat: Boolean = false,
    val diagnosisOverfit: Boolean = false,
    val diagnosisStillLearning: Boolean = false,
    val diagnosisNoisy: Boolean = false,
    val diagnosisDownstreamBad: Boolean = false,
    val rootCause: String = "",
    val decision: String = "",
    val finalCheckpoint: String = "",
    val bestCheckpoint: String = "",
    val checkpointNotes: String = "", // "step:loss;step:loss"
    val hypothesis: String = "",
    val whatChanged: String = "",
    val priority: String = "Sedang",
    val conclusion: String = "",
    val parentRunId: Long? = null,
    val parentRunName: String = "",
    val architecturePreset: String = "",
    val lossSeries: String = "", // newline-separated train losses
    val evalSeries: String = "",
    val secPerStep: String = "",
    val gpuHourlyCost: String = "",
    val tokensEstimate: String = "",
    val flopsEstimate: String = "",
    val plateauAlert: Boolean = false,
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
        Preset("7B dense", "7e9", "2048", "micro 1–2, accum tinggi", "CPT umum"),
        Preset("13B dense", "13e9", "2048", "micro 1, accum tinggi", "Butuh multi-GPU"),
        Preset("0.5B / 500M", "5e8", "1024", "micro 8–16", "Eksperimen cepat"),
        Preset("1–2B", "1.5e9", "2048", "micro 2–4", "Sweet spot lab kecil"),
        Preset("GaLore low-rank", "—", "1024", "rank 128 gap 200–400", "Hemat memori, throughput turun"),
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
