package com.trainlog.analyzer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trainlog.analyzer.data.db.AppDatabase
import com.trainlog.analyzer.data.model.TrainingRun
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrainingViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).trainingRunDao()

    val allRuns = dao.getAllRuns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val existing = dao.getAllRunsSnapshot()
            if (existing.isEmpty()) {
                sampleRuns().forEach { dao.insert(it) }
            }
        }
    }

    fun saveRun(run: TrainingRun, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = dao.insert(run)
            onSaved(id)
        }
    }

    fun updateRun(run: TrainingRun) {
        viewModelScope.launch { dao.update(run) }
    }

    fun deleteRun(run: TrainingRun) {
        viewModelScope.launch { dao.delete(run) }
    }

    suspend fun getRun(id: Long): TrainingRun? = dao.getRunById(id)

    private fun sampleRuns(): List<TrainingRun> = listOf(
        TrainingRun(
            name = "Qwen Arabic GaLore",
            date = "21 Agu 2026",
            totalSteps = "8685 / 9066",
            resumeFrom = "checkpoint-7400",
            finalTrainLoss = "2.115",
            finalEvalLoss = "2.121",
            bestEvalLoss = "2.121",
            relativeImprovement = "0.15",
            isPlateau = true,
            trainEvalGap = "0.006",
            noiseLevel = "Rendah",
            perplexity = "8.34",
            diagnosisFlat = true,
            rootCause = "Capacity / LR sudah sangat kecil (~2e-6).",
            decision = "Stop & pakai checkpoint terbaik",
            finalCheckpoint = "checkpoint-8600",
            hypothesis = "Perlu data lebih bersih atau naikkan kapasitas.",
            whatChanged = "Evaluasi downstream + data mixture",
            priority = "Tinggi",
            conclusion = "Loss sudah plateau jelas. Melanjutkan training tidak efisien."
        ),
        TrainingRun(
            name = "Gemma3 Arabic Tunix",
            date = "21 Agu 2026",
            totalSteps = "553102",
            resumeFrom = "470000",
            finalTrainLoss = "1.840",
            relativeImprovement = "0.20",
            isPlateau = true,
            noiseLevel = "Tinggi",
            diagnosisFlat = true,
            diagnosisNoisy = true,
            rootCause = "Plateau noisy. Mean early vs late hanya +0.20%.",
            decision = "Stop & pakai checkpoint terbaik",
            finalCheckpoint = "step-550000",
            hypothesis = "LR restart atau packing bisa diuji di run baru.",
            whatChanged = "Evaluasi downstream",
            priority = "Tinggi",
            conclusion = "82k step terakhir hampir datar."
        )
    )
}
