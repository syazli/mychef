package my.com.syazli.mychef.database.db

import android.content.Context
import androidx.room.*
import my.com.syazli.mychef.database.dao.RecipeDao
import my.com.syazli.mychef.database.dao.RecipeDescriptionDao
import my.com.syazli.mychef.database.dataclass.Recipe
import my.com.syazli.mychef.database.dataclass.RecipeDescription
import my.com.syazli.mychef.database.utility.Converters


@Database(entities = [Recipe::class, RecipeDescription::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun recipeDescriptionDao(): RecipeDescriptionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "recipe_database").build()
                INSTANCE = instance
                instance
            }
        }
    }
}