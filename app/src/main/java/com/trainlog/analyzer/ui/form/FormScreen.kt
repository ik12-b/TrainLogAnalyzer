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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trainlog.analyzer.data.model.TrainingRun
import com.trainlog.analyzer.viewmodel.TrainingViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    viewModel: TrainingViewModel,
    runId: Long? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val today = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today) }
    var totalSteps by remember { mutableStateOf("") }
    var resumeFrom by remember { mutableStateOf("") }
    var finalTrainLoss by remember { mutableStateOf("") }
    var finalEvalLoss by remember { mutableStateOf("") }
    var bestEvalLoss by remember { mutableStateOf("") }
    var relativeImprovement by remember { mutableStateOf("") }
    var isPlateau by remember { mutableStateOf(false) }
    var trainEvalGap by remember { mutableStateOf("") }
    var noiseLevel by remember { mutableStateOf("Sedang") }
    var perplexity by remember { mutableStateOf("") }
    var task1Name by remember { mutableStateOf("") }
    var task1Result by remember { mutableStateOf("") }
    var task2Name by remember { mutableStateOf("") }
    var task2Result by remember { mutableStateOf("") }
    var hasForgetting by remember { mutableStateOf(false) }
    var diagnosisFlat by remember { mutableStateOf(false) }
    var diagnosisOverfit by remember { mutableStateOf(false) }
    var diagnosisStillLearning by remember { mutableStateOf(false) }
    var diagnosisNoisy by remember { mutableStateOf(false) }
    var diagnosisDownstreamBad by remember { mutableStateOf(false) }
    var rootCause by remember { mutableStateOf("") }
    var decision by remember { mutableStateOf("") }
    var finalCheckpoint by remember { mutableStateOf("") }
    var hypothesis by remember { mutableStateOf("") }
    var whatChanged by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Sedang") }
    var conclusion by remember { mutableStateOf("") }

    // Load existing data if editing
    LaunchedEffect(runId) {
        if (runId != null) {
            val run = viewModel.getRun(runId)
            run?.let {
                name = it.name
                date = it.date
                totalSteps = it.totalSteps
                resumeFrom = it.resumeFrom
                finalTrainLoss = it.finalTrainLoss
                finalEvalLoss = it.finalEvalLoss
                bestEvalLoss = it.bestEvalLoss
                relativeImprovement = it.relativeImprovement
                isPlateau = it.isPlateau
                trainEvalGap = it.trainEvalGap
                noiseLevel = it.noiseLevel
                perplexity = it.perplexity
                task1Name = it.task1Name
                task1Result = it.task1Result
                task2Name = it.task2Name
                task2Result = it.task2Result
                hasForgetting = it.hasForgetting
                diagnosisFlat = it.diagnosisFlat
                diagnosisOverfit = it.diagnosisOverfit
                diagnosisStillLearning = it.diagnosisStillLearning
                diagnosisNoisy = it.diagnosisNoisy
                diagnosisDownstreamBad = it.diagnosisDownstreamBad
                rootCause = it.rootCause
                decision = it.decision
                finalCheckpoint = it.finalCheckpoint
                hypothesis = it.hypothesis
                whatChanged = it.whatChanged
                priority = it.priority
                conclusion = it.conclusion
            }
        }
    }

    fun buildRun(id: Long = 0): TrainingRun {
        return TrainingRun(
            id = id,
            name = name,
            date = date,
            totalSteps = totalSteps,
            resumeFrom = resumeFrom,
            finalTrainLoss = finalTrainLoss,
            finalEvalLoss = finalEvalLoss,
            bestEvalLoss = bestEvalLoss,
            relativeImprovement = relativeImprovement,
            isPlateau = isPlateau,
            trainEvalGap = trainEvalGap,
            noiseLevel = noiseLevel,
            perplexity = perplexity,
            task1Name = task1Name,
            task1Result = task1Result,
            task2Name = task2Name,
            task2Result = task2Result,
            hasForgetting = hasForgetting,
            diagnosisFlat = diagnosisFlat,
            diagnosisOverfit = diagnosisOverfit,
            diagnosisStillLearning = diagnosisStillLearning,
            diagnosisNoisy = diagnosisNoisy,
            diagnosisDownstreamBad = diagnosisDownstreamBad,
            rootCause = rootCause,
            decision = decision,
            finalCheckpoint = finalCheckpoint,
            hypothesis = hypothesis,
            whatChanged = whatChanged,
            priority = priority,
            conclusion = conclusion
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (runId != null) "Edit Run" else "New Training Run") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ===== BASIC INFO =====
            SectionTitle("Basic Info")
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Model / Run Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = totalSteps,
                onValueChange = { totalSteps = it },
                label = { Text("Total Steps") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = resumeFrom,
                onValueChange = { resumeFrom = it },
                label = { Text("Resume from") },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            // ===== LEARNING CURVE =====
            SectionTitle("Learning Curve")
            OutlinedTextField(
                value = finalTrainLoss,
                onValueChange = { finalTrainLoss = it },
                label = { Text("Final Train Loss") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = finalEvalLoss,
                onValueChange = { finalEvalLoss = it },
                label = { Text("Final Eval Loss") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = bestEvalLoss,
                onValueChange = { bestEvalLoss = it },
                label = { Text("Best Eval Loss") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = relativeImprovement,
                onValueChange = { relativeImprovement = it },
                label = { Text("Relative Improvement (%)") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isPlateau, onCheckedChange = { isPlateau = it })
                Text("Plateau")
            }
            OutlinedTextField(
                value = trainEvalGap,
                onValueChange = { trainEvalGap = it },
                label = { Text("Train–Eval Gap") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Noise Level", style = MaterialTheme.typography.bodyMedium)
            listOf("Rendah", "Sedang", "Tinggi").forEach { level ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = noiseLevel == level,
                            onClick = { noiseLevel = level },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = noiseLevel == level, onClick = null)
                    Text(level, Modifier.padding(start = 8.dp))
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            // ===== DOWNSTREAM =====
            SectionTitle("Downstream & Generalization")
            OutlinedTextField(
                value = perplexity,
                onValueChange = { perplexity = it },
                label = { Text("Perplexity") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = task1Name,
                onValueChange = { task1Name = it },
                label = { Text("Task 1 Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = task1Result,
                onValueChange = { task1Result = it },
                label = { Text("Task 1 Result") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = task2Name,
                onValueChange = { task2Name = it },
                label = { Text("Task 2 Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = task2Result,
                onValueChange = { task2Result = it },
                label = { Text("Task 2 Result") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = hasForgetting, onCheckedChange = { hasForgetting = it })
                Text("Ada Catastrophic Forgetting")
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            // ===== DIAGNOSIS =====
            SectionTitle("Diagnosis")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = diagnosisFlat, onCheckedChange = { diagnosisFlat = it })
                Text("Train & Eval flat")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = diagnosisOverfit, onCheckedChange = { diagnosisOverfit = it })
                Text("Overfitting")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = diagnosisStillLearning, onCheckedChange = { diagnosisStillLearning = it })
                Text("Masih ada ruang belajar")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = diagnosisNoisy, onCheckedChange = { diagnosisNoisy = it })
                Text("Sangat noisy")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = diagnosisDownstreamBad, onCheckedChange = { diagnosisDownstreamBad = it })
                Text("Downstream jelek")
            }
            OutlinedTextField(
                value = rootCause,
                onValueChange = { rootCause = it },
                label = { Text("Akar masalah") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            // ===== KEPUTUSAN =====
            SectionTitle("Keputusan")
            val decisions = listOf(
                "Stop & pakai checkpoint terbaik",
                "Lanjut training",
                "LR Restart",
                "Rubah data",
                "Rubah arsitektur",
                "Eksperimen baru"
            )
            decisions.forEach { d ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = decision == d,
                            onClick = { decision = d },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = decision == d, onClick = null)
                    Text(d, Modifier.padding(start = 8.dp))
                }
            }
            OutlinedTextField(
                value = finalCheckpoint,
                onValueChange = { finalCheckpoint = it },
                label = { Text("Checkpoint final") },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            // ===== NEXT PLAN =====
            SectionTitle("Next Experiment Plan")
            OutlinedTextField(
                value = hypothesis,
                onValueChange = { hypothesis = it },
                label = { Text("Hipotesis") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            OutlinedTextField(
                value = whatChanged,
                onValueChange = { whatChanged = it },
                label = { Text("Yang akan diubah") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Text("Priority", style = MaterialTheme.typography.bodyMedium)
            listOf("Tinggi", "Sedang", "Rendah").forEach { p ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = priority == p,
                            onClick = { priority = p },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = priority == p, onClick = null)
                    Text(p, Modifier.padding(start = 8.dp))
                }
            }

            OutlinedTextField(
                value = conclusion,
                onValueChange = { conclusion = it },
                label = { Text("Kesimpulan") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
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
                Text("Simpan")
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Batal")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
