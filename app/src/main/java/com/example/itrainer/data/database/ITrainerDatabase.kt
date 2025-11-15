package com.example.itrainer.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.itrainer.data.entities.Category
import com.example.itrainer.data.entities.GameDistribution
import com.example.itrainer.data.entities.Player
import com.example.itrainer.data.entities.Team
import com.example.itrainer.data.dao.CategoryDao
import com.example.itrainer.data.dao.PlayerDao
import com.example.itrainer.data.dao.*

import com.example.itrainer.data.dao.TeamDao

@Database(
    entities = [
        Category::class,
        Team::class,
        Player::class,
        GameDistribution::class
    ],
    version = 4
)
abstract class ITrainerDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun teamDao(): TeamDao
    abstract fun playerDao(): PlayerDao
    abstract fun gameDistributionDao(): GameDistributionDao

    companion object {
        @Volatile
        private var INSTANCE: ITrainerDatabase? = null

        fun getDatabase(context: Context): ITrainerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ITrainerDatabase::class.java,
                    "itrainer_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}