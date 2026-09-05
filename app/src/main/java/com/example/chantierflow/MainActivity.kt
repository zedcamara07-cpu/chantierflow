package com.example.chantierflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chantierflow.ui.ChantierFlowViewModel
import com.example.chantierflow.ui.screens.ExpenseDialog
import com.example.chantierflow.ui.screens.ExpensesScreen
import com.example.chantierflow.ui.screens.OrganizationScreen
import com.example.chantierflow.ui.screens.PhotoDialog
import com.example.chantierflow.ui.screens.PhotosScreen
import com.example.chantierflow.ui.screens.ProjectDialog
import com.example.chantierflow.ui.screens.ProjectsScreen
import com.example.chantierflow.ui.screens.ReportDialog
import com.example.chantierflow.ui.screens.ReportsScreen
import com.example.chantierflow.ui.screens.TaskDialog
import com.example.chantierflow.ui.screens.TasksScreen
import com.example.chantierflow.ui.theme.ChantierFlowTheme
import com.example.chantierflow.ui.theme.TealLight
import com.example.chantierflow.ui.theme.TealPrimary
import kotlinx.coroutines.flow.collectLatest

enum class ScreenTab(val title: String, val icon: ImageVector) {
    PROJECTS("Chantiers", Icons.Default.Business),
    TASKS("Tâches", Icons.Default.Assignment),
    REPORTS("Journal", Icons.Default.Description),
    EXPENSES("Dépenses", Icons.Default.Euro),
    PHOTOS("Photos", Icons.Default.Image),
    ORGANIZATION("Entreprise", Icons.Default.Menu)
}

class MainActivity : ComponentActivity() {

