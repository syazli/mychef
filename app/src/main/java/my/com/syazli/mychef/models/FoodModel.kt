package my.com.syazli.mychef.models

data class FoodModel (
    val id: Int,
    val title: String,
    val ingredients: Int,
    val duration: String,
    val category: String,
    val imageName: String,
    var isFavorite: Boolean
)
