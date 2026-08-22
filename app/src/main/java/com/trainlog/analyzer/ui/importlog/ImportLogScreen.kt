package com.trainlog.analyzer.ui.importlog

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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trainlog.analyzer.data.model.TrainingRun
import com.trainlog.analyzer.ui.components.LossChart
import com.trainlog.analyzer.util.Calc
import com.trainlog.analyzer.util.LogImporter
import com.trainlog.analyzer.util.PlateauAlert
import com.trainlog.analyzer.viewmodel.TrainingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportLogScreen(
    viewModel: TrainingViewModel,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var raw by remember { mutableStateOf("") }
    var window by remember { mutableStateOf("20") }
    var thr by remember { mutableStateOf("0.5") }
    var secPerStep by remember { mutableStateOf("") }
    val context = LocalContext.current

    val imported = remember(raw) { if (raw.isBlank()) null else LogImporter.import(raw) }
    val w = Calc.parseNum(window)?.toInt() ?: 20
    val threshold = Calc.parseNum(thr) ?: 0.5
    val plateau = imported?.trainLosses?.let { Calc.plateauCheck(it, minOf(w, maxOf(1, it.size - 1)), threshold) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import log") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            Text(
                "Tempel log HuggingFace Trainer / baris train/loss / deret angka. " +
                    "App akan extract loss, hitung plateau, dan buat run baru.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama run") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = raw,
                onValueChange = { raw = it },
                label = { Text("Isi log") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 10
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = window,
                    onValueChange = { window = it },
                    label = { Text("Window N") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = thr,
                    onValueChange = { thr = it },
                    label = { Text("Ambang %") },
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = secPerStep,
                onValueChange = { secPerStep = it },
                label = { Text("Detik / step (opsional, untuk ETA)") },
                modifier = Modifier.fillMaxWidth()
            )

            imported?.let { imp ->
                Text(
                    "${imp.trainLosses.size} train pts · ${imp.evalLosses.size} eval pts" +
                        (imp.lastStep?.let { " · last step $it" } ?: ""),
                    fontWeight = FontWeight.Medium
                )
                LossChart(
                    train = imp.trainLosses,
                    eval = imp.evalLosses,
                    plateauFromIndex = plateau?.let { imp.trainLosses.size - 1 - it.window }
                )
                plateau?.let { p ->
                    Text(
                        "Rel. improvement ${"%+.3f".format(p.relPct)}% → " +
                            if (p.isPlateau) "PLATEAU" else "masih turun",
                        color = if (p.isPlateau) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    "Train ${imp.lastTrainLoss?.let { "%.4f".format(it) } ?: "—"} · " +
                        "Eval ${imp.lastEvalLoss?.let { "%.4f".format(it) } ?: "—"} · " +
                        "PPL ${imp.perplexity?.let { "%.2f".format(it) } ?: "—"}"
                )
            }

            Button(
                onClick = {
                    val imp = imported ?: return@Button
                    val p = plateau
                    val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                    val run = TrainingRun(
                        name = name.ifBlank { "Imported run" },
                        date = date,
                        totalSteps = imp.lastStep?.toString() ?: imp.trainLosses.size.toString(),
                        finalTrainLoss = imp.lastTrainLoss?.let { "%.4f".format(it) } ?: "",
                        finalEvalLoss = imp.lastEvalLoss?.let { "%.4f".format(it) } ?: "",
                        bestEvalLoss = imp.evalLosses.minOrNull()?.let { "%.4f".format(it) } ?: "",
                        relativeImprovement = p?.relPct?.let { "%.3f".format(it) } ?: "",
                        isPlateau = p?.isPlateau == true,
                        trainEvalGap = if (imp.lastTrainLoss != null && imp.lastEvalLoss != null)
                            "%.4f".format(imp.lastEvalLoss - imp.lastTrainLoss) else "",
                        perplexity = imp.perplexity?.let { "%.3f".format(it) } ?: "",
                        lossSeries = LogImporter.seriesToText(imp.trainLosses),
                        evalSeries = LogImporter.seriesToText(imp.evalLosses),
                        secPerStep = secPerStep,
                        plateauAlert = p?.isPlateau == true,
                        diagnosisFlat = p?.isPlateau == true,
                        decision = if (p?.isPlateau == true) "Stop & pakai checkpoint terbaik" else "Lanjut training"
                    )
                    if (p?.isPlateau == true) {
                        PlateauAlert.notifyIfNeeded(
                            context,
                            run.name,
                            true,
                            p.relPct,
                            threshold
                        )
                    }
                    viewModel.saveRun(run) { id -> onSaved(id) }
                },
                enabled = imported != null && imported.trainLosses.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Buat run dari log")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
