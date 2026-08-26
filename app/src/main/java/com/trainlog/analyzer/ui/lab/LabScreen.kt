package com.trainlog.analyzer.ui.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trainlog.analyzer.util.Calc

private enum class LabTab { Curve, Ppl, Batch, Lr, Flops, Schedule, Mixture, Mfu }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabScreen() {
    var tab by remember { mutableStateOf(LabTab.Curve) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Lab kalkulator", fontWeight = FontWeight.SemiBold) })
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
                "Rumus researcher: plateau, PPL, batch, cosine LR, Chinchilla FLOPs",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LabTab.entries.forEach { t ->
                    FilterChip(
                        selected = tab == t,
                        onClick = { tab = t },
                        label = {
                            Text(
                                when (t) {
                                    LabTab.Curve -> "Kurva"
                                    LabTab.Ppl -> "PPL"
                                    LabTab.Batch -> "Batch"
                                    LabTab.Lr -> "LR"
                                    LabTab.Flops -> "FLOPs"
                                    LabTab.Schedule -> "Sched"
                                    LabTab.Mixture -> "Data"
                                    LabTab.Mfu -> "MFU"
                                }
                            )
                        }
                    )
                }
            }
            when (tab) {
                LabTab.Curve -> CurvePanel()
                LabTab.Ppl -> PplPanel()
                LabTab.Batch -> BatchPanel()
                LabTab.Lr -> LrPanel()
                LabTab.Flops -> FlopsPanel()
                LabTab.Schedule -> SchedulePanel()
                LabTab.Mixture -> MixturePanel()
                LabTab.Mfu -> MfuPanel()
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CurvePanel() {
    var raw by remember { mutableStateOf("2.1244\n2.1222\n2.1211") }
    var window by remember { mutableStateOf("2") }
    var thr by remember { mutableStateOf("0.5") }
    var alpha by remember { mutableStateOf("0.1") }
    var totalSteps by remember { mutableStateOf("20000") }

    val series = remember(raw) { Calc.parseTrainerLosses(raw) }
    val w = Calc.parseNum(window)?.toInt() ?: 1
    val t = Calc.parseNum(thr) ?: 0.5
    val a = Calc.parseNum(alpha) ?: 0.1
    val steps = Calc.parseNum(totalSteps) ?: 0.0
    val result = Calc.plateauCheck(series, w, t)
    val smoothed = Calc.ema(series, a)
    val sug = Calc.suggestedWindow(steps)

    LabCard("Deteksi plateau") {
        Text(
            "Tempel deret loss atau log HuggingFace {'loss': ...}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = raw,
            onValueChange = { raw = it },
            label = { Text("Loss series") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = window, onValueChange = { window = it },
                label = { Text("Window N") }, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = thr, onValueChange = { thr = it },
                label = { Text("Ambang %") }, modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = alpha, onValueChange = { alpha = it },
                label = { Text("EMA α") }, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = totalSteps, onValueChange = { totalSteps = it },
                label = { Text("Total steps") }, modifier = Modifier.weight(1f)
            )
        }
        Text("Saran window ≈ 3% steps: $sug", style = MaterialTheme.typography.bodySmall)
        ResultBox("${series.size} titik · EMA akhir ${smoothed.lastOrNull()?.let { "%.4f".format(it) } ?: "—"}")
        if (result == null) {
            Text("Butuh minimal 2 nilai loss.")
        } else {
            ResultBox("L: ${"%.4f".format(result.oldL)} → ${"%.4f".format(result.newL)}")
            ResultBox("Rel. improvement: ${"%+.3f".format(result.relPct)}%")
            ResultBox(
                if (result.isPlateau) "Keputusan: Plateau (< ${result.thresholdPct}%)"
                else "Keputusan: Masih turun signifikan"
            )
        }
    }
}

@Composable
private fun PplPanel() {
    var loss by remember { mutableStateOf("2.121") }
    var ppl by remember { mutableStateOf("") }
    var train by remember { mutableStateOf("2.115") }
    var ev by remember { mutableStateOf("2.121") }
    val L = Calc.parseNum(loss)
    val P = Calc.parseNum(ppl)
    val T = Calc.parseNum(train)
    val E = Calc.parseNum(ev)

    LabCard("Loss ↔ Perplexity") {
        Text("PPL = e^loss (NLL nats)", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(
            value = loss, onValueChange = { loss = it },
            label = { Text("CE / NLL loss") }, modifier = Modifier.fillMaxWidth()
        )
        ResultBox("Perplexity: ${L?.let { "%.3f".format(Calc.perplexityFromLoss(it)) } ?: "—"}")
        OutlinedTextField(
            value = ppl, onValueChange = { ppl = it },
            label = { Text("PPL → loss") }, modifier = Modifier.fillMaxWidth()
        )
        ResultBox("Loss: ${P?.let { Calc.lossFromPerplexity(it)?.let { v -> "%.4f".format(v) } } ?: "—"}")
    }
    LabCard("Train vs eval") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = train, onValueChange = { train = it },
                label = { Text("Train") }, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = ev, onValueChange = { ev = it },
                label = { Text("Eval") }, modifier = Modifier.weight(1f)
            )
        }
        val gap = if (T != null && E != null) Calc.trainEvalGap(T, E) else null
        val rel = if (T != null && E != null) Calc.relativeImprovement(T, E) else null
        ResultBox("Gap: ${gap?.let { "%.4f".format(it) } ?: "—"}")
        ResultBox("Rel: ${rel?.let { "%.3f%%".format(it.second) } ?: "—"}")
    }
}

