package com.trainlog.analyzer.ui.compare

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trainlog.analyzer.data.model.TrainingRun
import com.trainlog.analyzer.ui.components.LossChart
import com.trainlog.analyzer.util.Calc
import com.trainlog.analyzer.viewmodel.TrainingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    viewModel: TrainingViewModel,
    onBack: () -> Unit
) {
    val runs by viewModel.allRuns.collectAsState()
    var idA by remember { mutableStateOf<Long?>(null) }
    var idB by remember { mutableStateOf<Long?>(null) }
    val a = runs.find { it.id == idA }
    val b = runs.find { it.id == idB }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bandingkan runs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Pilih run A", fontWeight = FontWeight.SemiBold)
            RunPicker(runs, idA) { idA = it }
            Text("Pilih run B", fontWeight = FontWeight.SemiBold)
            RunPicker(runs, idB) { idB = it }

            if (a != null && b != null) {
                CompareTable(a, b)
                Text("Loss A", style = MaterialTheme.typography.titleSmall)
                LossChart(train = parseSeries(a.lossSeries))
                Text("Loss B", style = MaterialTheme.typography.titleSmall)
                LossChart(train = parseSeries(b.lossSeries))
            } else {
                Text(
                    "Pilih dua run untuk melihat metrik berdampingan.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RunPicker(runs: List<TrainingRun>, selected: Long?, onSelect: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        runs.forEach { r ->
            FilterChip(
                selected = r.id == selected,
                onClick = { onSelect(r.id) },
                label = { Text(r.name.ifBlank { "Run #${r.id}" }) }
            )
        }
    }
}

@Composable
private fun CompareTable(a: TrainingRun, b: TrainingRun) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text("", Modifier.weight(1.1f))
                Text("A", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("B", Modifier.weight(1f), fontWeight = FontWeight.Bold)
            }
            CompRow("Nama", a.name, b.name)
            CompRow("Steps", a.totalSteps, b.totalSteps)
            CompRow("Train loss", a.finalTrainLoss, b.finalTrainLoss)
            CompRow("Eval loss", a.finalEvalLoss, b.finalEvalLoss)
            CompRow("Rel %", a.relativeImprovement, b.relativeImprovement)
            CompRow("Plateau", yesNo(a.isPlateau), yesNo(b.isPlateau))
            CompRow("PPL", a.perplexity, b.perplexity)
            CompRow("Tokens", a.tokensEstimate, b.tokensEstimate)
            CompRow("FLOPs", a.flopsEstimate, b.flopsEstimate)
            CompRow("Sec/step", a.secPerStep, b.secPerStep)
            CompRow("Decision", a.decision, b.decision)
            CompRow("Best ckpt", a.bestCheckpoint, b.bestCheckpoint)
        }
    }
}

@Composable
private fun CompRow(k: String, va: String, vb: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(k, Modifier.weight(1.1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(va.ifBlank { "—" }, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        Text(vb.ifBlank { "—" }, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
    }
}

private fun yesNo(b: Boolean) = if (b) "Ya" else "Tidak"

private fun parseSeries(s: String): List<Double> =
    if (s.isBlank()) emptyList() else Calc.parseSeries(s)
