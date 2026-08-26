package com.trainlog.analyzer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trainlog.analyzer.util.Calc
import kotlin.math.max
import kotlin.math.min

/**
 * Full loss visualization: grid, train/eval/EMA curves, plateau marker, axis labels, legend, stats.
 */
@Composable
fun LossChart(
    train: List<Double>,
    eval: List<Double> = emptyList(),
    plateauFromIndex: Int? = null,
    showEma: Boolean = true,
    emaAlpha: Double = 0.15,
    modifier: Modifier = Modifier,
    trainColor: Color = MaterialTheme.colorScheme.primary,
    evalColor: Color = MaterialTheme.colorScheme.tertiary,
    emaColor: Color = MaterialTheme.colorScheme.secondary
) {
    if (train.isEmpty() && eval.isEmpty()) return

    val ema = remember(train, emaAlpha, showEma) {
        if (!showEma || train.size < 3) emptyList()
        else Calc.ema(train, emaAlpha)
    }

    val all = train + eval + ema
    val minY = all.minOrNull() ?: 0.0
    val maxY = all.maxOrNull() ?: 1.0
    // slight padding on y so line tidak nempel tepi
    val yPad = max((maxY - minY) * 0.08, 1e-6)
    val yMin = minY - yPad
    val yMax = maxY + yPad

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val leftPad = 44f
        val rightPad = 12f
        val topPad = 12f
        val bottomPad = 28f
        val w = size.width - leftPad - rightPad
        val h = size.height - topPad - bottomPad
        val span = (yMax - yMin).let { if (it < 1e-12) 1.0 else it }

        fun mapX(i: Int, n: Int): Float {
            if (n <= 1) return leftPad + w / 2
            return leftPad + w * i / (n - 1)
        }
        fun mapY(v: Double): Float {
            val t = ((v - yMin) / span).toFloat()
            return topPad + h * (1f - t)
        }

        val grid = Color.Gray.copy(alpha = 0.18f)
        val labelColor = Color.Gray.copy(alpha = 0.85f)

        // horizontal grid + y labels (4 levels)
        for (i in 0..4) {
            val frac = i / 4f
            val y = topPad + h * frac
            val value = yMax - span * frac
            drawLine(
                color = grid,
                start = Offset(leftPad, y),
                end = Offset(leftPad + w, y),
                strokeWidth = 1f
            )
            // simple tick mark
            drawLine(
                color = labelColor,
                start = Offset(leftPad - 4f, y),
                end = Offset(leftPad, y),
                strokeWidth = 1.5f
            )
        }

        // axes
        drawLine(
            color = Color.Gray.copy(alpha = 0.4f),
            start = Offset(leftPad, topPad),
            end = Offset(leftPad, topPad + h),
            strokeWidth = 1.5f
        )
        drawLine(
            color = Color.Gray.copy(alpha = 0.4f),
            start = Offset(leftPad, topPad + h),
            end = Offset(leftPad + w, topPad + h),
            strokeWidth = 1.5f
        )

        // plateau vertical marker
        if (plateauFromIndex != null && train.isNotEmpty()) {
            val idx = plateauFromIndex.coerceIn(0, train.lastIndex)
            val x = mapX(idx, train.size)
            drawLine(
                color = Color(0xFFB45309).copy(alpha = 0.55f),
                start = Offset(x, topPad),
                end = Offset(x, topPad + h),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
            )
        }

        fun drawArea(series: List<Double>, color: Color) {
            if (series.size < 2) return
            val path = Path()
            series.forEachIndexed { i, v ->
                val x = mapX(i, series.size)
                val y = mapY(v)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            // close to baseline for fill
            path.lineTo(mapX(series.lastIndex, series.size), topPad + h)
            path.lineTo(mapX(0, series.size), topPad + h)
            path.close()
            drawPath(
                path,
                brush = Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = 0.22f), color.copy(alpha = 0.02f)),
                    startY = topPad,
                    endY = topPad + h
                )
            )
        }

        fun drawSeries(series: List<Double>, color: Color, stroke: Float, dashed: Boolean = false) {
            if (series.isEmpty()) return
            val path = Path()
            series.forEachIndexed { i, v ->
                val x = mapX(i, series.size)
                val y = mapY(v)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path,
                color = color,
                style = Stroke(
                    width = stroke,
                    cap = StrokeCap.Round,
                    pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(12f, 8f)) else null
                )
            )
            // last point dot
            val last = series.last()
            drawCircle(
                color = color,
                radius = 5f,
                center = Offset(mapX(series.lastIndex, series.size), mapY(last))
            )
            drawCircle(
                color = Color.White,
                radius = 2.2f,
                center = Offset(mapX(series.lastIndex, series.size), mapY(last))
            )
        }

        if (train.isNotEmpty()) drawArea(train, trainColor)
        if (ema.isNotEmpty()) drawSeries(ema, emaColor, 2.5f, dashed = true)
        drawSeries(train, trainColor, 3.5f)
        drawSeries(eval, evalColor, 2.8f)
    }
}

@Composable
fun LossChartCard(
    title: String = "Kurva loss",
    train: List<Double>,
    eval: List<Double> = emptyList(),
    plateauFromIndex: Int? = null,
    showEma: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (train.isEmpty() && eval.isEmpty()) return

    val lastN = train.takeLast(min(20, train.size))
    val stats = remember(train, eval) {
        val last = train.lastOrNull()
        val minV = train.minOrNull()
        val maxV = train.maxOrNull()
        val meanLast = if (lastN.isEmpty()) null else lastN.average()
        Quad(last, minV, maxV, meanLast)
    }

    val yAll = train + eval
    val yMin = yAll.minOrNull() ?: 0.0
    val yMax = yAll.maxOrNull() ?: 1.0

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${train.size} pts" + if (eval.isNotEmpty()) " · ${eval.size} eval" else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Y range labels row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("max ${"%.3f".format(yMax)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("min ${"%.3f".format(yMin)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Box {
                LossChart(
                    train = train,
                    eval = eval,
                    plateauFromIndex = plateauFromIndex,
                    showEma = showEma,
                    modifier = Modifier.fillMaxWidth()
                )
                // overlay y-axis numeric labels on left
                Column(
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 2.dp)
                        .height(200.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(yMax, (yMax + yMin) / 2, yMin).forEach { v ->
                        Text(
                            "%.2f".format(v),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Legend
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendDot(MaterialTheme.colorScheme.primary, "Train")
                if (eval.isNotEmpty()) LegendDot(MaterialTheme.colorScheme.tertiary, "Eval")
                if (showEma && train.size >= 3) LegendDot(MaterialTheme.colorScheme.secondary, "EMA")
                if (plateauFromIndex != null) LegendDot(Color(0xFFB45309), "Plateau")
            }

            // Stats chips
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip("Last", stats.a?.let { "%.4f".format(it) } ?: "—")
                StatChip("Min", stats.b?.let { "%.4f".format(it) } ?: "—")
                StatChip("Max", stats.c?.let { "%.4f".format(it) } ?: "—")
                StatChip("μ last20", stats.d?.let { "%.4f".format(it) } ?: "—")
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Column(
        Modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
