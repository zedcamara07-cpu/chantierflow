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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Euro
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
import com.example.chantierflow.model.ExpenseEntity
import com.example.chantierflow.model.ProjectEntity
import com.example.chantierflow.ui.components.ChantierBadge
import com.example.chantierflow.ui.components.EmptyState
import com.example.chantierflow.ui.components.ProjectFilterChips
import com.example.chantierflow.ui.components.StatCard
import com.example.chantierflow.ui.theme.Slate600
import com.example.chantierflow.ui.theme.StatusGreen
import com.example.chantierflow.ui.theme.StatusGreenBg
import com.example.chantierflow.ui.theme.StatusRed
import com.example.chantierflow.ui.theme.TealPrimary

@Composable
fun ExpensesScreen(
    expenses: List<ExpenseEntity>,
    projects: List<ProjectEntity>,
    selectedProjectId: String?,
    onSelectProject: (String?) -> Unit,
    onSaveExpense: (id: String?, projectId: String, title: String, supplier: String, category: String, amount: String, date: String, status: String) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

    val filteredExpenses = expenses.filter { expense ->
        selectedProjectId == null || expense.projectId == selectedProjectId
    }

    val totalCents = filteredExpenses.sumOf { it.amountCents }

    Column(modifier = modifier.fillMaxSize()) {
        ProjectFilterChips(
            projects = projects,
            selectedProjectId = selectedProjectId,
            onSelectProject = onSelectProject
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            StatCard(
                title = if (selectedProjectId == null) "Total toutes dépenses" else "Dépenses du chantier",
                value = DomainUtils.formatMoney(totalCents),
                icon = Icons.Default.Euro,
                modifier = Modifier.fillMaxWidth(),
                containerColor = StatusGreenBg,
                contentColor = StatusGreen
            )
        }

        if (filteredExpenses.isEmpty()) {
            EmptyState(
                message = "Aucune dépense enregistrée. Utilisez le bouton + pour saisir une facture ou un achat.",
                icon = Icons.Default.Euro,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("expenses_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredExpenses, key = { it.id }) { expense ->
                    val projectName = projects.firstOrNull { it.id == expense.projectId }?.name ?: "Chantier"
                    ExpenseCard(
                        expense = expense,
                        projectName = projectName,
                        onEdit = {
                            editingExpense = expense
                            showDialog = true
                        },
                        onDelete = { onDeleteExpense(expense) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        ExpenseDialog(
            expense = editingExpense,
            projects = projects,
            defaultProjectId = selectedProjectId ?: projects.firstOrNull()?.id ?: "",
            onDismiss = {
                showDialog = false
                editingExpense = null
            },
            onConfirm = { projId, title, supplier, category, amount, date, status ->
                onSaveExpense(editingExpense?.id, projId, title, supplier, category, amount, date, status)
                showDialog = false
                editingExpense = null
            }
        )
    }
}

@Composable
fun ExpenseCard(
    expense: ExpenseEntity,
    projectName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("expense_card_${expense.id}"),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$projectName • ${DomainUtils.formatDate(expense.expenseDate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate600
                    )
                }
                Text(
                    text = DomainUtils.formatMoney(expense.amountCents),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )
            }

            if (expense.supplier.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Fournisseur : ${expense.supplier}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChantierBadge(status = expense.category)
                    ChantierBadge(status = expense.status)
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDialog(
    expense: ExpenseEntity?,
    projects: List<ProjectEntity>,
    defaultProjectId: String,
    onDismiss: () -> Unit,
    onConfirm: (projectId: String, title: String, supplier: String, category: String, amount: String, date: String, status: String) -> Unit
) {
    var projectId by remember { mutableStateOf(expense?.projectId ?: defaultProjectId) }
    var title by remember { mutableStateOf(expense?.title ?: "") }
    var supplier by remember { mutableStateOf(expense?.supplier ?: "") }
    var category by remember { mutableStateOf(expense?.category ?: "Matériaux") }
    var amountText by remember {
        mutableStateOf(if (expense != null) (expense.amountCents / 100.0).toString() else "")
    }
    var expenseDate by remember { mutableStateOf(expense?.expenseDate ?: DomainUtils.localToday()) }
    var status by remember { mutableStateOf(expense?.status ?: "pending") }

    var expandedProject by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }
    var inputError by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Matériaux", "Main-d’œuvre", "Location", "Transport", "Autre")
    val statuses = listOf("pending" to "En attente", "paid" to "Payé")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (expense == null) "Nouvelle dépense" else "Modifier la dépense",
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
                    label = { Text("Libellé de la dépense *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_expense_title")
                )

                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("Fournisseur / Prestataire") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it
                            inputError = null
                        },
                        label = { Text("Montant (€) *") },
                        singleLine = true,
                        isError = inputError != null,
                        supportingText = if (inputError != null) { { Text(inputError!!) } } else null,
                        modifier = Modifier.weight(1f).testTag("input_expense_amount")
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandedCategory,
                        onExpandedChange = { expandedCategory = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Catégorie") },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        expandedCategory = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = expenseDate,
                        onValueChange = { expenseDate = it },
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
                            value = statuses.firstOrNull { it.first == status }?.second ?: status,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Paiement") },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedStatus,
                            onDismissRequest = { expandedStatus = false }
                        ) {
                            statuses.forEach { (k, v) ->
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        DomainUtils.toCents(amountText)
                        if (title.isNotBlank() && projectId.isNotBlank()) {
                            onConfirm(
                                projectId,
                                title.trim(),
                                supplier.trim(),
                                category,
                                amountText.trim(),
                                expenseDate.trim(),
                                status
                            )
                        }
                    } catch (e: Exception) {
                        inputError = e.message ?: "Format invalide"
                    }
                },
                enabled = title.isNotBlank() && amountText.isNotBlank() && projectId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                modifier = Modifier.testTag("submit_expense_button")
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
