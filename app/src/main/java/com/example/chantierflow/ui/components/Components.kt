package com.example.chantierflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chantierflow.model.ProjectEntity
import com.example.chantierflow.ui.theme.Slate400
import com.example.chantierflow.ui.theme.Slate600
import com.example.chantierflow.ui.theme.StatusAmber
import com.example.chantierflow.ui.theme.StatusAmberBg
import com.example.chantierflow.ui.theme.StatusBlue
import com.example.chantierflow.ui.theme.StatusBlueBg
import com.example.chantierflow.ui.theme.StatusGray
import com.example.chantierflow.ui.theme.StatusGrayBg
import com.example.chantierflow.ui.theme.StatusGreen
import com.example.chantierflow.ui.theme.StatusGreenBg
import com.example.chantierflow.ui.theme.StatusRed
import com.example.chantierflow.ui.theme.StatusRedBg
import com.example.chantierflow.ui.theme.TealLight
import com.example.chantierflow.ui.theme.TealPrimary

@Composable
fun ChantierBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (label, textColor, bgColor) = when (status.lowercase()) {
        "planned" -> Triple("Planifié", StatusBlue, StatusBlueBg)
        "active" -> Triple("En cours", StatusGreen, StatusGreenBg)
        "paused" -> Triple("En pause", StatusAmber, StatusAmberBg)
        "completed" -> Triple("Terminé", StatusGray, StatusGrayBg)
        "archived" -> Triple("Archivé", StatusGray, StatusGrayBg)
        "todo" -> Triple("À faire", StatusBlue, StatusBlueBg)
        "doing" -> Triple("En cours", StatusAmber, StatusAmberBg)
        "blocked" -> Triple("Bloqué", StatusRed, StatusRedBg)
        "done" -> Triple("Terminé", StatusGreen, StatusGreenBg)
        "draft" -> Triple("Brouillon", StatusGray, StatusGrayBg)
        "submitted" -> Triple("Transmis", StatusBlue, StatusBlueBg)
        "validated" -> Triple("Validé", StatusGreen, StatusGreenBg)
        "pending" -> Triple("En attente", StatusAmber, StatusAmberBg)
        "paid" -> Triple("Payé", StatusGreen, StatusGreenBg)
        "high" -> Triple("Haute", StatusRed, StatusRedBg)
        "normal" -> Triple("Normale", StatusBlue, StatusBlueBg)
        "low" -> Triple("Basse", StatusGray, StatusGrayBg)
        else -> Triple(status, StatusGray, StatusGrayBg)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ChantierProgressBar(
    progressPercent: Int?,
    modifier: Modifier = Modifier
) {
    val percent = progressPercent ?: 0
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Avancement des tâches",
                style = MaterialTheme.typography.bodySmall,
                color = Slate600
            )
            Text(
                text = if (progressPercent == null) "—" else "$percent %",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = TealPrimary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE2E8F0))
        ) {
            if (percent > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (percent / 100f).coerceIn(0f, 1f))
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(TealPrimary)
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = TealLight,
    contentColor: Color = TealPrimary
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = Slate600
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    icon: ImageVector = Icons.Default.Info,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Slate600,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ProjectFilterChips(
    projects: List<ProjectEntity>,
    selectedProjectId: String?,
    onSelectProject: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedProjectId == null,
            onClick = { onSelectProject(null) },
            label = { Text("Tous les chantiers") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = TealPrimary,
                selectedLabelColor = Color.White
            ),
            modifier = Modifier.testTag("filter_all_projects")
        )
        projects.forEach { project ->
            FilterChip(
                selected = selectedProjectId == project.id,
                onClick = { onSelectProject(project.id) },
                label = { Text(project.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TealPrimary,
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("filter_project_${project.id}")
            )
        }
    }
}
