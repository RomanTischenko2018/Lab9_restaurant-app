package com.example.restaurant.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/** База данных приложения (SQLite через Room). */
@Database(
    entities = [DishEntity::class, CartEntity::class, OrderEntity::class, OrderItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dishDao(): DishDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao

    companion object {
        private const val DB_NAME = "restaurant.db"

        /** Создаёт БД; при первом создании заполняет таблицу блюд начальным меню. */
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        SeedData.dishes.forEach { d ->
                            db.execSQL(
                                "INSERT INTO dishes (name, description, price, category) VALUES (?, ?, ?, ?)",
                                arrayOf<Any>(d.name, d.description, d.price, d.category)
                            )
                        }
                    }
                })
                .build()
    }
}
