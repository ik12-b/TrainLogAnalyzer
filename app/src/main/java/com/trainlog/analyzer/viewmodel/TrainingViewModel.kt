package com.trainlog.analyzer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trainlog.analyzer.data.db.AppDatabase
import com.trainlog.analyzer.data.model.TrainingRun
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrainingViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.getDatabase(app).trainingRunDao()

    val allRuns = dao.getAllRuns().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (dao.count() == 0) {
                sampleRuns().forEach { dao.insert(it) }
            }
        }
    }

    fun saveRun(run: TrainingRun, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = dao.insert(run)
            launch(Dispatchers.Main) { onSaved(id) }
        }
    }

    fun updateRun(run: TrainingRun) {
        viewModelScope.launch(Dispatchers.IO) { dao.update(run) }
    }

    fun deleteRun(run: TrainingRun) {
        viewModelScope.launch(Dispatchers.IO) { dao.delete(run) }
    }

    suspend fun getRun(id: Long): TrainingRun? = dao.getRunById(id)

    private fun sampleRuns(): List<TrainingRun> = listOf(
        TrainingRun(
            name = "Qwen Arabic GaLore",
            date = "21 Aug 2026",
            hardware = "2× Tesla T4",
            frameworkVersions = "torch + galore_torch",
            architecturePreset = "0.5B / 500M",
            totalParams = "385277184",
            trainableParams = "385277184",
            precision = "bf16",
            attnImpl = "sdpa",
            resumeFrom = "checkpoint-7400",
            galoreRank = "128",
            galoreGap = "400",
            seqLen = "1024",
            globalBatch = "128",
            microBatch = "16",
            gradAccum = "4",
            numGpus = "2",
            optimizer = "GaLore AdamW",
            lrMax = "3e-4",
            scheduleType = "cosine",
            totalSteps = "8685 / 9066",
            finalTrainLoss = "2.115",
            relativeImprovement = "-0.24",
            isPlateau = true,
            diagnosisFlat = true,
            perplexity = "8.290",
            secPerStep = "32.76",
            tokensEstimate = "~1.1B",
            decision = "Stop & pakai checkpoint terbaik",
            hypothesis = "GaLore rank 128 cukup untuk CPT Arabic ~0.4B",
            whatChanged = "Resume dari ckpt-7400, packed data",
            failureNotes = "missing keys: lm_head.weight (tied?)",
            priority = "Tinggi",
            conclusion = "Train loss datar di ~2.11; eval downstream diperlukan sebelum lanjut."
        ),
        TrainingRun(
            name = "Gemma3 Arabic Tunix",
            date = "22 Aug 2026",
            hardware = "8× TPU",
            frameworkVersions = "jax + tunix",
            architecturePreset = "Custom",
            numLayers = "26",
            embedDim = "1152",
            numHeads = "4",
            vocabSize = "38521",
            precision = "bf16",
            resumeFrom = "pruned + <doc_sep>",
            initNotes = "vocab 38519→38521, token <doc_sep>",
            seqLen = "1024",
            globalBatch = "64",
            numGpus = "8",
            totalSteps = "553102",
            finalTrainLoss = "1.9185",
            noiseLevel = "Tinggi",
            diagnosisNoisy = true,
            diagnosisStillLearning = true,
            perplexity = "6.811",
            decision = "Lanjut + monitor plateau window 5k step",
            hypothesis = "CPT Arabic pada Gemma3 1B-class dengan Tunix FSDP",
            priority = "Sedang",
            conclusion = "Loss noisy di ~1.7–2.0; butuh eval held-out & downstream."
        )
    )
}