@Composable
private fun BatchPanel() {
    var micro by remember { mutableStateOf("16") }
    var accum by remember { mutableStateOf("4") }
    var gpus by remember { mutableStateOf("2") }
    var seq by remember { mutableStateOf("1024") }
    var steps by remember { mutableStateOf("8500") }
    var used by remember { mutableStateOf("980") }
    var pad by remember { mutableStateOf("1024") }

    val m = Calc.parseNum(micro) ?: 0.0
    val a = Calc.parseNum(accum) ?: 0.0
    val g = Calc.parseNum(gpus) ?: 0.0
    val s = Calc.parseNum(seq) ?: 0.0
    val st = Calc.parseNum(steps) ?: 0.0
    val gb = Calc.effectiveBatch(m, a, g)
    val tok = Calc.tokensSeen(st, gb, s)
    val eff = Calc.packingEfficiency(Calc.parseNum(used) ?: 0.0, Calc.parseNum(pad) ?: 0.0)

    LabCard("Effective batch & tokens") {
        Text("Global batch = micro × accum × GPU", style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = micro, onValueChange = { micro = it }, label = { Text("Batch/GPU") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = accum, onValueChange = { accum = it }, label = { Text("Accum") }, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = gpus, onValueChange = { gpus = it }, label = { Text("GPU") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = seq, onValueChange = { seq = it }, label = { Text("Seq") }, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(value = steps, onValueChange = { steps = it }, label = { Text("Steps") }, modifier = Modifier.fillMaxWidth())
        ResultBox("Global batch: ${gb.toInt()}")
        ResultBox("Tokens: ${Calc.formatTokens(tok)}")
    }
    LabCard("Packing efficiency") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = used, onValueChange = { used = it }, label = { Text("Used") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = pad, onValueChange = { pad = it }, label = { Text("Padded") }, modifier = Modifier.weight(1f))
        }
        ResultBox("Efisiensi: ${eff?.let { "%.1f%%".format(it * 100) } ?: "—"}")
    }
}

@Composable
private fun LrPanel() {
    var maxLr by remember { mutableStateOf("3e-4") }
    var minLr by remember { mutableStateOf("1e-6") }
    var T by remember { mutableStateOf("9066") }
    var warm by remember { mutableStateOf("100") }
    var tNow by remember { mutableStateOf("8500") }
    var oldLr by remember { mutableStateOf("3e-4") }
    var bOld by remember { mutableStateOf("128") }
    var bNew by remember { mutableStateOf("512") }
    var sqrtMode by remember { mutableStateOf(false) }

    val lr = Calc.cosineLr(
        Calc.parseNum(tNow) ?: 0.0,
        Calc.parseNum(T) ?: 1.0,
        Calc.parseNum(maxLr) ?: 0.0,
        Calc.parseNum(minLr) ?: 0.0,
        Calc.parseNum(warm) ?: 0.0
    )
    val scaled = Calc.scaleLr(
        Calc.parseNum(oldLr) ?: 0.0,
        Calc.parseNum(bOld) ?: 0.0,
        Calc.parseNum(bNew) ?: 0.0,
        sqrtMode
    )

    LabCard("Cosine + warmup") {
        Text("lr(t) setelah warmup linear", style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = maxLr, onValueChange = { maxLr = it }, label = { Text("LR max") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = minLr, onValueChange = { minLr = it }, label = { Text("LR min") }, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = T, onValueChange = { T = it }, label = { Text("Total T") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = warm, onValueChange = { warm = it }, label = { Text("Warmup") }, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(value = tNow, onValueChange = { tNow = it }, label = { Text("Step sekarang") }, modifier = Modifier.fillMaxWidth())
        ResultBox("LR(t): %.3e".format(lr))
    }
    LabCard("Scale LR vs batch") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = oldLr, onValueChange = { oldLr = it }, label = { Text("LR lama") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = bOld, onValueChange = { bOld = it }, label = { Text("Batch lama") }, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(value = bNew, onValueChange = { bNew = it }, label = { Text("Batch baru") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !sqrtMode, onClick = { sqrtMode = false }, label = { Text("Linear") })
            FilterChip(selected = sqrtMode, onClick = { sqrtMode = true }, label = { Text("Sqrt") })
        }
        ResultBox("LR baru: ${scaled?.let { "%.3e".format(it) } ?: "—"}")
    }
}

@Composable
private fun FlopsPanel() {
    var params by remember { mutableStateOf("385e6") }
    var tokens by remember { mutableStateOf("1.1e9") }
    var steps by remember { mutableStateOf("1666") }
    var sec by remember { mutableStateOf("33.6") }
    var remain by remember { mutableStateOf("566") }

    val flops = Calc.trainingFlops(Calc.parseNum(params) ?: 0.0, Calc.parseNum(tokens) ?: 0.0)
    val st = Calc.parseNum(steps) ?: 0.0
    val s = Calc.parseNum(sec) ?: 0.0
    val r = Calc.parseNum(remain) ?: 0.0

    LabCard("Chinchilla FLOPs") {
        Text("≈ 6 × N_params × N_tokens", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(value = params, onValueChange = { params = it }, label = { Text("Parameters") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = tokens, onValueChange = { tokens = it }, label = { Text("Tokens") }, modifier = Modifier.fillMaxWidth())
        ResultBox(Calc.formatFlops(flops))
    }
    LabCard("Estimasi waktu") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = steps, onValueChange = { steps = it }, label = { Text("Steps done") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = sec, onValueChange = { sec = it }, label = { Text("Sec/step") }, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(value = remain, onValueChange = { remain = it }, label = { Text("Sisa steps") }, modifier = Modifier.fillMaxWidth())
        ResultBox("Terpakai: ${Calc.formatDuration(st * s)}")
        ResultBox("ETA sisa: ${Calc.formatDuration(r * s)}")
        ResultBox("Total: ${Calc.formatDuration((st + r) * s)}")
    }
}


@Composable
private fun SchedulePanel() {
    var kind by remember { mutableStateOf("cosine") }
    var maxLr by remember { mutableStateOf("3e-4") }
    var minLr by remember { mutableStateOf("1e-6") }
    var T by remember { mutableStateOf("10000") }
    var warm by remember { mutableStateOf("200") }
    var tNow by remember { mutableStateOf("5000") }
    var decayFrac by remember { mutableStateOf("0.8") }
    var cycle by remember { mutableStateOf("2000") }

    val t = Calc.parseNum(tNow) ?: 0.0
    val tot = Calc.parseNum(T) ?: 1.0
    val mx = Calc.parseNum(maxLr) ?: 0.0
    val mn = Calc.parseNum(minLr) ?: 0.0
    val w = Calc.parseNum(warm) ?: 0.0
    val lr = when (kind) {
        "linear" -> Calc.linearLr(t, tot, mx, mn, w)
        "wsd" -> Calc.wsdLr(t, tot, mx, mn, w, Calc.parseNum(decayFrac) ?: 0.8)
        "restarts" -> Calc.cosineWithRestarts(t, Calc.parseNum(cycle) ?: 2000.0, mx, mn)
        else -> Calc.cosineLr(t, tot, mx, mn, w)
    }

    LabCard("Scheduler playground") {
        Text("Cosine · Linear · WSD · Cosine restarts", style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("cosine", "linear", "wsd", "restarts").forEach { k ->
                FilterChip(selected = kind == k, onClick = { kind = k }, label = { Text(k) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = maxLr, onValueChange = { maxLr = it }, label = { Text("LR max") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = minLr, onValueChange = { minLr = it }, label = { Text("LR min") }, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = T, onValueChange = { T = it }, label = { Text("T") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = warm, onValueChange = { warm = it }, label = { Text("Warmup") }, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(value = tNow, onValueChange = { tNow = it }, label = { Text("Step t") }, modifier = Modifier.fillMaxWidth())
        if (kind == "wsd") {
            OutlinedTextField(value = decayFrac, onValueChange = { decayFrac = it }, label = { Text("Decay start frac") }, modifier = Modifier.fillMaxWidth())
        }
        if (kind == "restarts") {
            OutlinedTextField(value = cycle, onValueChange = { cycle = it }, label = { Text("Cycle length") }, modifier = Modifier.fillMaxWidth())
        }
        ResultBox("LR(t) = %.4e".format(lr))
    }
}

@Composable
private fun MixturePanel() {
    var total by remember { mutableStateOf("1e11") }
    var params by remember { mutableStateOf("7e9") }
    var wNews by remember { mutableStateOf("40") }
    var wWeb by remember { mutableStateOf("30") }
    var wCode by remember { mutableStateOf("15") }
    var wChat by remember { mutableStateOf("15") }

    val weights = listOf(
        Calc.parseNum(wNews) ?: 0.0,
        Calc.parseNum(wWeb) ?: 0.0,
        Calc.parseNum(wCode) ?: 0.0,
        Calc.parseNum(wChat) ?: 0.0
    )
    val tokTotal = Calc.parseNum(total) ?: 0.0
    val toks = Calc.mixtureTokens(weights, tokTotal)
    val ratio = Calc.chinchillaRatio(tokTotal, Calc.parseNum(params) ?: 0.0)
    val labels = listOf("News/domain", "Web", "Code", "Chat/instruct")

    LabCard("Data mixture planner") {
        Text("Bobot relatif → token per sumber. Cek rasio D/N (~20 Chinchilla).", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(value = total, onValueChange = { total = it }, label = { Text("Total tokens D") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = params, onValueChange = { params = it }, label = { Text("Params N") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = wNews, onValueChange = { wNews = it }, label = { Text("News %") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = wWeb, onValueChange = { wWeb = it }, label = { Text("Web %") }, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = wCode, onValueChange = { wCode = it }, label = { Text("Code %") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = wChat, onValueChange = { wChat = it }, label = { Text("Chat %") }, modifier = Modifier.weight(1f))
        }
        labels.zip(toks).forEach { (lab, tk) ->
            ResultBox("$lab: ${Calc.formatTokens(tk)}")
        }
        ResultBox("D/N: ${ratio?.let { "%.1f tokens/param".format(it) } ?: "—"}")
        if (ratio != null) {
            Text(
                when {
                    ratio < 10 -> "Di bawah rezim Chinchilla — model mungkin undertrained."
                    ratio in 10.0..40.0 -> "Mendekati rezim compute-optimal Chinchilla."
                    else -> "Sangat data-heavy; bagus jika data berkualitas."
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


@Composable
private fun MfuPanel() {
    var params by remember { mutableStateOf("7e9") }
    var batch by remember { mutableStateOf("128") }
    var seq by remember { mutableStateOf("2048") }
    var sec by remember { mutableStateOf("1.5") }
    var peak by remember { mutableStateOf("3.12e14") } // ~A100 bf16 rough ballpark example

    val n = Calc.parseNum(params)
    val b = Calc.parseNum(batch)
    val s = Calc.parseNum(seq)
    val sps = Calc.parseNum(sec)
    val pk = Calc.parseNum(peak)
    val tps = if (b != null && s != null && sps != null) Calc.tokensPerSec(b, s, sps) else null
    val ach = if (n != null && tps != null) Calc.achievedFlopsPerSec(n, tps) else null
    val mfu = if (ach != null && pk != null) Calc.mfuPercent(ach, pk) else null
    val wall = if (sps != null) Calc.wallHoursFromSteps(10000.0, sps) else null

    LabCard("MFU & throughput") {
        Text("Achieved FLOPs/s ≈ 6·N·(tokens/s). MFU = achieved / peak device.", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(value = params, onValueChange = { params = it }, label = { Text("Params N") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = batch, onValueChange = { batch = it }, label = { Text("Global batch") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = seq, onValueChange = { seq = it }, label = { Text("Seq") }, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = sec, onValueChange = { sec = it }, label = { Text("Sec/step") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = peak, onValueChange = { peak = it }, label = { Text("Peak FLOPs/s") }, modifier = Modifier.weight(1f))
        }
        ResultBox("Tokens/s: ${tps?.let { "%.1f".format(it) } ?: "—"}")
        ResultBox("Achieved FLOPs/s: ${ach?.let { Calc.formatFlops(it) + "/s" } ?: "—"}")
        ResultBox("MFU: ${mfu?.let { "%.1f %%".format(it) } ?: "—"}")
        ResultBox("Wall for 10k steps: ${wall?.let { "%.2f h".format(it) } ?: "—"}")
        Text("Isi peak sesuai datasheet GPU/TPU (contoh kasar saja).", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun LabCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

@Composable
private fun ResultBox(text: String) {
    Text(
        text,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
            .padding(12.dp),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary
    )
}
