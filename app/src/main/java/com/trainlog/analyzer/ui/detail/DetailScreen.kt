package com.trainlog.analyzer.ui.detail

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
import com.trainlog.analyzer.util.PdfExporter
import com.trainlog.analyzer.viewmodel.TrainingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: TrainingViewModel,
    runId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    var run by remember { mutableStateOf<TrainingRun?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(runId) {
        run = viewModel.getRun(runId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(run?.name?.ifBlank { "Detail" } ?: "Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val current = run
                            if (current != null) {
                                try {
                                    PdfExporter.exportAndShare(context, current)
                                } catch (e: Exception) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Gagal export PDF: ${e.message ?: "unknown"}"
                                        )
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Export PDF")
                    }
                    IconButton(onClick = { onEdit(runId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus")
                    }
                }
            )
        }
    ) { padding ->
        val current = run
        if (current == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("Memuat...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (current.isPlateau) "Status: Plateau" else "Status: Masih belajar",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (current.isPlateau) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text("${current.date} • ${current.totalSteps} steps")
                if (current.resumeFrom.isNotBlank()) {
                    Text("Resume from: ${current.resumeFrom}")
                }

                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                SectionHeader("Learning Curve")
                InfoRow("Train Loss", current.finalTrainLoss)
                InfoRow("Eval Loss", current.finalEvalLoss)
                InfoRow("Best Eval Loss", current.bestEvalLoss)
                InfoRow(
                    "Rel. Improvement",
                    current.relativeImprovement.let { if (it.isNotBlank()) "$it%" else "-" }
                )
                InfoRow("Train–Eval Gap", current.trainEvalGap)
                InfoRow("Noise Level", current.noiseLevel)

                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                SectionHeader("Downstream")
                InfoRow("Perplexity", current.perplexity)
                if (current.task1Name.isNotBlank()) {
                    InfoRow(current.task1Name, current.task1Result)
                }
                if (current.task2Name.isNotBlank()) {
                    InfoRow(current.task2Name, current.task2Result)
                }
                InfoRow(
                    "Catastrophic Forgetting",
                    if (current.hasForgetting) "Ada degradasi" else "Aman"
                )

                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                SectionHeader("Diagnosis")
                val diagnoses = buildList {
                    if (current.diagnosisFlat) add("Train & Eval flat")
                    if (current.diagnosisOverfit) add("Overfitting")
                    if (current.diagnosisStillLearning) add("Masih ada ruang belajar")
                    if (current.diagnosisNoisy) add("Sangat noisy")
                    if (current.diagnosisDownstreamBad) add("Downstream jelek")
                }
                if (diagnoses.isEmpty()) {
                    Text("-", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    diagnoses.forEach { Text("• $it") }
                }
                if (current.rootCause.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Akar masalah:", fontWeight = FontWeight.Medium)
                    Text(current.rootCause)
                }

                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                SectionHeader("Keputusan")
                Text(
                    text = current.decision.ifBlank { "-" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (current.finalCheckpoint.isNotBlank()) {
                    InfoRow("Checkpoint", current.finalCheckpoint)
                }

                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                SectionHeader("Next Experiment Plan")
                if (current.hypothesis.isNotBlank()) {
                    Text("Hipotesis:", fontWeight = FontWeight.Medium)
                    Text(current.hypothesis)
                    Spacer(Modifier.height(8.dp))
                }
                if (current.whatChanged.isNotBlank()) {
                    Text("Yang diubah:", fontWeight = FontWeight.Medium)
                    Text(current.whatChanged)
                    Spacer(Modifier.height(8.dp))
                }
                InfoRow("Priority", current.priority)

                if (current.conclusion.isNotBlank()) {
                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                    SectionHeader("Kesimpulan")
                    Text(current.conclusion)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showDeleteDialog && run != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Run?") },
            text = { Text("Data analisis \"${run!!.name}\" akan dihapus permanen.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRun(run!!)
                        showDeleteDialog = false
                        onBack()
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.45f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.55f)
        )
    }
}
