package com.example.chantierflow.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chantierflow.domain.DomainUtils
import com.example.chantierflow.model.ProjectEntity
import com.example.chantierflow.model.ReportEntity
import com.example.chantierflow.ui.components.ChantierBadge
import com.example.chantierflow.ui.components.EmptyState
import com.example.chantierflow.ui.components.ProjectFilterChips
import com.example.chantierflow.ui.theme.Slate600
import com.example.chantierflow.ui.theme.StatusRed
import com.example.chantierflow.ui.theme.TealPrimary

@Composable
fun ReportsScreen(
    reports: List<ReportEntity>,
    projects: List<ProjectEntity>,
    selectedProjectId: String?,
    onSelectProject: (String?) -> Unit,
    onSaveReport: (id: String?, projectId: String, title: String, content: String, reportDate: String, status: String) -> Unit,
    onDeleteReport: (ReportEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingReport by remember { mutableStateOf<ReportEntity?>(null) }

    val filteredReports = reports.filter { report ->
        selectedProjectId == null || report.projectId == selectedProjectId
    }

    Column(modifier = modifier.fillMaxSize()) {
        ProjectFilterChips(
            projects = projects,
            selectedProjectId = selectedProjectId,
            onSelectProject = onSelectProject
        )

        if (filteredReports.isEmpty()) {
            EmptyState(
                message = "Aucun rapport de chantier rédigé. Cliquez sur le bouton + pour consigner la journée.",
                icon = Icons.Default.Description,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("reports_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredReports, key = { it.id }) { report ->
                    val projectName = projects.firstOrNull { it.id == report.projectId }?.name ?: "Chantier"
                    ReportCard(
                        report = report,
                        projectName = projectName,
                        onEdit = {
                            editingReport = report
                            showDialog = true
                        },
                        onDelete = { onDeleteReport(report) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        ReportDialog(
            report = editingReport,
            projects = projects,
            defaultProjectId = selectedProjectId ?: projects.firstOrNull()?.id ?: "",
            onDismiss = {
                showDialog = false
                editingReport = null
            },
            onConfirm = { projId, title, content, date, status ->
                onSaveReport(editingReport?.id, projId, title, content, date, status)
                showDialog = false
                editingReport = null
            }
        )
    }
}

@Composable
fun ReportCard(
    report: ReportEntity,
    projectName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("report_card_${report.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = DomainUtils.formatDate(report.reportDate),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                }
                ChantierBadge(status = report.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = report.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = projectName,
                style = MaterialTheme.typography.labelSmall,
                color = Slate600
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = report.content,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate600
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Modifier",
                        tint = Slate600,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = StatusRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDialog(
    report: ReportEntity?,
    projects: List<ProjectEntity>,
    defaultProjectId: String,
    onDismiss: () -> Unit,
    onConfirm: (projectId: String, title: String, content: String, date: String, status: String) -> Unit
) {
    var projectId by remember { mutableStateOf(report?.projectId ?: defaultProjectId) }
    var title by remember { mutableStateOf(report?.title ?: "") }
    var content by remember { mutableStateOf(report?.content ?: "") }
    var reportDate by remember { mutableStateOf(report?.reportDate ?: DomainUtils.localToday()) }
    var status by remember { mutableStateOf(report?.status ?: "draft") }

    var expandedProject by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }

    val statusOptions = listOf(
        "draft" to "Brouillon",
        "submitted" to "Transmis",
        "validated" to "Validé"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (report == null) "Nouveau rapport journalier" else "Modifier le rapport",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (projects.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedProject,
                        onExpandedChange = { expandedProject = it }
                    ) {
                        val currentProjectName = projects.firstOrNull { it.id == projectId }?.name ?: "Choisir un chantier"
                        OutlinedTextField(
                            value = currentProjectName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Chantier *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProject) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedProject,
                            onDismissRequest = { expandedProject = false }
                        ) {
                            projects.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p.name) },
                                    onClick = {
                                        projectId = p.id
                                        expandedProject = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre du rapport *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_report_title")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = reportDate,
                        onValueChange = { reportDate = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandedStatus,
                        onExpandedChange = { expandedStatus = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = statusOptions.firstOrNull { it.first == status }?.second ?: status,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Statut") },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedStatus,
                            onDismissRequest = { expandedStatus = false }
                        ) {
                            statusOptions.forEach { (k, v) ->
                                DropdownMenuItem(
                                    text = { Text(v) },
                                    onClick = {
                                        status = k
                                        expandedStatus = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Observations, intervenants, livraisons *") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth().testTag("input_report_content")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank() && projectId.isNotBlank()) {
                        onConfirm(
                            projectId,
                            title.trim(),
                            content.trim(),
                            reportDate.trim(),
                            status
                        )
                    }
                },
                enabled = title.isNotBlank() && content.isNotBlank() && projectId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                modifier = Modifier.testTag("submit_report_button")
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
