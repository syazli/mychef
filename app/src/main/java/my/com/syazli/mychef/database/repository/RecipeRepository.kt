package my.com.syazli.mychef.database.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import my.com.syazli.mychef.database.dao.RecipeDao
import my.com.syazli.mychef.database.dataclass.Recipe

class RecipeRepository(private val dao: RecipeDao, private val context: Context) {
    val allRecipes: LiveData<List<Recipe>> = dao.getAllRecipes()

    suspend fun seedDatabaseIfEmpty() {
        if (dao.getCount() == 0) {
            val recipes = loadRecipesFromJson()
            dao.insertAll(recipes)
        }
    }

    private fun loadRecipesFromJson(): List<Recipe> {
        val jsonString = context.assets.open("recipetypes.json")
            .bufferedReader()
            .use { it.readText() }

        val type = object : TypeToken<List<Recipe>>() {}.type
        return Gson().fromJson(jsonString, type)
    }

    fun getRecipesByCategory(category: String): LiveData<List<Recipe>> {
        return dao.getRecipesByCategory(category)
    }

    fun getRecipeById(id: Int): LiveData<Recipe> {
        return dao.getRecipeById(id)
    }

    suspend fun updateFood(food: Recipe) {
        return dao.updateFood(food)
    }

    suspend fun deleteRecipeById(recipeId: Int) {
        dao.deleteRecipeById(recipeId)
    }

    suspend fun insert(recipe: Recipe) {
        dao.insert(recipe)
    }

    suspend fun getNextId(): Int {
        return (dao.getMaxId() ?: 0) + 1
    }
}