package com.example.eventticketapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.eventticketapp.data.local.converter.DateConverter
import com.example.eventticketapp.data.local.converter.TicketTypeConverter
import com.example.eventticketapp.data.local.dao.EventDao
import com.example.eventticketapp.data.local.dao.TicketDao
import com.example.eventticketapp.data.local.dao.UserDao
import com.example.eventticketapp.data.local.entity.EventEntity
import com.example.eventticketapp.data.local.entity.TicketEntity
import com.example.eventticketapp.data.local.entity.UserEntity

@Database(
    entities = [EventEntity::class, TicketEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class, TicketTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun ticketDao(): TicketDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "event_ticket_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
