package my.com.syazli.mychef.database.utility

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import my.com.syazli.mychef.database.dataclass.Recipe
import my.com.syazli.mychef.database.dataclass.RecipeDescription
import my.com.syazli.mychef.database.db.AppDatabase

object LocalHelper {

    private const val TAG = "LocalHelper"

    private val myScope = CoroutineScope(SupervisorJob())
    private val myDispatcher: CoroutineDispatcher = Dispatchers.IO

    fun saveNewRecipe(context: Context, recipe: Recipe, recipeDescription: RecipeDescription, onResult: (success: Boolean) -> Unit) {
        myScope.launch {
            try {
                val db = AppDatabase.getDatabase(context)

                val recipeId = withContext(myDispatcher) {
                    if (recipe.id != 0) recipe.id
                    else (db.recipeDao().getMaxId() ?: 0) + 1
                }

                val descriptionId = withContext(myDispatcher) {
                    if (recipeDescription.id != 0) recipeDescription.id
                    else (db.recipeDescriptionDao().getMaxId() ?: 0) + 1
                }

                val finalRecipe = recipe.copy(
                    id = recipeId,
                    ingredients = recipeDescription.ingredients.size
                )

                val finalDescription = recipeDescription.copy(
                    id = descriptionId,
                    recipeId = recipeId
                )

                withContext(myDispatcher) {
                    db.recipeDao().upsertRecipe(finalRecipe)
                    db.recipeDescriptionDao().upsertRecipeDescription(finalDescription)
                }
                onResult(true)

            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

}