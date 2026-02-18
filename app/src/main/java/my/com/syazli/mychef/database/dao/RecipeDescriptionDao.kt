package my.com.syazli.mychef.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import my.com.syazli.mychef.database.dataclass.Recipe
import my.com.syazli.mychef.database.dataclass.RecipeDescription
import my.com.syazli.mychef.database.utility.Constants.RECIPE_DESCRIPTION_TABLE

@Dao
interface RecipeDescriptionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(recipes: List<RecipeDescription>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(recipeDescription: RecipeDescription)

    @Query("SELECT * FROM $RECIPE_DESCRIPTION_TABLE")
    fun getAllRecipeDescription(): LiveData<List<RecipeDescription>>

    @Query("SELECT * FROM $RECIPE_DESCRIPTION_TABLE WHERE recipeId = :recipeId")
    fun getDescriptionByRecipeId(recipeId: Int): LiveData<RecipeDescription>

    @Query("SELECT COUNT(*) FROM $RECIPE_DESCRIPTION_TABLE")
    suspend fun getCount(): Int

    @Query("SELECT MAX(id) FROM $RECIPE_DESCRIPTION_TABLE")
    suspend fun getMaxId(): Int?

    @Upsert
    suspend fun upsertRecipeDescription(foodDescription: RecipeDescription)

}