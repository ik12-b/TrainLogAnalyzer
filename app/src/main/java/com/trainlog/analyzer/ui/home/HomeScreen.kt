package com.trainlog.analyzer.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.trainlog.analyzer.R
import com.trainlog.analyzer.data.model.TrainingRun
import com.trainlog.analyzer.ui.components.ActionTile
import com.trainlog.analyzer.ui.components.BadgeKind
import com.trainlog.analyzer.ui.components.EmptyState
import com.trainlog.analyzer.ui.components.SectionLabel
import com.trainlog.analyzer.ui.components.StatusBadge
import com.trainlog.analyzer.viewmodel.TrainingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TrainingViewModel,
    onAddClick: () -> Unit,
    onRunClick: (Long) -> Unit,
    onLabClick: () -> Unit,
    onImportClick: () -> Unit = {},
    onCompareClick: () -> Unit = {}
) {
    val runs by viewModel.allRuns.collectAsState()
    val plateauCount = runs.count { it.isPlateau }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.app_icon),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(Modifier.size(12.dp))
                        Column {
                            Text("TrainLog", fontWeight = FontWeight.Bold)
                            Text(
                                "Lab notebook",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(Modifier.height(4.dp))
                // Hero
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            "TRAINING RUNS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Catat eksperimen.\nPutuskan dengan data.",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            HeroStat("${runs.size}", "runs")
                            HeroStat("$plateauCount", "plateau")
                            HeroStat(
                                runs.count { !it.isPlateau }.toString(),
                                "aktif"
                            )
                        }
                    }
                }
            }

            item {
                SectionLabel("Alat")
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        title = "Lab",
                        subtitle = "PPL · FLOPs · MFU",
                        icon = Icons.Default.Build,
                        onClick = onLabClick,
                        modifier = Modifier.weight(1f)
                    )
                    ActionTile(
                        title = "Import",
                        subtitle = "Parse log HF/Tunix",
                        icon = Icons.Default.Share,
                        onClick = onImportClick,
                        modifier = Modifier.weight(1f),
                        accent = MaterialTheme.colorScheme.tertiary
                    )
                }
                Spacer(Modifier.height(10.dp))
                ActionTile(
                    title = "Compare runs",
                    subtitle = "Side-by-side metrik & kurva loss",
                    icon = Icons.Default.KeyboardArrowRight,
                    onClick = onCompareClick,
                    modifier = Modifier.fillMaxWidth(),
                    accent = MaterialTheme.colorScheme.secondary
                )
            }

            item {
                SectionLabel("Runs (${runs.size})")
            }

            if (runs.isEmpty()) {
                item {
                    EmptyState(
                        title = "Belum ada run",
                        body = "Tap + untuk run baru, atau Import log dari Qwen / Gemma."
                    )
                }
            } else {
                items(runs, key = { it.id }) { run ->
                    RunCard(run = run, onClick = { onRunClick(run.id) })
                }
            }

            item { Spacer(Modifier.height(88.dp)) }
        }
    }
}

@Composable
private fun HeroStat(value: String, label: String) {
    Column(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun RunCard(run: TrainingRun, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    run.name.ifBlank { "Untitled" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.size(8.dp))
                StatusBadge(
                    text = if (run.isPlateau) "Plateau" else "Active",
                    kind = if (run.isPlateau) BadgeKind.Danger else BadgeKind.Success
                )
            }

            if (run.date.isNotBlank() || run.hardware.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    listOfNotNull(
                        run.date.takeIf { it.isNotBlank() },
                        run.hardware.takeIf { it.isNotBlank() }
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniMetric(
                    "Loss",
                    run.finalTrainLoss.ifBlank { "—" },
                    Modifier.weight(1f)
                )
                MiniMetric(
                    "PPL",
                    run.perplexity.ifBlank { "—" },
                    Modifier.weight(1f)
                )
                MiniMetric(
                    "Steps",
                    run.totalSteps.ifBlank { "—" }.take(12),
                    Modifier.weight(1f)
                )
            }

            if (run.decision.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    run.decision,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Detail",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun MiniMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