    private val viewModel: ChantierFlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ChantierFlowTheme {
                ChantierFlowApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChantierFlowApp(viewModel: ChantierFlowViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf(ScreenTab.PROJECTS) }
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddProjectDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddReportDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddPhotoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ChantierFlow",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(
                        onClick = { currentTab = ScreenTab.ORGANIZATION },
                        modifier = Modifier.testTag("nav_organization_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Organisation",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TealPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                listOf(
                    ScreenTab.PROJECTS,
                    ScreenTab.TASKS,
                    ScreenTab.REPORTS,
                    ScreenTab.EXPENSES,
                    ScreenTab.PHOTOS
                ).forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            indicatorColor = TealLight
                        ),
                        modifier = Modifier.testTag("bottom_nav_${tab.name.lowercase()}")
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentTab != ScreenTab.ORGANIZATION) {
                FloatingActionButton(
                    onClick = {
                        when (currentTab) {
                            ScreenTab.PROJECTS -> showAddProjectDialog = true
                            ScreenTab.TASKS -> showAddTaskDialog = true
                            ScreenTab.REPORTS -> showAddReportDialog = true
                            ScreenTab.EXPENSES -> showAddExpenseDialog = true
                            ScreenTab.PHOTOS -> showAddPhotoDialog = true
                            ScreenTab.ORGANIZATION -> {}
                        }
                    },
                    containerColor = TealPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_item")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ajouter"
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                when (currentTab) {
                    ScreenTab.PROJECTS -> {
                        val totalExpenses = uiState.expenses.sumOf { it.amountCents }
                        ProjectsScreen(
                            projects = uiState.projects,
                            tasks = uiState.tasks,
                            totalExpensesCents = totalExpenses,
                            onSaveProject = { id, name, client, addr, desc, status, start, end ->
                                viewModel.saveProject(id, name, client, addr, desc, status, start, end)
                            },
                            onDeleteProject = { viewModel.deleteProject(it) },
                            onNavigateToTasks = { projectId ->
                                viewModel.selectProject(projectId)
                                currentTab = ScreenTab.TASKS
                            }
                        )
                    }
                    ScreenTab.TASKS -> {
                        TasksScreen(
                            tasks = uiState.tasks,
                            projects = uiState.projects,
                            selectedProjectId = uiState.selectedProjectId,
                            onSelectProject = { viewModel.selectProject(it) },
                            onToggleTaskStatus = { viewModel.toggleTaskStatus(it) },
                            onSaveTask = { id, projId, title, desc, status, priority, due ->
                                viewModel.saveTask(id, projId, title, desc, status, priority, due)
                            },
                            onDeleteTask = { viewModel.deleteTask(it) }
                        )
                    }
                    ScreenTab.REPORTS -> {
                        ReportsScreen(
                            reports = uiState.reports,
                            projects = uiState.projects,
                            selectedProjectId = uiState.selectedProjectId,
                            onSelectProject = { viewModel.selectProject(it) },
                            onSaveReport = { id, projId, title, content, date, status ->
                                viewModel.saveReport(id, projId, title, content, date, status)
                            },
                            onDeleteReport = { viewModel.deleteReport(it) }
                        )
                    }
                    ScreenTab.EXPENSES -> {
                        ExpensesScreen(
                            expenses = uiState.expenses,
                            projects = uiState.projects,
                            selectedProjectId = uiState.selectedProjectId,
                            onSelectProject = { viewModel.selectProject(it) },
                            onSaveExpense = { id, projId, title, supplier, cat, amt, date, status ->
                                viewModel.saveExpense(id, projId, title, supplier, cat, amt, date, status)
                            },
                            onDeleteExpense = { viewModel.deleteExpense(it) }
                        )
                    }
                    ScreenTab.PHOTOS -> {
                        PhotosScreen(
                            photos = uiState.photos,
                            projects = uiState.projects,
                            selectedProjectId = uiState.selectedProjectId,
                            onSelectProject = { viewModel.selectProject(it) },
                            onAddPhoto = { projId, caption, url ->
                                viewModel.addPhoto(projId, caption, url)
                            },
                            onDeletePhoto = { viewModel.deletePhoto(it) }
                        )
                    }
                    ScreenTab.ORGANIZATION -> {
                        OrganizationScreen(
                            currentOrg = uiState.currentOrg,
                            projectsCount = uiState.projects.size,
                            tasksCount = uiState.tasks.size
                        )
                    }
                }
            }
        }
    }

    // Contextual Add Dialogs from FAB
    if (showAddProjectDialog) {
        ProjectDialog(
            project = null,
            onDismiss = { showAddProjectDialog = false },
            onConfirm = { name, client, addr, desc, status, start, end ->
                viewModel.saveProject(null, name, client, addr, desc, status, start, end)
                showAddProjectDialog = false
            }
        )
    }

    if (showAddTaskDialog) {
        TaskDialog(
            task = null,
            projects = uiState.projects,
            defaultProjectId = uiState.selectedProjectId ?: uiState.projects.firstOrNull()?.id ?: "",
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { projId, title, desc, status, priority, due ->
                viewModel.saveTask(null, projId, title, desc, status, priority, due)
                showAddTaskDialog = false
            }
        )
    }

    if (showAddReportDialog) {
        ReportDialog(
            report = null,
            projects = uiState.projects,
            defaultProjectId = uiState.selectedProjectId ?: uiState.projects.firstOrNull()?.id ?: "",
            onDismiss = { showAddReportDialog = false },
            onConfirm = { projId, title, content, date, status ->
                viewModel.saveReport(null, projId, title, content, date, status)
                showAddReportDialog = false
            }
        )
    }

    if (showAddExpenseDialog) {
        ExpenseDialog(
            expense = null,
            projects = uiState.projects,
            defaultProjectId = uiState.selectedProjectId ?: uiState.projects.firstOrNull()?.id ?: "",
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { projId, title, supplier, cat, amt, date, status ->
                viewModel.saveExpense(null, projId, title, supplier, cat, amt, date, status)
                showAddExpenseDialog = false
            }
        )
    }

    if (showAddPhotoDialog) {
        PhotoDialog(
            projects = uiState.projects,
            defaultProjectId = uiState.selectedProjectId ?: uiState.projects.firstOrNull()?.id ?: "",
            onDismiss = { showAddPhotoDialog = false },
            onConfirm = { projId, caption, url ->
                viewModel.addPhoto(projId, caption, url)
                showAddPhotoDialog = false
            }
        )
    }
}
