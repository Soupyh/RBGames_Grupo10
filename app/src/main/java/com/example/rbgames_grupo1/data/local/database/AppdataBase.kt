package com.example.rbgames_grupo1.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.rbgames_grupo1.data.local.users.UserDao
import com.example.rbgames_grupo1.data.local.users.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// @Database registra entidades y versión del esquema.
// version = 1: como es primera inclusión con teléfono, partimos en 1.

@Database(entities = [UserEntity::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "bsd_rbgames"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'USUARIO'")
                // (Si ya tenías filas, quedan con role='USUARIO')
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    // .createFromAsset("database/bsd_rbgames.db") // No se debe usar createFromAsset con el callback onCreate para sembrar datos
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            seedAsync(context)
                        }
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // 👇 por si migraste o ya existía DB sin seed
                            seedIfEmptyAsync(context)
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }

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
                    role = "ADMIN"
                ),
                UserEntity(
                    name = "Benjamin Leal",
                    email = "benjamin@a.cl",
                    phone = "+56922222222",
                    password = "Benjamin123!",
                    role = "USUARIO"
                ),
                UserEntity(
                    name = "Soporte",
                    email = "soporte@a.cl",
                    phone = "+56933333333",
                    password = "Soporte123!",
                    role = "SOPORTE"
                )
            )
            seed.forEach { dao.insert(it) }
        }
    }
}
