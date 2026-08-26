package com.trainlog.analyzer.ui.detail

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trainlog.analyzer.data.model.TrainingRun
import com.trainlog.analyzer.ui.components.LossChartCard
import com.trainlog.analyzer.util.Calc
import com.trainlog.analyzer.util.PdfExporter
import com.trainlog.analyzer.util.ReportExporter
import com.trainlog.analyzer.viewmodel.TrainingViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: TrainingViewModel,
    runId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    var run by remember { mutableStateOf<TrainingRun?>(null) }
    var showDelete by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(runId) {
        run = viewModel.getRun(runId)
    }

    val current = run
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(current?.name?.ifBlank { "Run" } ?: "Run") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val r = run ?: return@IconButton
                        try {
                            PdfExporter.exportAndShare(context, r)
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar("PDF gagal: ${e.message}")
                            }
                        }
                    }) {
                        Icon(Icons.Default.Share, "PDF")
                    }
                    IconButton(onClick = {
                        val r = run ?: return@IconButton
                        val md = ReportExporter.markdown(r)
                        val f = File(context.cacheDir, "TrainLog_${r.name.replace(Regex("[^a-zA-Z0-9_-]"), "_")}.md")
                        f.writeText(md)
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context, "${context.packageName}.fileprovider", f
                        )
                        context.startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/markdown"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_TEXT, md)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                },
                                "Export Markdown"
                            )
                        )
                    }) {
                        Icon(Icons.Default.Share, "MD")
                    }
                    IconButton(onClick = { onEdit(runId) }) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    IconButton(onClick = { showDelete = true }) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (current == null) {
            Text("Loading…", Modifier.padding(padding).padding(16.dp))
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StatusLine(current)
            Section("1. Identitas")
            KV("Tanggal", current.date)
            KV("Git", current.gitCommit)
            KV("Seed", current.seed)
            KV("Host", current.hostname)
            KV("Hardware", current.hardware)
            KV("Framework", current.frameworkVersions)
            KV("Parent", current.parentRunName)
            if (current.configYaml.isNotBlank()) Text(current.configYaml, style = MaterialTheme.typography.bodySmall)

            Section("2. Model")
            KV("Preset", current.architecturePreset)
            KV("Layers / d / heads", listOf(current.numLayers, current.embedDim, current.numHeads).filter { it.isNotBlank() }.joinToString(" / "))
            KV("Vocab", current.vocabSize)
            KV("Params", current.totalParams + if (current.trainableParams.isNotBlank()) " (train ${current.trainableParams})" else "")
            KV("Precision / attn", "${current.precision} / ${current.attnImpl}")
            KV("Resume", current.resumeFrom)
            KV("GaLore / LoRA", listOfNotNull(
                current.galoreRank.takeIf { it.isNotBlank() }?.let { "rank $it" },
                current.galoreGap.takeIf { it.isNotBlank() }?.let { "gap $it" },
                current.loraRank.takeIf { it.isNotBlank() }?.let { "lora $it" }
            ).joinToString(", "))
            if (current.initNotes.isNotBlank()) Text(current.initNotes, style = MaterialTheme.typography.bodySmall)

            Section("3. Data")
            KV("Sources", current.dataSources)
            KV("Seq / batch", "seq ${current.seqLen} · global ${current.globalBatch} · micro ${current.microBatch} × accum ${current.gradAccum} · ${current.numGpus} GPU")
            KV("Packing", current.packingEfficiency)
            KV("Tokens", current.tokensEstimate)
            KV("Mixture", current.mixtureWeights)
            KV("Per domain", current.tokensPerDomain)
            KV("Held-out", current.heldOutSize)
            KV("Epoch eq.", current.epochEquivalent)

            Section("4. Optim")
            KV("Optimizer", current.optimizer)
            KV("LR", "${current.lrMax} → ${current.lrMin} (warmup ${current.warmupSteps})")
            KV("Schedule", current.scheduleType)
            KV("WD / clip", "${current.weightDecay} / ${current.gradClip}")
            KV("Last LR / grad", "${current.lastLr} / ${current.lastGradNorm}")

            Section("5. Learning curve")
            KV("Steps", current.totalSteps)
            KV("Train / Eval / Best", "${current.finalTrainLoss} / ${current.finalEvalLoss} / ${current.bestEvalLoss}")
            KV("Rel. improvement", current.relativeImprovement.let { if (it.isBlank()) "—" else "$it %" })
            KV("Gap / PPL / Noise", "${current.trainEvalGap} / ${current.perplexity} / ${current.noiseLevel}")
            val trainSeries = if (current.lossSeries.isNotBlank()) Calc.parseSeries(current.lossSeries) else emptyList()
            val evalSeries = if (current.evalSeries.isNotBlank()) Calc.parseSeries(current.evalSeries) else emptyList()
            if (trainSeries.isNotEmpty() || evalSeries.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                LossChartCard(title = "Kurva loss", train = trainSeries, eval = evalSeries)
            }

            Section("6. Compute")
            KV("Sec/step", current.secPerStep)
            KV("Tok/s", current.tokensPerSec)
            KV("FLOPs", current.flopsEstimate)
            KV("MFU", current.mfuPercent.let { if (it.isBlank()) "—" else "$it %" })
            KV("Wall / GPU-h", "${current.wallClockHours} h / ${current.gpuHours} GPU-h")
            KV("Cost", current.estimatedCost)

            Section("7. Downstream")
            if (current.task1Name.isNotBlank()) KV(current.task1Name, current.task1Result)
            if (current.task2Name.isNotBlank()) KV(current.task2Name, current.task2Result)
            if (current.task3Name.isNotBlank()) KV(current.task3Name, current.task3Result)
            KV("Forgetting", if (current.hasForgetting) "Ya — ${current.forgettingNote}" else "Aman")
            KV("Harness", current.evalHarness)
            if (current.sampleNotes.isNotBlank()) Text(current.sampleNotes, style = MaterialTheme.typography.bodySmall)

            Section("8. Checkpoint")
            KV("Final", current.finalCheckpoint)
            KV("Best", current.bestCheckpoint)
            if (current.checkpointNotes.isNotBlank()) Text(current.checkpointNotes, style = MaterialTheme.typography.bodySmall)

            Section("9. Keputusan")
            val diags = listOfNotNull(
                current.diagnosisFlat.takeIf { it }?.let { "Flat" },
                current.diagnosisOverfit.takeIf { it }?.let { "Overfit" },
                current.diagnosisStillLearning.takeIf { it }?.let { "Masih belajar" },
                current.diagnosisNoisy.takeIf { it }?.let { "Noisy" },
                current.diagnosisDownstreamBad.takeIf { it }?.let { "Downstream jelek" }
            )
            KV("Diagnosis", diags.joinToString(", ").ifBlank { "—" })
            KV("Root cause", current.rootCause)
            KV("Hipotesis", current.hypothesis)
            KV("Perubahan", current.whatChanged)
            KV("Keputusan", current.decision)
            KV("Priority", current.priority)
            if (current.conclusion.isNotBlank()) {
                Text(current.conclusion, style = MaterialTheme.typography.bodyMedium)
            }

            Section("10. Failures")
            Text(current.failureNotes.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showDelete && current != null) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Hapus run?") },
            text = { Text("Hapus \"${current.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRun(current)
                    showDelete = false
                    onBack()
                }) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun StatusLine(r: TrainingRun) {
    Text(
        if (r.isPlateau) "Status: PLATEAU" else "Status: training / review",
        fontWeight = FontWeight.Bold,
        color = if (r.isPlateau) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun Section(title: String) {
    Spacer(Modifier.height(8.dp))
    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    HorizontalDivider(Modifier.padding(vertical = 4.dp))
}

@Composable
private fun KV(label: String, value: String) {
    if (value.isBlank()) return
    Row(Modifier.fillMaxWidth()) {
        Text(label, Modifier.weight(0.4f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, Modifier.weight(0.6f), style = MaterialTheme.typography.bodySmall)
    }
}
