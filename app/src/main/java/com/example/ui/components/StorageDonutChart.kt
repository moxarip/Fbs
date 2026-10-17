package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BackupRepository
import com.example.model.BackupFileItem
import com.example.model.StorageCategory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StorageDonutChart(
    files: List<BackupFileItem>,
    modifier: Modifier = Modifier
) {
    val categoryTotals = remember(files) {
        val map = mutableMapOf<StorageCategory, Long>()
        StorageCategory.values().forEach { map[it] = 0L }
        files.forEach { file ->
            map[file.category] = (map[file.category] ?: 0L) + file.sizeBytes
        }
        map
    }

    val totalBytes = remember(categoryTotals) {
        categoryTotals.values.sum().coerceAtLeast(1L)
    }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(files) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(190.dp)
        ) {
            Canvas(modifier = Modifier.size(170.dp)) {
                val strokeWidth = 22.dp.toPx()
                var startAngle = -90f

                // Background track
                drawArc(
                    color = Color.DarkGray.copy(alpha = 0.25f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )

                categoryTotals.forEach { (cat, bytes) ->
                    val proportion = (bytes.toFloat() / totalBytes.toFloat()) * animProgress.value
                    val sweepAngle = proportion * 360f
                    if (sweepAngle > 0.5f) {
                        drawArc(
                            color = Color(cat.colorHex),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle - 2f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += sweepAngle
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = BackupRepository.formatBytes(totalBytes),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Vault Space",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category Legend Chips
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoryTotals.forEach { (cat, bytes) ->
                if (bytes > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(10.dp),
                            shape = CircleShape,
                            color = Color(cat.colorHex)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${cat.label}: ${BackupRepository.formatBytes(bytes)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
