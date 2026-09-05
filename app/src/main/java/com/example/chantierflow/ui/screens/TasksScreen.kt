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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chantierflow.domain.DomainUtils
import com.example.chantierflow.model.ProjectEntity
import com.example.chantierflow.model.TaskEntity
import com.example.chantierflow.ui.components.ChantierBadge
import com.example.chantierflow.ui.components.EmptyState
import com.example.chantierflow.ui.components.ProjectFilterChips
import com.example.chantierflow.ui.theme.Slate400
import com.example.chantierflow.ui.theme.Slate600
import com.example.chantierflow.ui.theme.StatusGreen
import com.example.chantierflow.ui.theme.StatusRed
import com.example.chantierflow.ui.theme.TealPrimary

@Composable
fun TasksScreen(
    tasks: List<TaskEntity>,
    projects: List<ProjectEntity>,
    selectedProjectId: String?,
    onSelectProject: (String?) -> Unit,
    onToggleTaskStatus: (TaskEntity) -> Unit,
    onSaveTask: (id: String?, projectId: String, title: String, desc: String, status: String, priority: String, due: String?) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskEntity?>(null) }

    val filteredTasks = tasks.filter { task ->
        selectedProjectId == null || task.projectId == selectedProjectId
    }

    Column(modifier = modifier.fillMaxSize()) {
        ProjectFilterChips(
            projects = projects,
            selectedProjectId = selectedProjectId,
            onSelectProject = onSelectProject
        )

        if (filteredTasks.isEmpty()) {
            EmptyState(
                message = "Aucune tâche pour ce chantier. Utilisez le bouton + pour en ajouter une.",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("tasks_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    val projectName = projects.firstOrNull { it.id == task.projectId }?.name ?: "Chantier"
                    TaskCard(
                        task = task,
                        projectName = projectName,
                        onToggle = { onToggleTaskStatus(task) },
                        onEdit = {
                            editingTask = task
                            showDialog = true
                        },
                        onDelete = { onDeleteTask(task) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        TaskDialog(
            task = editingTask,
            projects = projects,
            defaultProjectId = selectedProjectId ?: projects.firstOrNull()?.id ?: "",
            onDismiss = {
                showDialog = false
                editingTask = null
            },
            onConfirm = { projId, title, desc, status, priority, due ->
                onSaveTask(editingTask?.id, projId, title, desc, status, priority, due)
                showDialog = false
                editingTask = null
            }
        )
    }
}

@Composable
fun TaskCard(
    task: TaskEntity,
    projectName: String,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDone = task.status.lowercase() == "done"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.testTag("toggle_task_button_${task.id}")
            ) {
                Icon(
                    imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Basculer l'état",
                    tint = if (isDone) StatusGreen else Slate400,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isDone) Slate400 else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = projectName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate600
                )

                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChantierBadge(status = task.status)
                    ChantierBadge(status = task.priority)
                    if (!task.dueDate.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Slate600,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = DomainUtils.formatDate(task.dueDate),
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate600
                            )
                        }
                    }
                }
            }

            Row {
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
fun TaskDialog(
    task: TaskEntity?,
    projects: List<ProjectEntity>,
    defaultProjectId: String,
    onDismiss: () -> Unit,
    onConfirm: (projectId: String, title: String, desc: String, status: String, priority: String, due: String?) -> Unit
) {
    var projectId by remember { mutableStateOf(task?.projectId ?: defaultProjectId) }
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var status by remember { mutableStateOf(task?.status ?: "todo") }
    var priority by remember { mutableStateOf(task?.priority ?: "normal") }
    var dueDate by remember { mutableStateOf(task?.dueDate ?: "") }

    var expandedProject by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }
    var expandedPriority by remember { mutableStateOf(false) }

    val statusList = listOf(
        "todo" to "À faire",
        "doing" to "En cours",
        "blocked" to "Bloqué",
        "done" to "Terminé"
    )
    val priorityList = listOf(
        "low" to "Basse",
        "normal" to "Normale",
        "high" to "Haute"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (task == null) "Nouvelle tâche" else "Modifier la tâche",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Project picker
                if (projects.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedProject,
                        onExpandedChange = { expandedProject = it }
                    ) {
                        val selectedProjectName = projects.firstOrNull { it.id == projectId }?.name ?: "Choisir un chantier"
                        OutlinedTextField(
                            value = selectedProjectName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Chantier associé *") },
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
                    label = { Text("Titre de la tâche *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_task_title")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status picker
                    ExposedDropdownMenuBox(
                        expanded = expandedStatus,
                        onExpandedChange = { expandedStatus = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = statusList.firstOrNull { it.first == status }?.second ?: status,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Statut") },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedStatus,
                            onDismissRequest = { expandedStatus = false }
                        ) {
                            statusList.forEach { (k, v) ->
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

                    // Priority picker
                    ExposedDropdownMenuBox(
                        expanded = expandedPriority,
                        onExpandedChange = { expandedPriority = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = priorityList.firstOrNull { it.first == priority }?.second ?: priority,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Priorité") },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedPriority,
                            onDismissRequest = { expandedPriority = false }
                        ) {
                            priorityList.forEach { (k, v) ->
                                DropdownMenuItem(
                                    text = { Text(v) },
                                    onClick = {
                                        priority = k
                                        expandedPriority = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Échéance (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

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
                    if (title.isNotBlank() && projectId.isNotBlank()) {
                        onConfirm(
                            projectId,
                            title.trim(),
                            description.trim(),
                            status,
                            priority,
                            dueDate.ifBlank { null }
                        )
                    }
                },
                enabled = title.isNotBlank() && projectId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                modifier = Modifier.testTag("submit_task_button")
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
