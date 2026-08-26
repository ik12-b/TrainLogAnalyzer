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
import com.trainlog.analyzer.ui.components.LossChartCard
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
    var window by remember { mutableStateOf("50") }
    var thr by remember { mutableStateOf("0.5") }
    val context = LocalContext.current

    val imported = remember(raw) { if (raw.isBlank()) null else LogImporter.import(raw) }
    val w = Calc.parseNum(window)?.toInt() ?: 50
    val threshold = Calc.parseNum(thr) ?: 0.5
    val plateau = imported?.trainLosses?.let {
        if (it.size < 2) null
        else Calc.plateauCheck(it, minOf(w, maxOf(1, it.size - 1)), threshold)
    }
    val tokens = imported?.let { LogImporter.estimateTokens(it) }
    val flops = imported?.let { LogImporter.estimateFlops(it) }

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
                "Tempel log Qwen GaLore (HF Trainer) atau Gemma3 Tunix ([loss-monitor]). " +
                    "App mengekstrak loss, step, LR, batch, params, detik/step, lalu deteksi plateau.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama run") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("mis. Gemma3 Arabic / Qwen GaLore") }
            )
            OutlinedTextField(
                value = raw,
                onValueChange = { raw = it },
                label = { Text("Isi log (paste penuh atau cuplikan)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 12
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

            imported?.let { imp ->
                Text("Sumber: ${imp.sourceHint}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Text(
                    "${imp.trainLosses.size} train pts · ${imp.evalLosses.size} eval pts" +
                        (imp.lastStep?.let { " · step $it" } ?: "") +
                        (imp.totalSteps?.let { "/$it" } ?: "")
                )
                LossChartCard(
                    title = "Kurva loss (import)",
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
                    "Loss ${imp.lastTrainLoss?.let { "%.4f".format(it) } ?: "—"} · " +
                        "Eval ${imp.lastEvalLoss?.let { "%.4f".format(it) } ?: "—"} · " +
                        "PPL ${imp.perplexity?.let { "%.2f".format(it) } ?: "—"}"
                )
                Text(
                    "LR ${imp.lastLr?.let { "%.3e".format(it) } ?: "—"} · " +
                        "grad ${imp.lastGradNorm?.let { "%.3f".format(it) } ?: "—"} · " +
                        "epoch ${imp.lastEpoch?.let { "%.3f".format(it) } ?: "—"}"
                )
                Text(
                    "batch ${imp.globalBatch ?: "—"} · seq ${imp.seqLen ?: "—"} · " +
                        "params ${imp.params?.let { "%,d".format(it) } ?: "—"} · " +
                        "s/it ${imp.secPerStepEstimate?.let { "%.2f".format(it) } ?: "—"}"
                )
                if (tokens != null) {
                    Text("Tokens est. ${Calc.formatTokens(tokens)} · FLOPs ${flops?.let { Calc.formatFlops(it) } ?: "—"}")
                }
                imp.resumeFrom?.let {
                    Text("Resume: ${it.takeLast(48)}", style = MaterialTheme.typography.bodySmall)
                }
            }

            Button(
                onClick = {
                    val imp = imported ?: return@Button
                    val p = plateau
                    val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                    val stepLabel = when {
                        imp.lastStep != null && imp.totalSteps != null ->
                            "${imp.lastStep} / ${imp.totalSteps}"
                        imp.lastStep != null -> imp.lastStep.toString()
                        else -> imp.trainLosses.size.toString()
                    }
                    val defaultName = when {
                        name.isNotBlank() -> name
                        imp.sourceHint.contains("Tunix") -> "Gemma3 Tunix"
                        imp.sourceHint.contains("GaLore") || imp.sourceHint.contains("HuggingFace") ->
                            "Qwen GaLore"
                        else -> "Imported run"
                    }
                    val run = TrainingRun(
                        name = defaultName,
                        date = date,
                        totalSteps = stepLabel,
                        resumeFrom = imp.resumeFrom?.substringAfterLast('/') ?: "",
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
                        secPerStep = imp.secPerStepEstimate?.let { "%.2f".format(it) } ?: "",
                        tokensEstimate = tokens?.let { Calc.formatTokens(it) } ?: "",
                        flopsEstimate = flops?.let { Calc.formatFlops(it) } ?: "",
                        plateauAlert = p?.isPlateau == true,
                        diagnosisFlat = p?.isPlateau == true,
                        diagnosisNoisy = imp.trainLosses.size > 10 &&
                            (imp.trainLosses.takeLast(20).let { s ->
                                if (s.isEmpty()) false
                                else (s.maxOrNull()!! - s.minOrNull()!!) > 0.3
                            }),
                        decision = if (p?.isPlateau == true) "Stop & pakai checkpoint terbaik"
                        else "Lanjut training",
                        architecturePreset = when {
                            (imp.params ?: 0) in 300_000_000L..500_000_000L -> "0.5B / 500M"
                            else -> ""
                        },
                        totalParams = imp.params?.toString() ?: "",
                        seqLen = imp.seqLen?.toString() ?: "",
                        globalBatch = imp.globalBatch?.toString() ?: "",
                        galoreRank = imp.galoreRank?.toString() ?: "",
                        lastLr = imp.lastLr?.let { "%.4e".format(it) } ?: "",
                        lastGradNorm = imp.lastGradNorm?.let { "%.4f".format(it) } ?: "",
                        conclusion = buildString {
                            append("Parsed as ${imp.sourceHint}. ")
                            append("${imp.trainLosses.size} loss points. ")
                            p?.let {
                                append(
                                    "Rel improvement ${"%.3f".format(it.relPct)}% over window ${it.window}. "
                                )
                            }
                            imp.lastLr?.let { append("Last LR ${"%.3e".format(it)}. ") }
                        }
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
