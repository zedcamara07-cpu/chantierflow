package com.example.chantierflow.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "organizations")
data class OrganizationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val role: String = "owner" // "owner", "manager", "member"
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val organizationId: String,
    val name: String,
    val clientName: String = "",
    val address: String = "",
    val description: String = "",
    val status: String = "planned", // 'planned', 'active', 'paused', 'completed', 'archived'
    val startDate: String? = null,
    val endDate: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tasks",
    indices = [Index("projectId")]
)
data class TaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val organizationId: String,
    val projectId: String,
    val title: String,
    val description: String = "",
    val status: String = "todo", // 'todo', 'doing', 'blocked', 'done'
    val priority: String = "normal", // 'low', 'normal', 'high'
    val dueDate: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reports",
    indices = [Index("projectId")]
)
data class ReportEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val organizationId: String,
    val projectId: String,
    val title: String,
    val content: String,
    val reportDate: String, // YYYY-MM-DD
    val status: String = "draft", // 'draft', 'submitted', 'validated'
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "expenses",
    indices = [Index("projectId")]
)
data class ExpenseEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val organizationId: String,
    val projectId: String,
    val title: String,
    val supplier: String = "",
    val category: String = "Matériaux", // 'Matériaux', 'Main-d’œuvre', 'Location', 'Transport', 'Autre'
    val amountCents: Long, // e.g. 150000 = 1500.00 €
    val expenseDate: String, // YYYY-MM-DD
    val status: String = "pending", // 'pending', 'paid'
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "photos",
    indices = [Index("projectId")]
)
data class PhotoEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val organizationId: String,
    val projectId: String,
    val storagePath: String,
    val caption: String = "",
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
