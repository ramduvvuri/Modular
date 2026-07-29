package com.modular.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.modular.app.data.local.dao.ModeDao
import com.modular.app.data.local.dao.SessionDao
import com.modular.app.data.local.entity.AllowedAppEntity
import com.modular.app.data.local.entity.ModeEntity
import com.modular.app.data.local.entity.SessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ModeEntity::class, AllowedAppEntity::class, SessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ModularDatabase : RoomDatabase() {

    abstract fun modeDao(): ModeDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: ModularDatabase? = null

        fun getInstance(context: Context): ModularDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ModularDatabase::class.java,
                    "modular_database.db"
                )
                .addCallback(DatabaseCallback(context.applicationContext))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaultModes(database.modeDao())
                    }
                }
            }

            private suspend fun populateDefaultModes(modeDao: ModeDao) {
                val studyId = modeDao.insertMode(
                    ModeEntity(name = "Study", icon = "book")
                )
                val classId = modeDao.insertMode(
                    ModeEntity(name = "Class", icon = "school")
                )
                val nightId = modeDao.insertMode(
                    ModeEntity(name = "Night", icon = "bedtime")
                )

                // Optional default allowed app stubs
                modeDao.insertAllowedApps(
                    listOf(
                        AllowedAppEntity(modeId = studyId, packageName = "com.android.calculator2", appName = "Calculator"),
                        AllowedAppEntity(modeId = classId, packageName = "com.google.android.apps.docs", appName = "Google Docs")
                    )
                )
            }
        }
    }
}
