package my.com.syazli.mychef.database.dataclass

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.*
import kotlinx.android.parcel.Parcelize
import my.com.syazli.mychef.database.utility.Constants

@Parcelize
@Entity(
    tableName = Constants.RECIPE_DESCRIPTION_TABLE,
    foreignKeys = [ForeignKey( Recipe::class, ["id"], ["recipeId"], ForeignKey.CASCADE)],
    indices = [Index(value = ["recipeId"])]
)
data class RecipeDescription(
    @PrimaryKey val id: Int,
    val recipeId: Int,
    val about: String,
    val ingredients: List<String>,
    val instructions: List<String>
) : Parcelable