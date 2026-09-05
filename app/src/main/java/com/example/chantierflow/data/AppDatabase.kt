package com.example.chantierflow.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.chantierflow.model.ExpenseEntity
import com.example.chantierflow.model.OrganizationEntity
import com.example.chantierflow.model.PhotoEntity
import com.example.chantierflow.model.ProjectEntity
import com.example.chantierflow.model.ReportEntity
import com.example.chantierflow.model.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        OrganizationEntity::class,
        ProjectEntity::class,
        TaskEntity::class,
        ReportEntity::class,
        ExpenseEntity::class,
        PhotoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun organizationDao(): OrganizationDao
    abstract fun projectDao(): ProjectDao
    abstract fun taskDao(): TaskDao
    abstract fun reportDao(): ReportDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun photoDao(): PhotoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chantierflow_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            suspend fun populateInitialData(db: AppDatabase) {
                val defaultOrgId = "org-batir-pro-01"
                db.organizationDao().insert(
                    OrganizationEntity(
                        id = defaultOrgId,
                        name = "Bâtir Pro Rénovation",
                        role = "owner"
                    )
                )

                val proj1Id = "proj-villa-chene"
                val proj2Id = "proj-apt-victor-hugo"
                db.projectDao().insert(
                    ProjectEntity(
                        id = proj1Id,
                        organizationId = defaultOrgId,
                        name = "Rénovation Villa Les Chênes",
                        clientName = "M. et Mme Dupont",
                        address = "14 allée des Chênes, 33000 Bordeaux",
                        description = "Rénovation globale du rez-de-chaussée, cuisine ouverte et pose de parquet massif.",
                        status = "active",
                        startDate = "2025-02-01",
                        endDate = "2025-04-15"
                    )
                )
                db.projectDao().insert(
                    ProjectEntity(
                        id = proj2Id,
                        organizationId = defaultOrgId,
                        name = "Réhabilitation Appartement Victor Hugo",
                        clientName = "SCI Le Bel Horizon",
                        address = "42 avenue Victor Hugo, 75016 Paris",
                        description = "Cloisonnement, mise aux normes électriques et plomberie sanitaire.",
                        status = "planned",
                        startDate = "2025-03-01",
                        endDate = "2025-06-30"
                    )
                )

                // Tasks for Project 1
                db.taskDao().insert(
                    TaskEntity(
                        organizationId = defaultOrgId,
                        projectId = proj1Id,
                        title = "Démolition des cloisons séparatives",
                        description = "Évacuation des gravats vers la benne",
                        status = "done",
                        priority = "high",
                        dueDate = "2025-02-10"
                    )
                )
                db.taskDao().insert(
                    TaskEntity(
                        organizationId = defaultOrgId,
                        projectId = proj1Id,
                        title = "Pose du réseau d'évacuation cuisine",
                        description = "Raccordement au collecteur principal en 100mm",
                        status = "doing",
                        priority = "high",
                        dueDate = "2025-02-28"
                    )
                )
                db.taskDao().insert(
                    TaskEntity(
                        organizationId = defaultOrgId,
                        projectId = proj1Id,
                        title = "Passage des gaines électriques",
                        description = "Points lumineux et prises plan de travail",
                        status = "todo",
                        priority = "normal",
                        dueDate = "2025-03-08"
                    )
                )

                // Reports
                db.reportDao().insert(
                    ReportEntity(
                        organizationId = defaultOrgId,
                        projectId = proj1Id,
                        title = "Avancement plomberie et livraison matériaux",
                        content = "Réception de 15 sacs de ragréage et des raccords cuivre. Vérification des pentes d'écoulement sous la dalle.",
                        reportDate = "2025-02-26",
                        status = "validated"
                    )
                )

                // Expenses
                db.expenseDao().insert(
                    ExpenseEntity(
                        organizationId = defaultOrgId,
                        projectId = proj1Id,
                        title = "Location mini-benne 8m³ gravats",
                        supplier = "Bennes & Services Aquitaine",
                        category = "Location",
                        amountCents = 48500, // 485.00 €
                        expenseDate = "2025-02-05",
                        status = "paid"
                    )
                )
                db.expenseDao().insert(
                    ExpenseEntity(
                        organizationId = defaultOrgId,
                        projectId = proj1Id,
                        title = "Achat parquet chêne massif et sous-couche",
                        supplier = "Point.P Matériaux",
                        category = "Matériaux",
                        amountCents = 184050, // 1840.50 €
                        expenseDate = "2025-02-18",
                        status = "pending"
                    )
                )
            }
        }
    }
}
