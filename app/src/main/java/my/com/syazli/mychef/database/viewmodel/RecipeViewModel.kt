package my.com.syazli.mychef.database.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.*
import kotlinx.coroutines.*
import my.com.syazli.mychef.database.dataclass.Recipe
import my.com.syazli.mychef.database.db.AppDatabase
import my.com.syazli.mychef.database.repository.RecipeRepository

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecipeRepository
    val allRecipes: LiveData<List<Recipe>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = RecipeRepository(db.recipeDao(), application)
        allRecipes = repository.allRecipes

        viewModelScope.launch(Dispatchers.IO) {
            repository.seedDatabaseIfEmpty()
        }
    }

    fun getRecipesByCategory(category: String): LiveData<List<Recipe>> {
        return repository.getRecipesByCategory(category)
    }


    fun getRecipeById(id: Int): LiveData<Recipe> {
        return repository.getRecipeById(id)
    }

    fun updateFood(food: Recipe) = viewModelScope.launch {
        repository.updateFood(food)
    }

    fun deleteRecipe(recipeId: Int) {
        viewModelScope.launch {
            repository.deleteRecipeById(recipeId)
        }
    }

    suspend fun saveRecipe(recipe: Recipe) = withContext(Dispatchers.IO) {
        repository.insert(recipe)
    }

    suspend fun getNextId(): Int = withContext(Dispatchers.IO) {
        repository.getNextId()
    }
}