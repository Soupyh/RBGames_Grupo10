package com.example.rbgames_grupo1.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.rbgames_grupo1.data.local.users.UserDao
import com.example.rbgames_grupo1.data.local.users.UserEntity
import com.example.rbgames_grupo1.data.local.reports.ReportDao
import com.example.rbgames_grupo1.data.local.reports.ReportEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



@Database(
    entities = [UserEntity::class, ReportEntity::class],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    // DAOs
    abstract fun userDao(): UserDao
    abstract fun reportDao(): ReportDao   // ⬅️ NUEVO

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "bsd_rbgames"

        // agrega columna role a users (por defecto USUARIO)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'USUARIO'")
            }
        }

        // agrega columna photoUri a users
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN photoUri TEXT")
            }
        }

        // crea tabla reports
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS reports (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userEmail TEXT NOT NULL,
                        subject   TEXT NOT NULL,
                        message   TEXT NOT NULL,
                        status    TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    // Encadena TODAS las migraciones conocidas
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Semilla inicial al crear DB por primera vez
                            seedAsync(context)
                        }
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Si ya existía DB, asegura semilla de usuarios si está vacía
                            seedIfEmptyAsync(context)
                        }
                    })
                    // .fallbackToDestructiveMigration() // usar solo en desarrollo si no te importan los datos
                    .build()
                INSTANCE = instance
                instance
            }

        // ---------- Seed ----------
        private fun seedAsync(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                seedInternal(getInstance(context).userDao())
            }
        }
        private fun seedIfEmptyAsync(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = getInstance(context).userDao()
                if (dao.count() == 0) seedInternal(dao)
            }
        }
        private suspend fun seedInternal(dao: UserDao) {
            val seed = listOf(
                UserEntity(
                    name = "Admin",
                    email = "a@a.cl",
                    phone = "+56911111111",
                    password = "Admin123!",
                    role = "ADMIN",
                    photoUri = null
                ),
                UserEntity(
                    name = "Benjamin Leal",
                    email = "benjamin@a.cl",
                    phone = "+56922222222",
                    password = "Benjamin123!",
                    role = "USUARIO",
                    photoUri = null
                ),
                UserEntity(
                    name = "Soporte",
                    email = "soporte@a.cl",
                    phone = "+56933333333",
                    password = "Soporte123!",
                    role = "SOPORTE",
                    photoUri = null
                )
            )
            seed.forEach { dao.insert(it) }
        }
    }
}
