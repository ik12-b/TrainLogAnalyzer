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
    val hasForgetting: Boolean = false,
    val diagnosisFlat: Boolean = false,
    val diagnosisOverfit: Boolean = false,
    val diagnosisStillLearning: Boolean = false,
    val diagnosisNoisy: Boolean = false,
    val diagnosisDownstreamBad: Boolean = false,
    val rootCause: String = "",
    val decision: String = "",
    val finalCheckpoint: String = "",
    val hypothesis: String = "",
    val whatChanged: String = "",
    val priority: String = "Sedang",
    val conclusion: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
