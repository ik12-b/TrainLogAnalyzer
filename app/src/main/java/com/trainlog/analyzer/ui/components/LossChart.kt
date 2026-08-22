package com.trainlog.analyzer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun LossChart(
    train: List<Double>,
    eval: List<Double> = emptyList(),
    plateauFromIndex: Int? = null,
    modifier: Modifier = Modifier,
    trainColor: Color = MaterialTheme.colorScheme.primary,
    evalColor: Color = MaterialTheme.colorScheme.tertiary
) {
    if (train.isEmpty() && eval.isEmpty()) return

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val all = train + eval
        if (all.isEmpty()) return@Canvas
        val minY = all.minOrNull() ?: 0.0
        val maxY = all.maxOrNull() ?: 1.0
        val span = (maxY - minY).let { if (it < 1e-9) 1.0 else it }
        val pad = 8f
        val w = size.width - pad * 2
        val h = size.height - pad * 2

        fun mapX(i: Int, n: Int): Float {
            if (n <= 1) return pad + w / 2
            return pad + w * i / (n - 1)
        }
        fun mapY(v: Double): Float {
            val t = ((v - minY) / span).toFloat()
            return pad + h * (1f - t)
        }

        fun drawSeries(series: List<Double>, color: Color, stroke: Float = 3f) {
            if (series.isEmpty()) return
            val path = Path()
            series.forEachIndexed { i, v ->
                val x = mapX(i, series.size)
                val y = mapY(v)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color = color, style = Stroke(width = stroke))
        }

        // grid
        val grid = Color.Gray.copy(alpha = 0.2f)
        for (i in 0..3) {
            val y = pad + h * i / 3f
            drawLine(grid, Offset(pad, y), Offset(pad + w, y), strokeWidth = 1f)
        }

        if (plateauFromIndex != null && train.isNotEmpty()) {
            val x = mapX(plateauFromIndex.coerceIn(0, train.lastIndex), train.size)
            drawLine(
                color = Color(0xFFB45309).copy(alpha = 0.5f),
                start = Offset(x, pad),
                end = Offset(x, pad + h),
                strokeWidth = 2f
            )
        }

        drawSeries(train, trainColor)
        drawSeries(eval, evalColor, stroke = 2.5f)
    }
}
