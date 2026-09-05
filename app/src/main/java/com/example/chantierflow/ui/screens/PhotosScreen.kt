package com.example.chantierflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chantierflow.model.PhotoEntity
import com.example.chantierflow.model.ProjectEntity
import com.example.chantierflow.ui.components.EmptyState
import com.example.chantierflow.ui.components.ProjectFilterChips
import com.example.chantierflow.ui.theme.Slate400
import com.example.chantierflow.ui.theme.Slate600
import com.example.chantierflow.ui.theme.StatusRed
import com.example.chantierflow.ui.theme.TealLight
import com.example.chantierflow.ui.theme.TealPrimary

@Composable
fun PhotosScreen(
    photos: List<PhotoEntity>,
    projects: List<ProjectEntity>,
    selectedProjectId: String?,
    onSelectProject: (String?) -> Unit,
    onAddPhoto: (projectId: String, caption: String, url: String?) -> Unit,
    onDeletePhoto: (PhotoEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    val filteredPhotos = photos.filter { photo ->
        selectedProjectId == null || photo.projectId == selectedProjectId
    }

    Column(modifier = modifier.fillMaxSize()) {
        ProjectFilterChips(
            projects = projects,
            selectedProjectId = selectedProjectId,
            onSelectProject = onSelectProject
        )

        if (filteredPhotos.isEmpty()) {
            EmptyState(
                message = "Aucune photo pour ce chantier. Utilisez le bouton + pour documenter l'avancement visuel.",
                icon = Icons.Default.Image,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("photos_grid"),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPhotos, key = { it.id }) { photo ->
                    val projectName = projects.firstOrNull { it.id == photo.projectId }?.name ?: "Chantier"
                    PhotoCard(
                        photo = photo,
                        projectName = projectName,
                        onDelete = { onDeletePhoto(photo) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        PhotoDialog(
            projects = projects,
            defaultProjectId = selectedProjectId ?: projects.firstOrNull()?.id ?: "",
            onDismiss = { showDialog = false },
            onConfirm = { projId, caption, url ->
                onAddPhoto(projId, caption, url)
                showDialog = false
            }
        )
    }
}

@Composable
fun PhotoCard(
    photo: PhotoEntity,
    projectName: String,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("photo_card_${photo.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(TealLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = if (photo.caption.isNotBlank()) photo.caption else "Photo de chantier",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = projectName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = StatusRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDialog(
    projects: List<ProjectEntity>,
    defaultProjectId: String,
    onDismiss: () -> Unit,
    onConfirm: (projectId: String, caption: String, url: String?) -> Unit
) {
    var projectId by remember { mutableStateOf(defaultProjectId) }
    var caption by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var expandedProject by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter une photo de chantier", fontWeight = FontWeight.Bold) },
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
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Légende / Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_photo_caption")
                )

                OutlinedTextField(
                    value = photoUrl,
                    onValueChange = { photoUrl = it },
                    label = { Text("Lien ou référence photo (facultatif)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (projectId.isNotBlank()) {
                        onConfirm(projectId, caption.trim(), photoUrl.ifBlank { null })
                    }
                },
                enabled = projectId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                modifier = Modifier.testTag("submit_photo_button")
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
