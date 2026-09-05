package com.example.chantierflow.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chantierflow.data.AppDatabase
import com.example.chantierflow.domain.DomainUtils
import com.example.chantierflow.model.ExpenseEntity
import com.example.chantierflow.model.OrganizationEntity
import com.example.chantierflow.model.PhotoEntity
import com.example.chantierflow.model.ProjectEntity
import com.example.chantierflow.model.ReportEntity
import com.example.chantierflow.model.TaskEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChantierFlowUiState(
    val organizations: List<OrganizationEntity> = emptyList(),
    val currentOrg: OrganizationEntity? = null,
    val projects: List<ProjectEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val reports: List<ReportEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val photos: List<PhotoEntity> = emptyList(),
    val selectedProjectId: String? = null,
    val isLoading: Boolean = false
)

class ChantierFlowViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val orgDao = database.organizationDao()
    private val projectDao = database.projectDao()
    private val taskDao = database.taskDao()
    private val reportDao = database.reportDao()
    private val expenseDao = database.expenseDao()
    private val photoDao = database.photoDao()

    private val _selectedProjectId = MutableStateFlow<String?>(null)
    val selectedProjectId: StateFlow<String?> = _selectedProjectId.asStateFlow()

    private val _currentOrgId = MutableStateFlow("org-batir-pro-01")

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    val uiState: StateFlow<ChantierFlowUiState> = combine(
        orgDao.getAllOrganizations(),
        projectDao.getProjects("org-batir-pro-01"),
        taskDao.getAllTasks("org-batir-pro-01"),
        reportDao.getAllReports("org-batir-pro-01"),
        expenseDao.getAllExpenses("org-batir-pro-01"),
        photoDao.getAllPhotos("org-batir-pro-01"),
        _selectedProjectId
    ) { orgs, projects, tasks, reports, expenses, photos, selectedProj ->
        val currentOrg = orgs.firstOrNull { it.id == _currentOrgId.value } ?: orgs.firstOrNull()
        ChantierFlowUiState(
            organizations = orgs,
            currentOrg = currentOrg,
            projects = projects,
            tasks = tasks,
            reports = reports,
            expenses = expenses,
            photos = photos,
            selectedProjectId = selectedProj,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChantierFlowUiState(isLoading = true)
    )

    fun selectProject(projectId: String?) {
        _selectedProjectId.value = projectId
    }

    // Projects CRUD
    fun saveProject(
        id: String?,
        name: String,
        clientName: String,
        address: String,
        description: String,
        status: String,
        startDate: String?,
        endDate: String?
    ) {
        viewModelScope.launch {
            val orgId = uiState.value.currentOrg?.id ?: "org-batir-pro-01"
            if (id == null) {
                val newProject = ProjectEntity(
                    organizationId = orgId,
                    name = name,
                    clientName = clientName,
                    address = address,
                    description = description,
                    status = status,
                    startDate = startDate,
                    endDate = endDate
                )
                projectDao.insert(newProject)
                _userMessage.emit("Chantier « $name » créé avec succès.")
            } else {
                val existing = projectDao.getProjectById(id)
                if (existing != null) {
                    val updated = existing.copy(
                        name = name,
                        clientName = clientName,
                        address = address,
                        description = description,
                        status = status,
                        startDate = startDate,
                        endDate = endDate,
                        updatedAt = System.currentTimeMillis()
                    )
                    projectDao.update(updated)
                    _userMessage.emit("Chantier « $name » mis à jour.")
                }
            }
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            projectDao.delete(project)
            if (_selectedProjectId.value == project.id) {
                _selectedProjectId.value = null
            }
            _userMessage.emit("Chantier « ${project.name} » supprimé.")
        }
    }

    // Tasks CRUD
    fun saveTask(
        id: String?,
        projectId: String,
        title: String,
        description: String,
        status: String,
        priority: String,
        dueDate: String?
    ) {
        viewModelScope.launch {
            val orgId = uiState.value.currentOrg?.id ?: "org-batir-pro-01"
            if (id == null) {
                val newTask = TaskEntity(
                    organizationId = orgId,
                    projectId = projectId,
                    title = title,
                    description = description,
                    status = status,
                    priority = priority,
                    dueDate = dueDate
                )
                taskDao.insert(newTask)
                _userMessage.emit("Tâche ajoutée.")
            } else {
                val updatedTask = TaskEntity(
                    id = id,
                    organizationId = orgId,
                    projectId = projectId,
                    title = title,
                    description = description,
                    status = status,
                    priority = priority,
                    dueDate = dueDate,
                    updatedAt = System.currentTimeMillis()
                )
                taskDao.update(updatedTask)
                _userMessage.emit("Tâche mise à jour.")
            }
        }
    }

    fun toggleTaskStatus(task: TaskEntity) {
        viewModelScope.launch {
            val nextStatus = when (task.status.lowercase()) {
                "todo" -> "doing"
                "doing" -> "done"
                "done" -> "todo"
                else -> "todo"
            }
            taskDao.update(task.copy(status = nextStatus, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            taskDao.delete(task)
            _userMessage.emit("Tâche supprimée.")
        }
    }

    // Reports CRUD
    fun saveReport(
        id: String?,
        projectId: String,
        title: String,
        content: String,
        reportDate: String,
        status: String
    ) {
        viewModelScope.launch {
            val orgId = uiState.value.currentOrg?.id ?: "org-batir-pro-01"
            if (id == null) {
                val newReport = ReportEntity(
                    organizationId = orgId,
                    projectId = projectId,
                    title = title,
                    content = content,
                    reportDate = reportDate,
                    status = status
                )
                reportDao.insert(newReport)
                _userMessage.emit("Rapport journalier enregistré.")
            } else {
                val updatedReport = ReportEntity(
                    id = id,
                    organizationId = orgId,
                    projectId = projectId,
                    title = title,
                    content = content,
                    reportDate = reportDate,
                    status = status,
                    updatedAt = System.currentTimeMillis()
                )
                reportDao.update(updatedReport)
                _userMessage.emit("Rapport journalier mis à jour.")
            }
        }
    }

    fun deleteReport(report: ReportEntity) {
        viewModelScope.launch {
            reportDao.delete(report)
            _userMessage.emit("Rapport supprimé.")
        }
    }

    // Expenses CRUD
    fun saveExpense(
        id: String?,
        projectId: String,
        title: String,
        supplier: String,
        category: String,
        amountInput: String,
        expenseDate: String,
        status: String
    ) {
        viewModelScope.launch {
            try {
                val amountCents = DomainUtils.toCents(amountInput)
                val orgId = uiState.value.currentOrg?.id ?: "org-batir-pro-01"
                if (id == null) {
                    val newExpense = ExpenseEntity(
                        organizationId = orgId,
                        projectId = projectId,
                        title = title,
                        supplier = supplier,
                        category = category,
                        amountCents = amountCents,
                        expenseDate = expenseDate,
                        status = status
                    )
                    expenseDao.insert(newExpense)
                    _userMessage.emit("Dépense de ${DomainUtils.formatMoney(amountCents)} enregistrée.")
                } else {
                    val updatedExpense = ExpenseEntity(
                        id = id,
                        organizationId = orgId,
                        projectId = projectId,
                        title = title,
                        supplier = supplier,
                        category = category,
                        amountCents = amountCents,
                        expenseDate = expenseDate,
                        status = status,
                        updatedAt = System.currentTimeMillis()
                    )
                    expenseDao.update(updatedExpense)
                    _userMessage.emit("Dépense mise à jour.")
                }
            } catch (e: Exception) {
                _userMessage.emit(e.message ?: "Erreur de saisie")
            }
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseDao.delete(expense)
            _userMessage.emit("Dépense supprimée.")
        }
    }

    // Photos CRUD
    fun addPhoto(projectId: String, caption: String, url: String?) {
        viewModelScope.launch {
            val orgId = uiState.value.currentOrg?.id ?: "org-batir-pro-01"
            val path = "$orgId/$projectId/${System.currentTimeMillis()}.jpg"
            val photo = PhotoEntity(
                organizationId = orgId,
                projectId = projectId,
                storagePath = path,
                caption = caption,
                photoUrl = url
            )
            photoDao.insert(photo)
            _userMessage.emit("Photo ajoutée au chantier.")
        }
    }

    fun deletePhoto(photo: PhotoEntity) {
        viewModelScope.launch {
            photoDao.delete(photo)
            _userMessage.emit("Photo supprimée.")
        }
    }
}
