package com.perksls.inesai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.perksls.inesai.data.local.dao.ConversationDao
import com.perksls.inesai.data.local.dao.MessageDao
import com.perksls.inesai.data.local.dao.ProviderDao
import com.perksls.inesai.data.local.entity.ConversationEntity
import com.perksls.inesai.data.local.entity.MessageEntity
import com.perksls.inesai.data.local.entity.ProviderEntity

@Database(
    entities = [ConversationEntity::class, MessageEntity::class, ProviderEntity::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun providerDao(): ProviderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // v1 → v2: criação da tabela providers
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS providers (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        baseUrl TEXT NOT NULL,
                        apiKey TEXT NOT NULL,
                        models TEXT NOT NULL,
                        isPrimary INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        // v2 → v3: adicionar coluna activeModel
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Adiciona coluna com default igual ao primeiro modelo (vazio se não houver)
                db.execSQL(
                    "ALTER TABLE providers ADD COLUMN activeModel TEXT NOT NULL DEFAULT ''"
                )
                // Preenche activeModel com o primeiro modelo da lista CSV existente
                // SQLite não tem split nativo, mas podemos usar substr+instr para o efeito
                db.execSQL(
                    """
                    UPDATE providers SET activeModel = 
                        CASE 
                            WHEN instr(models, ',') > 0 THEN substr(models, 1, instr(models, ',') - 1)
                            ELSE models
                        END
                    WHERE activeModel = ''
                    """.trimIndent()
                )
            }
        }


        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE providers ADD COLUMN isOpenAICompatible INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE providers ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
                // Inicializar sortOrder pela ordem de criação
                db.execSQL("UPDATE providers SET sortOrder = (SELECT COUNT(*) FROM providers p2 WHERE p2.createdAt <= providers.createdAt) - 1")
            }
        }

        
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE messages ADD COLUMN fileName TEXT")
                db.execSQL("ALTER TABLE messages ADD COLUMN fileContent TEXT")
            }
        }

                fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inesai_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
