package my.com.syazli.mychef.database.dataclass

import my.com.syazli.mychef.database.utility.Constants
import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = Constants.RECIPE_TABLE)
data class Recipe(

    @PrimaryKey val id: Int,
    var title: String,
    val ingredients: Int,
    val duration: String,
    val category: String,
    var imageName: String,
    var isFavorite: Boolean
) : Parcelable