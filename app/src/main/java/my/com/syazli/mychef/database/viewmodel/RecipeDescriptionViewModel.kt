package my.com.syazli.mychef.database.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import my.com.syazli.mychef.database.dataclass.RecipeDescription
import my.com.syazli.mychef.database.db.AppDatabase
import my.com.syazli.mychef.database.repository.RecipeDescriptionRepository

class RecipeDescriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecipeDescriptionRepository
    val allRecipes: LiveData<List<RecipeDescription>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = RecipeDescriptionRepository(db.recipeDescriptionDao(), application)
        allRecipes = repository.allRecipes

        viewModelScope.launch(Dispatchers.IO) {
            repository.seedDatabaseIfEmpty()
        }
    }

    fun getDescriptionByRecipeId(recipeId: Int): LiveData<RecipeDescription> {
        return repository.getDescriptionByRecipeId(recipeId)
    }

    suspend fun saveRecipeDescription(recipeDescription: RecipeDescription) = withContext(Dispatchers.IO) {
        repository.insert(recipeDescription)
    }

    suspend fun getNextId(): Int = withContext(Dispatchers.IO) {
        repository.getNextId()
    }
}