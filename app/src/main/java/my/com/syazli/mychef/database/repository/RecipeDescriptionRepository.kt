package my.com.syazli.mychef.database.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import my.com.syazli.mychef.database.dao.RecipeDescriptionDao
import my.com.syazli.mychef.database.dataclass.RecipeDescription

class RecipeDescriptionRepository(private val dao: RecipeDescriptionDao, private val context: Context) {
    val allRecipes: LiveData<List<RecipeDescription>> = dao.getAllRecipeDescription()

    suspend fun seedDatabaseIfEmpty() {
        if (dao.getCount() == 0) {
            val recipes = loadRecipeDescriptionFromJson()
            dao.insertAll(recipes)
        }
    }

    private fun loadRecipeDescriptionFromJson(): List<RecipeDescription> {
        val jsonString = context.assets.open("recipedescription.json")
            .bufferedReader()
            .use { it.readText() }

        val type = object : TypeToken<List<RecipeDescription>>() {}.type
        return Gson().fromJson(jsonString, type)
    }

    fun getDescriptionByRecipeId(recipeId: Int): LiveData<RecipeDescription> {
        return dao.getDescriptionByRecipeId(recipeId)
    }

    suspend fun insert(recipeDescription: RecipeDescription) {
        dao.insert(recipeDescription)
    }

    suspend fun getNextId(): Int {
        return (dao.getMaxId() ?: 0) + 1
    }
}