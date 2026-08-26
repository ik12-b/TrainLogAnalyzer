package com.trainlog.analyzer.ui.form

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trainlog.analyzer.data.model.ArchPresets
import com.trainlog.analyzer.data.model.DownstreamTemplates
import com.trainlog.analyzer.data.model.PrecisionOptions
import com.trainlog.analyzer.data.model.ScheduleTypes
import com.trainlog.analyzer.data.model.TrainingRun
import com.trainlog.analyzer.util.Calc
import com.trainlog.analyzer.viewmodel.TrainingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    viewModel: TrainingViewModel,
    runId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    // Identity
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())) }
    var gitCommit by remember { mutableStateOf("") }
    var seed by remember { mutableStateOf("") }
    var hostname by remember { mutableStateOf("") }
    var hardware by remember { mutableStateOf("") }
    var frameworkVersions by remember { mutableStateOf("") }
    var configYaml by remember { mutableStateOf("") }
    var parentRunName by remember { mutableStateOf("") }

    // Model
    var architecturePreset by remember { mutableStateOf("") }
    var numLayers by remember { mutableStateOf("") }
    var embedDim by remember { mutableStateOf("") }
    var numHeads by remember { mutableStateOf("") }
    var vocabSize by remember { mutableStateOf("") }
    var totalParams by remember { mutableStateOf("") }
    var trainableParams by remember { mutableStateOf("") }
    var precision by remember { mutableStateOf("bf16") }
    var attnImpl by remember { mutableStateOf("") }
    var resumeFrom by remember { mutableStateOf("") }
    var initNotes by remember { mutableStateOf("") }
    var galoreRank by remember { mutableStateOf("") }
    var galoreGap by remember { mutableStateOf("") }
    var loraRank by remember { mutableStateOf("") }

    // Data
    var dataSources by remember { mutableStateOf("") }
    var seqLen by remember { mutableStateOf("") }
    var globalBatch by remember { mutableStateOf("") }
    var microBatch by remember { mutableStateOf("") }
    var gradAccum by remember { mutableStateOf("") }
    var numGpus by remember { mutableStateOf("") }
    var packingEfficiency by remember { mutableStateOf("") }
    var tokensEstimate by remember { mutableStateOf("") }
    var tokensPerDomain by remember { mutableStateOf("") }
    var mixtureWeights by remember { mutableStateOf("") }
    var heldOutSize by remember { mutableStateOf("") }
    var corpusUniqueTokens by remember { mutableStateOf("") }
    var epochEquivalent by remember { mutableStateOf("") }

    // Optim
    var optimizer by remember { mutableStateOf("AdamW") }
    var lrMax by remember { mutableStateOf("") }
    var lrMin by remember { mutableStateOf("") }
    var warmupSteps by remember { mutableStateOf("") }
    var scheduleType by remember { mutableStateOf("cosine") }
    var weightDecay by remember { mutableStateOf("") }
    var gradClip by remember { mutableStateOf("") }
    var lastLr by remember { mutableStateOf("") }
    var lastGradNorm by remember { mutableStateOf("") }

    // Curve
    var totalSteps by remember { mutableStateOf("") }
    var finalTrainLoss by remember { mutableStateOf("") }
    var finalEvalLoss by remember { mutableStateOf("") }
    var bestEvalLoss by remember { mutableStateOf("") }
    var relativeImprovement by remember { mutableStateOf("") }
    var isPlateau by remember { mutableStateOf(false) }
    var trainEvalGap by remember { mutableStateOf("") }
    var noiseLevel by remember { mutableStateOf("Sedang") }
    var perplexity by remember { mutableStateOf("") }
    var lossSeries by remember { mutableStateOf("") }
    var evalSeries by remember { mutableStateOf("") }

    // Compute
    var flopsEstimate by remember { mutableStateOf("") }
    var secPerStep by remember { mutableStateOf("") }
    var tokensPerSec by remember { mutableStateOf("") }
    var mfuPercent by remember { mutableStateOf("") }
    var wallClockHours by remember { mutableStateOf("") }
    var gpuHours by remember { mutableStateOf("") }
    var estimatedCost by remember { mutableStateOf("") }
    var peakFlopsDevice by remember { mutableStateOf("") }

    // Eval
    var task1Name by remember { mutableStateOf("") }
    var task1Result by remember { mutableStateOf("") }
    var task2Name by remember { mutableStateOf("") }
    var task2Result by remember { mutableStateOf("") }
    var task3Name by remember { mutableStateOf("") }
    var task3Result by remember { mutableStateOf("") }
    var hasForgetting by remember { mutableStateOf(false) }
    var forgettingNote by remember { mutableStateOf("") }
    var evalHarness by remember { mutableStateOf("") }
    var sampleNotes by remember { mutableStateOf("") }

    // Ckpt
    var finalCheckpoint by remember { mutableStateOf("") }
    var bestCheckpoint by remember { mutableStateOf("") }
    var checkpointNotes by remember { mutableStateOf("") }

    // Diagnosis
    var diagnosisFlat by remember { mutableStateOf(false) }
    var diagnosisOverfit by remember { mutableStateOf(false) }
    var diagnosisStillLearning by remember { mutableStateOf(false) }
    var diagnosisNoisy by remember { mutableStateOf(false) }
    var diagnosisDownstreamBad by remember { mutableStateOf(false) }
    var rootCause by remember { mutableStateOf("") }
    var decision by remember { mutableStateOf("") }
    var hypothesis by remember { mutableStateOf("") }
    var whatChanged by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Sedang") }
    var conclusion by remember { mutableStateOf("") }
    var failureNotes by remember { mutableStateOf("") }

    LaunchedEffect(runId) {
        if (runId != null) {
            val r = viewModel.getRun(runId) ?: return@LaunchedEffect
            name = r.name; date = r.date; gitCommit = r.gitCommit; seed = r.seed
            hostname = r.hostname; hardware = r.hardware; frameworkVersions = r.frameworkVersions
            configYaml = r.configYaml; parentRunName = r.parentRunName
            architecturePreset = r.architecturePreset; numLayers = r.numLayers; embedDim = r.embedDim
            numHeads = r.numHeads; vocabSize = r.vocabSize; totalParams = r.totalParams
            trainableParams = r.trainableParams; precision = r.precision; attnImpl = r.attnImpl
            resumeFrom = r.resumeFrom; initNotes = r.initNotes
            galoreRank = r.galoreRank; galoreGap = r.galoreGap; loraRank = r.loraRank
            dataSources = r.dataSources; seqLen = r.seqLen; globalBatch = r.globalBatch
            microBatch = r.microBatch; gradAccum = r.gradAccum; numGpus = r.numGpus
            packingEfficiency = r.packingEfficiency; tokensEstimate = r.tokensEstimate
            tokensPerDomain = r.tokensPerDomain; mixtureWeights = r.mixtureWeights
            heldOutSize = r.heldOutSize; corpusUniqueTokens = r.corpusUniqueTokens
            epochEquivalent = r.epochEquivalent
            optimizer = r.optimizer; lrMax = r.lrMax; lrMin = r.lrMin; warmupSteps = r.warmupSteps
            scheduleType = r.scheduleType; weightDecay = r.weightDecay; gradClip = r.gradClip
            lastLr = r.lastLr; lastGradNorm = r.lastGradNorm
            totalSteps = r.totalSteps; finalTrainLoss = r.finalTrainLoss; finalEvalLoss = r.finalEvalLoss
            bestEvalLoss = r.bestEvalLoss; relativeImprovement = r.relativeImprovement
            isPlateau = r.isPlateau; trainEvalGap = r.trainEvalGap; noiseLevel = r.noiseLevel
            perplexity = r.perplexity; lossSeries = r.lossSeries; evalSeries = r.evalSeries
            flopsEstimate = r.flopsEstimate; secPerStep = r.secPerStep; tokensPerSec = r.tokensPerSec
            mfuPercent = r.mfuPercent; wallClockHours = r.wallClockHours; gpuHours = r.gpuHours
            estimatedCost = r.estimatedCost; peakFlopsDevice = r.peakFlopsDevice
            task1Name = r.task1Name; task1Result = r.task1Result
            task2Name = r.task2Name; task2Result = r.task2Result
            task3Name = r.task3Name; task3Result = r.task3Result
            hasForgetting = r.hasForgetting; forgettingNote = r.forgettingNote
            evalHarness = r.evalHarness; sampleNotes = r.sampleNotes
            finalCheckpoint = r.finalCheckpoint; bestCheckpoint = r.bestCheckpoint
            checkpointNotes = r.checkpointNotes
            diagnosisFlat = r.diagnosisFlat; diagnosisOverfit = r.diagnosisOverfit
            diagnosisStillLearning = r.diagnosisStillLearning; diagnosisNoisy = r.diagnosisNoisy
            diagnosisDownstreamBad = r.diagnosisDownstreamBad
            rootCause = r.rootCause; decision = r.decision; hypothesis = r.hypothesis
            whatChanged = r.whatChanged; priority = r.priority; conclusion = r.conclusion
            failureNotes = r.failureNotes
        }
    }

    fun autoCompute() {
        val train = Calc.parseNum(finalTrainLoss)
        val eval = Calc.parseNum(finalEvalLoss)
        if (train != null && eval != null) {
            trainEvalGap = "%.4f".format(eval - train)
        }
        val lossForPpl = eval ?: train
        if (lossForPpl != null) perplexity = "%.3f".format(Calc.perplexityFromLoss(lossForPpl))

        val series = Calc.parseSeries(lossSeries)
        if (series.size >= 2) {
            val w = Calc.suggestedWindow(Calc.parseNum(totalSteps) ?: series.size.toDouble())
            Calc.plateauCheck(series, minOf(w, series.size - 1), 0.5)?.let {
                relativeImprovement = "%.3f".format(it.relPct)
                isPlateau = it.isPlateau
                diagnosisFlat = it.isPlateau
            }
        }

        val steps = Calc.parseNum(totalSteps.split("/").first().trim())
        val gb = Calc.parseNum(globalBatch)
        val seq = Calc.parseNum(seqLen)
        val params = Calc.parseNum(totalParams)
        val sps = Calc.parseNum(secPerStep)
        val gpus = Calc.parseNum(numGpus) ?: 1.0
        val peak = Calc.parseNum(peakFlopsDevice)
        val costRate = 0.0 // optional

        if (steps != null && gb != null && seq != null) {
            val tok = Calc.tokensSeen(steps, gb, seq)
            tokensEstimate = Calc.formatTokens(tok)
            if (params != null) flopsEstimate = Calc.formatFlops(Calc.trainingFlops(params, tok))
            val corpus = Calc.parseNum(corpusUniqueTokens)
            if (corpus != null) epochEquivalent = "%.3f".format(tok / corpus)
        }
        if (gb != null && seq != null && sps != null) {
            Calc.tokensPerSec(gb, seq, sps)?.let {
                tokensPerSec = "%.1f".format(it)
                if (params != null) {
                    val ach = Calc.achievedFlopsPerSec(params, it)
                    if (peak != null) {
                        Calc.mfuPercent(ach, peak)?.let { m -> mfuPercent = "%.1f".format(m) }
                    }
                }
            }
        }
        if (steps != null && sps != null) {
            Calc.wallHoursFromSteps(steps, sps)?.let {
                wallClockHours = "%.2f".format(it)
                gpuHours = "%.2f".format(Calc.gpuHours(it, gpus))
            }
        }
        val micro = Calc.parseNum(microBatch)
        val accum = Calc.parseNum(gradAccum)
        if (micro != null && accum != null) {
            val eff = Calc.effectiveBatch(micro, accum, gpus)
            if (globalBatch.isBlank()) globalBatch = "%.0f".format(eff)
        }
    }

    fun buildRun(id: Long = 0) = TrainingRun(
        id = id,
        name = name, date = date, gitCommit = gitCommit, seed = seed,
        hostname = hostname, hardware = hardware, frameworkVersions = frameworkVersions,
        configYaml = configYaml, parentRunName = parentRunName,
        architecturePreset = architecturePreset, numLayers = numLayers, embedDim = embedDim,
        numHeads = numHeads, vocabSize = vocabSize, totalParams = totalParams,
        trainableParams = trainableParams, precision = precision, attnImpl = attnImpl,
        resumeFrom = resumeFrom, initNotes = initNotes,
        galoreRank = galoreRank, galoreGap = galoreGap, loraRank = loraRank,
        dataSources = dataSources, seqLen = seqLen, globalBatch = globalBatch,
        microBatch = microBatch, gradAccum = gradAccum, numGpus = numGpus,
        packingEfficiency = packingEfficiency, tokensEstimate = tokensEstimate,
        tokensPerDomain = tokensPerDomain, mixtureWeights = mixtureWeights,
        heldOutSize = heldOutSize, corpusUniqueTokens = corpusUniqueTokens,
        epochEquivalent = epochEquivalent,
        optimizer = optimizer, lrMax = lrMax, lrMin = lrMin, warmupSteps = warmupSteps,
        scheduleType = scheduleType, weightDecay = weightDecay, gradClip = gradClip,
        lastLr = lastLr, lastGradNorm = lastGradNorm,
        totalSteps = totalSteps, finalTrainLoss = finalTrainLoss, finalEvalLoss = finalEvalLoss,
        bestEvalLoss = bestEvalLoss, relativeImprovement = relativeImprovement,
        isPlateau = isPlateau, trainEvalGap = trainEvalGap, noiseLevel = noiseLevel,
        perplexity = perplexity, lossSeries = lossSeries, evalSeries = evalSeries,
        plateauAlert = isPlateau,
        flopsEstimate = flopsEstimate, secPerStep = secPerStep, tokensPerSec = tokensPerSec,
        mfuPercent = mfuPercent, wallClockHours = wallClockHours, gpuHours = gpuHours,
        estimatedCost = estimatedCost, peakFlopsDevice = peakFlopsDevice,
        task1Name = task1Name, task1Result = task1Result,
        task2Name = task2Name, task2Result = task2Result,
        task3Name = task3Name, task3Result = task3Result,
        hasForgetting = hasForgetting, forgettingNote = forgettingNote,
        evalHarness = evalHarness, sampleNotes = sampleNotes,
        finalCheckpoint = finalCheckpoint, bestCheckpoint = bestCheckpoint,
        checkpointNotes = checkpointNotes,
        diagnosisFlat = diagnosisFlat, diagnosisOverfit = diagnosisOverfit,
        diagnosisStillLearning = diagnosisStillLearning, diagnosisNoisy = diagnosisNoisy,
        diagnosisDownstreamBad = diagnosisDownstreamBad,
        rootCause = rootCause, decision = decision, hypothesis = hypothesis,
        whatChanged = whatChanged, priority = priority, conclusion = conclusion,
        failureNotes = failureNotes
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (runId != null) "Edit run" else "Run baru") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionTitle("1. Identitas & reproduksibilitas")
            Field("Nama run", name) { name = it }
            Field("Tanggal", date) { date = it }
            Field("Git commit", gitCommit) { gitCommit = it }
            Field("Seed", seed) { seed = it }
            Field("Hostname", hostname) { hostname = it }
            Field("Hardware (mis. 2×T4 / 8×TPU)", hardware) { hardware = it }
            Field("Versi framework", frameworkVersions) { frameworkVersions = it }
            Field("Parent run", parentRunName) { parentRunName = it }
            Field("Config / CLI (ringkas)", configYaml) { configYaml = it }

            SectionTitle("2. Model & inisialisasi")
            Text("Preset arsitektur", style = MaterialTheme.typography.labelMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ArchPresets.ALL.take(3).forEach { p ->
                    FilterChip(
                        selected = architecturePreset == p.name,
                        onClick = {
                            architecturePreset = p.name
                            if (p.params.isNotBlank()) totalParams = p.params
                            if (p.seq.isNotBlank()) seqLen = p.seq
                        },
                        label = { Text(p.name, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ArchPresets.ALL.drop(3).forEach { p ->
                    FilterChip(
                        selected = architecturePreset == p.name,
                        onClick = { architecturePreset = p.name },
                        label = { Text(p.name, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Field("Layers", numLayers, Modifier.weight(1f)) { numLayers = it }
                Field("d_model", embedDim, Modifier.weight(1f)) { embedDim = it }
                Field("Heads", numHeads, Modifier.weight(1f)) { numHeads = it }
            }
            Field("Vocab size", vocabSize) { vocabSize = it }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Field("Total params", totalParams, Modifier.weight(1f)) { totalParams = it }
                Field("Trainable", trainableParams, Modifier.weight(1f)) { trainableParams = it }
            }
            Text("Precision", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PrecisionOptions.ALL.forEach { p ->
                    FilterChip(selected = precision == p, onClick = { precision = p }, label = { Text(p) })
                }
            }
            Field("Attn impl (sdpa/flash/…)", attnImpl) { attnImpl = it }
            Field("Resume from", resumeFrom) { resumeFrom = it }
            Field("Init notes (vocab resize, missing keys)", initNotes) { initNotes = it }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Field("GaLore rank", galoreRank, Modifier.weight(1f)) { galoreRank = it }
                Field("GaLore gap", galoreGap, Modifier.weight(1f)) { galoreGap = it }
                Field("LoRA rank", loraRank, Modifier.weight(1f)) { loraRank = it }
            }

            SectionTitle("3. Data")
            Field("Sumber data", dataSources) { dataSources = it }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Field("Seq len", seqLen, Modifier.weight(1f)) { seqLen = it }
                Field("Micro batch", microBatch, Modifier.weight(1f)) { microBatch = it }
                Field("Grad accum", gradAccum, Modifier.weight(1f)) { gradAccum = it }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Field("# GPU/TPU", numGpus, Modifier.weight(1f)) { numGpus = it }
                Field("Global batch", globalBatch, Modifier.weight(1f)) { globalBatch = it }
                Field("Packing eff.", packingEfficiency, Modifier.weight(1f)) { packingEfficiency = it }
            }
            Field("Mixture weights", mixtureWeights) { mixtureWeights = it }
            Field("Tokens per domain", tokensPerDomain) { tokensPerDomain = it }
            Field("Held-out size", heldOutSize) { heldOutSize = it }
            Field("Corpus unik (tokens)", corpusUniqueTokens) { corpusUniqueTokens = it }

            SectionTitle("4. Optimisasi & schedule")
            Field("Optimizer", optimizer) { optimizer = it }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Field("LR max", lrMax, Modifier.weight(1f)) { lrMax = it }
                Field("LR min", lrMin, Modifier.weight(1f)) { lrMin = it }
                Field("Warmup", warmupSteps, Modifier.weight(1f)) { warmupSteps = it }
            }
            Text("Schedule", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ScheduleTypes.ALL.forEach { s ->
                    FilterChip(selected = scheduleType == s, onClick = { scheduleType = s }, label = { Text(s) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Field("Weight decay", weightDecay, Modifier.weight(1f)) { weightDecay = it }
                Field("Grad clip", gradClip, Modifier.weight(1f)) { gradClip = it }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Field("Last LR", lastLr, Modifier.weight(1f)) { lastLr = it }
                Field("Last grad norm", lastGradNorm, Modifier.weight(1f)) { lastGradNorm = it }
            }

            SectionTitle("5. Learning curve")
            Field("Total steps (atau current/total)", totalSteps) { totalSteps = it }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Field("Train loss", finalTrainLoss, Modifier.weight(1f)) { finalTrainLoss = it }
                Field("Eval loss", finalEvalLoss, Modifier.weight(1f)) { finalEvalLoss = it }
                Field("Best eval", bestEvalLoss, Modifier.weight(1f)) { bestEvalLoss = it }
            }
            Field("Loss series (satu angka per baris)", lossSeries) { lossSeries = it }
            Field("Eval series", evalSeries) { evalSeries = it }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isPlateau, onCheckedChange = { isPlateau = it })
                Text("Plateau")
            }
            Field("Noise level (Rendah/Sedang/Tinggi)", noiseLevel) { noiseLevel = it }

            SectionTitle("6. Compute budget")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Field("Sec/step", secPerStep, Modifier.weight(1f)) { secPerStep = it }
                Field("Peak FLOPs/s device", peakFlopsDevice, Modifier.weight(1f)) { peakFlopsDevice = it }
            }
            Field("Estimasi biaya (opsional)", estimatedCost) { estimatedCost = it }

            SectionTitle("7. Downstream eval")
            Text("Template task", style = MaterialTheme.typography.labelMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                DownstreamTemplates.TASKS.take(3).forEach { t ->
                    FilterChip(
                        selected = task1Name == t,
                        onClick = { if (task1Name.isBlank()) task1Name = t else if (task2Name.isBlank()) task2Name = t else task3Name = t },
                        label = { Text(t.take(12), style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            Field("Task 1 nama", task1Name) { task1Name = it }
            Field("Task 1 hasil", task1Result) { task1Result = it }
            Field("Task 2 nama", task2Name) { task2Name = it }
            Field("Task 2 hasil", task2Result) { task2Result = it }
            Field("Task 3 nama", task3Name) { task3Name = it }
            Field("Task 3 hasil", task3Result) { task3Result = it }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = hasForgetting, onCheckedChange = { hasForgetting = it })
                Text("Ada forgetting")
            }
            Field("Catatan forgetting", forgettingNote) { forgettingNote = it }
            Field("Eval harness / versi", evalHarness) { evalHarness = it }
            Field("Sample generasi (catatan)", sampleNotes) { sampleNotes = it }

            SectionTitle("8. Checkpoint")
            Field("Final checkpoint", finalCheckpoint) { finalCheckpoint = it }
            Field("Best checkpoint", bestCheckpoint) { bestCheckpoint = it }
            Field("Notes (nama:loss per baris)", checkpointNotes) { checkpointNotes = it }

            SectionTitle("9. Diagnosis & keputusan")
            CheckRow("Flat", diagnosisFlat) { diagnosisFlat = it }
            CheckRow("Overfit", diagnosisOverfit) { diagnosisOverfit = it }
            CheckRow("Masih belajar", diagnosisStillLearning) { diagnosisStillLearning = it }
            CheckRow("Noisy", diagnosisNoisy) { diagnosisNoisy = it }
            CheckRow("Downstream jelek", diagnosisDownstreamBad) { diagnosisDownstreamBad = it }
            Field("Akar masalah", rootCause) { rootCause = it }
            Field("Hipotesis", hypothesis) { hypothesis = it }
            Field("Apa yang diubah vs parent", whatChanged) { whatChanged = it }
            Field("Keputusan", decision) { decision = it }
            Field("Priority (Tinggi/Sedang/Rendah)", priority) { priority = it }
            Field("Kesimpulan", conclusion) { conclusion = it }

            SectionTitle("10. Kegagalan & numerik")
            Field("NaN/OOM/missing keys/slow save…", failureNotes) { failureNotes = it }

            Button(onClick = { autoCompute() }, modifier = Modifier.fillMaxWidth()) {
                Text("Hitung otomatis (PPL, gap, tokens, FLOPs, MFU, plateau)")
            }

            // Live computed preview
            if (perplexity.isNotBlank() || tokensEstimate.isNotBlank() || relativeImprovement.isNotBlank()) {
                Text(
                    buildString {
                        if (perplexity.isNotBlank()) append("PPL $perplexity · ")
                        if (trainEvalGap.isNotBlank()) append("gap $trainEvalGap · ")
                        if (relativeImprovement.isNotBlank()) append("rel $relativeImprovement% · ")
                        if (tokensEstimate.isNotBlank()) append("tok $tokensEstimate · ")
                        if (flopsEstimate.isNotBlank()) append("FLOPs $flopsEstimate · ")
                        if (mfuPercent.isNotBlank()) append("MFU $mfuPercent% · ")
                        if (tokensPerSec.isNotBlank()) append("$tokensPerSec tok/s · ")
                        if (wallClockHours.isNotBlank()) append("${wallClockHours}h")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = {
                    autoCompute()
                    val run = buildRun(id = runId ?: 0)
                    if (runId != null) {
                        viewModel.updateRun(run)
                        onSaved()
                    } else {
                        viewModel.saveRun(run) { onSaved() }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simpan run")
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun Field(
    label: String,
    value: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = !label.contains("series") && !label.contains("notes") && !label.contains("Config") && !label.contains("Kesimpulan") && !label.contains("Hipotesis") && !label.contains("NaN")
    )
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onChange)
        Text(label)
    }
}
