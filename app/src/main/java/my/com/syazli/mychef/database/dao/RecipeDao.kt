package my.com.syazli.mychef.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.*
import my.com.syazli.mychef.database.dataclass.Recipe
import my.com.syazli.mychef.database.utility.Constants.RECIPE_TABLE

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(recipes: List<Recipe>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(recipe: Recipe)

    @Query("SELECT * FROM $RECIPE_TABLE")
    fun getAllRecipes(): LiveData<List<Recipe>>

    @Query("SELECT * FROM $RECIPE_TABLE WHERE id = :id")
    fun getRecipeById(id: Int): LiveData<Recipe>

    @Query("SELECT * FROM $RECIPE_TABLE WHERE category = :category")
    fun getRecipesByCategory(category: String): LiveData<List<Recipe>>

    @Query("SELECT COUNT(*) FROM $RECIPE_TABLE")
    suspend fun getCount(): Int

    @Query("SELECT MAX(id) FROM $RECIPE_TABLE")
    suspend fun getMaxId(): Int?

    @Query("DELETE FROM $RECIPE_TABLE WHERE id = :recipeId")
    suspend fun deleteRecipeById(recipeId: Int)

    @Update
    suspend fun updateFood(food: Recipe)

    @Upsert
    suspend fun upsertRecipe(food: Recipe)

}