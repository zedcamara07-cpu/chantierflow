package com.example.chantierflow.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.chantierflow.model.ExpenseEntity
import com.example.chantierflow.model.OrganizationEntity
import com.example.chantierflow.model.PhotoEntity
import com.example.chantierflow.model.ProjectEntity
import com.example.chantierflow.model.ReportEntity
import com.example.chantierflow.model.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrganizationDao {
    @Query("SELECT * FROM organizations")
    fun getAllOrganizations(): Flow<List<OrganizationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(org: OrganizationEntity)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects WHERE organizationId = :orgId ORDER BY createdAt DESC")
    fun getProjects(orgId: String): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity)

    @Update
    suspend fun update(project: ProjectEntity)

    @Delete
    suspend fun delete(project: ProjectEntity)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE organizationId = :orgId ORDER BY createdAt DESC")
    fun getAllTasks(orgId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getTasksForProject(projectId: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)
}

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports WHERE organizationId = :orgId ORDER BY reportDate DESC, createdAt DESC")
    fun getAllReports(orgId: String): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE projectId = :projectId ORDER BY reportDate DESC, createdAt DESC")
    fun getReportsForProject(projectId: String): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: ReportEntity)

    @Update
    suspend fun update(report: ReportEntity)

    @Delete
    suspend fun delete(report: ReportEntity)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE organizationId = :orgId ORDER BY expenseDate DESC, createdAt DESC")
    fun getAllExpenses(orgId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE projectId = :projectId ORDER BY expenseDate DESC, createdAt DESC")
    fun getExpensesForProject(projectId: String): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity)

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)
}

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos WHERE organizationId = :orgId ORDER BY createdAt DESC")
    fun getAllPhotos(orgId: String): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getPhotosForProject(projectId: String): Flow<List<PhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: PhotoEntity)

    @Delete
    suspend fun delete(photo: PhotoEntity)
}
