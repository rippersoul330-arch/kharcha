package com.kharcha.app.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kharcha.app.data.CategoryTotal
import com.kharcha.app.util.Money

/** Safe hex -> Color; falls back to grey for deleted/uncategorised slices. */
fun colorFromHex(hex: String?): Color = try {
    Color(android.graphics.Color.parseColor(hex ?: "#78909C"))
} catch (e: IllegalArgumentException) {
    Color(0xFF78909C)
}

/**
 * A donut chart of the month's spending by category, with the grand total in the
 * middle and a legend listing each category's amount and share.
 */
@Composable
fun SpendingPieChart(
    totals: List<CategoryTotal>,
    grandTotalPaise: Long,
    modifier: Modifier = Modifier
) {
    val emptyRingColor = MaterialTheme.colorScheme.surfaceVariant
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.size(200.dp)
            ) {
                val stroke = size.minDimension * 0.18f
                val diameter = size.minDimension - stroke
                val topLeft = Offset(
                    (size.width - diameter) / 2f,
                    (size.height - diameter) / 2f
                )
                val arcSize = Size(diameter, diameter)

                if (grandTotalPaise <= 0L) {
                    // Empty ring when there's nothing yet.
                    drawArc(
                        color = emptyRingColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke)
                    )
                    return@Canvas
                }

                val gap = if (totals.size > 1) 3f else 0f
                var startAngle = -90f
                totals.forEach { slice ->
                    val fullSweep = (slice.totalPaise.toFloat() / grandTotalPaise.toFloat()) * 360f
                    drawArc(
                        color = colorFromHex(slice.colorHex),
                        startAngle = startAngle + gap / 2f,
                        sweepAngle = (fullSweep - gap).coerceAtLeast(1f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    startAngle += fullSweep
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Spent",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = Money.formatRupees(grandTotalPaise),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        totals.forEach { slice ->
            val share = if (grandTotalPaise > 0)
                (slice.totalPaise * 100f / grandTotalPaise) else 0f
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(colorFromHex(slice.colorHex))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = slice.categoryName ?: "Uncategorised",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${Money.formatRupees(slice.totalPaise)}  •  ${share.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (totals.isEmpty()) {
            Text(
                text = "No expenses yet this month.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}
