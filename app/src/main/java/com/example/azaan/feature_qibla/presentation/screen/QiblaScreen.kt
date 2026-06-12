package com.example.azaan.feature_qibla.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.azaan.feature_qibla.presentation.viewmodel.QiblaViewModel
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(
    viewModel: QiblaViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Qibla Compass") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.loading -> {
                    CircularProgressIndicator()
                }
                state.error != null -> {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                state.direction != null -> {
                    CompassView(direction = state.direction)
                }
            }
        }
    }
}

@Composable
private fun CompassView(direction: com.example.azaan.feature_qibla.domain.QiblaDirection) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val compassSize = 280.dp

        Box(
            modifier = Modifier.size(compassSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val canvasSize = size.minDimension
                val center = Offset(size.width / 2, size.height / 2)
                val radius = canvasSize / 2

                // Draw compass circle
                drawCircle(
                    color = Color(0xFF1A1A2E),
                    radius = radius
                )
                drawCircle(
                    color = Color(0xFF2D2D44),
                    radius = radius * 0.95f
                )

                // Draw degree ticks
                for (i in 0 until 360 step 30) {
                    val angle = Math.toRadians(i.toDouble())
                    val innerRadius = if (i % 90 == 0) radius * 0.75f else radius * 0.82f
                    val outerRadius = radius * 0.88f
                    val startX = center.x + (innerRadius * cos(angle)).toFloat()
                    val startY = center.y + (innerRadius * sin(angle)).toFloat()
                    val endX = center.x + (outerRadius * cos(angle)).toFloat()
                    val endY = center.y + (outerRadius * sin(angle)).toFloat()
                    drawLine(
                        color = Color.White.copy(alpha = 0.4f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (i % 90 == 0) 3f else 1f
                    )
                }

                // Rotate everything by -deviceAzimuth (so north aligns with device heading)
                val rotationDegrees = -direction.deviceAzimuth

                rotate(rotationDegrees, pivot = center) {
                    // North needle (red)
                    drawLine(
                        color = Color.Red,
                        start = center,
                        end = Offset(center.x, center.y - radius * 0.7f),
                        strokeWidth = 6f
                    )
                    // South needle (white)
                    drawLine(
                        color = Color.LightGray,
                        start = center,
                        end = Offset(center.x, center.y + radius * 0.7f),
                        strokeWidth = 6f
                    )

                    // Qibla marker (green arc at the top of the needle)
                    val qiblaRad = Math.toRadians(direction.qiblaAngle.toDouble())
                    val qiblaEndX = center.x + (radius * 0.65f * sin(qiblaRad)).toFloat()
                    val qiblaEndY = center.y - (radius * 0.65f * cos(qiblaRad)).toFloat()

                    // Draw Qibla direction line
                    drawLine(
                        color = Color.Green,
                        start = center,
                        end = Offset(qiblaEndX, qiblaEndY),
                        strokeWidth = 4f
                    )

                    // Qibla indicator circle
                    drawCircle(
                        color = Color.Green,
                        radius = 12f,
                        center = Offset(qiblaEndX, qiblaEndY)
                    )
                }

                // Center dot
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Direction labels
        Text(
            text = "Qibla Direction",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${direction.qiblaAngle.toInt()}° from North",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Device facing label
        Text(
            text = "Device facing: ${direction.deviceAzimuth.toInt()}°",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = getDirectionLabel(direction.bearingToQibla),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

private fun getDirectionLabel(bearing: Float): String {
    return when {
        bearing < 22.5f || bearing >= 337.5f -> "You are facing Qibla!"
        bearing < 67.5f -> "Turn slightly right"
        bearing < 112.5f -> "Turn right"
        bearing < 157.5f -> "Turn sharply right"
        bearing < 202.5f -> "Turn around"
        bearing < 247.5f -> "Turn sharply left"
        bearing < 292.5f -> "Turn left"
        bearing < 337.5f -> "Turn slightly left"
        else -> "You are facing Qibla!"
    }
}
