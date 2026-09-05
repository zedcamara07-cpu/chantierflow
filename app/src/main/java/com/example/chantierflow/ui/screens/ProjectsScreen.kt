package com.example.chantierflow.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.sp
import com.example.chantierflow.domain.DomainUtils
import com.example.chantierflow.model.ProjectEntity
import com.example.chantierflow.model.TaskEntity
import com.example.chantierflow.ui.components.ChantierBadge
import com.example.chantierflow.ui.components.ChantierProgressBar
import com.example.chantierflow.ui.components.EmptyState
import com.example.chantierflow.ui.components.StatCard
import com.example.chantierflow.ui.theme.Slate600
import com.example.chantierflow.ui.theme.StatusGreen
import com.example.chantierflow.ui.theme.StatusGreenBg
import com.example.chantierflow.ui.theme.StatusRed
import com.example.chantierflow.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    projects: List<ProjectEntity>,
    tasks: List<TaskEntity>,
    totalExpensesCents: Long,
    onSaveProject: (id: String?, name: String, client: String, addr: String, desc: String, status: String, start: String?, end: String?) -> Unit,
    onDeleteProject: (ProjectEntity) -> Unit,
    onNavigateToTasks: (projectId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingProject by remember { mutableStateOf<ProjectEntity?>(null) }

    val activeCount = projects.count { it.status == "active" }
    val doneTasksCount = tasks.count { it.status == "done" }

    Column(modifier = modifier.fillMaxSize()) {
        // Summary stats row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "Chantiers actifs",
                value = "$activeCount / ${projects.size}",
                icon = Icons.Default.Business,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Total Dépenses",
                value = DomainUtils.formatMoney(totalExpensesCents),
                icon = Icons.Default.Euro,
                modifier = Modifier.weight(1f),
                containerColor = StatusGreenBg,
                contentColor = StatusGreen
            )
        }

        if (projects.isEmpty()) {
            EmptyState(
                message = "Aucun chantier enregistré. Cliquez sur le bouton + pour démarrer un nouveau suivi.",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("projects_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(projects, key = { it.id }) { project ->
                    val projectTasks = tasks.filter { it.projectId == project.id }
                    val progress = DomainUtils.calculateProgress(projectTasks)

                    ProjectCard(
                        project = project,
                        tasks = projectTasks,
                        progress = progress,
                        onEdit = {
                            editingProject = project
                            showDialog = true
                        },
                        onDelete = { onDeleteProject(project) },
                        onViewTasks = { onNavigateToTasks(project.id) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        ProjectDialog(
            project = editingProject,
            onDismiss = {
                showDialog = false
                editingProject = null
            },
            onConfirm = { name, client, addr, desc, status, start, end ->
                onSaveProject(editingProject?.id, name, client, addr, desc, status, start, end)
                showDialog = false
                editingProject = null
            }
        )
    }
}

@Composable
fun ProjectCard(
    project: ProjectEntity,
    tasks: List<TaskEntity>,
    progress: Int?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("project_card_${project.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                ChantierBadge(status = project.status)
            }

            if (project.clientName.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Client : ${project.clientName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate600
                )
            }

            if (project.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Slate600,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = project.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            ChantierProgressBar(progressPercent = progress)

            Spacer(modifier = Modifier.height(6.dp))
            val doneCount = tasks.count { it.status == "done" }
            Text(
                text = if (tasks.isNotEmpty()) "$doneCount / ${tasks.size} tâche(s) terminée(s)" else "Aucune tâche pour le moment",
                style = MaterialTheme.typography.labelSmall,
                color = Slate600
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onViewTasks,
                    modifier = Modifier.testTag("view_tasks_button_${project.id}")
                ) {
                    Text("Voir les tâches", color = TealPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Modifier",
                            tint = Slate600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = StatusRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDialog(
    project: ProjectEntity?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, client: String, addr: String, desc: String, status: String, start: String?, end: String?) -> Unit
) {
    var name by remember { mutableStateOf(project?.name ?: "") }
    var clientName by remember { mutableStateOf(project?.clientName ?: "") }
    var address by remember { mutableStateOf(project?.address ?: "") }
    var description by remember { mutableStateOf(project?.description ?: "") }
    var status by remember { mutableStateOf(project?.status ?: "active") }
    var startDate by remember { mutableStateOf(project?.startDate ?: "") }
    var endDate by remember { mutableStateOf(project?.endDate ?: "") }
    var expandedStatus by remember { mutableStateOf(false) }

    val statusOptions = listOf(
        "planned" to "Planifié",
        "active" to "En cours",
        "paused" to "En pause",
        "completed" to "Terminé",
        "archived" to "Archivé"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (project == null) "Nouveau chantier" else "Modifier le chantier",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom du chantier *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_project_name")
                )
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Client") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_project_client")
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Adresse du chantier") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_project_address")
                )

                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = it }
                ) {
                    OutlinedTextField(
                        value = statusOptions.firstOrNull { it.first == status }?.second ?: status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Statut") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { expandedStatus = false }
                    ) {
                        statusOptions.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    status = key
                                    expandedStatus = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name.trim(),
                            clientName.trim(),
                            address.trim(),
                            description.trim(),
                            status,
                            startDate.ifBlank { null },
                            endDate.ifBlank { null }
                        )
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                modifier = Modifier.testTag("submit_project_button")
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
