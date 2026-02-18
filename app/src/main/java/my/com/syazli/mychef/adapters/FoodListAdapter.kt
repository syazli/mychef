package my.com.syazli.mychef.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.nfc.Tag
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import my.com.syazli.mychef.R
import my.com.syazli.mychef.database.dataclass.Recipe
import my.com.syazli.mychef.database.viewmodel.RecipeViewModel
import my.com.syazli.mychef.databinding.ListItemFoodBinding
import my.com.syazli.mychef.models.FoodModel
import java.io.File

class FoodListAdapter(private val context: Context, private val recipeViewModel: RecipeViewModel, private val foodListListener: (FoodModel) -> Unit) : ListAdapter<FoodModel, FoodListAdapter.FoodViewHolder>(DiffCallback()) {

    private var TAG = javaClass.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ListItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FoodViewHolder(private val binding: ListItemFoodBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(food: FoodModel) {
            Log.d(TAG,  "bind: food = $food")
            binding.tvTitle.text = food.title
            binding.tvServing.text = "${food.ingredients} ingredients"
            binding.tvDuration.text = food.duration
            binding.tvCategory.text = food.category
            binding.tvCategory.setBackgroundResource(R.drawable.bg_category_badge)
            binding.tvCategory.background.setTint(getCategoryColor(food.category))

            val favoriteRes = if (food.isFavorite) R.drawable.ic_filled_heart else R.drawable.ic_empty_heart
            binding.ivFavorite.setImageResource(favoriteRes)

            val file = File(food.imageName)
            val imagePath = if (file.exists()) file else null
            Log.d(TAG,  "bind: imagePath = $imagePath")

            Glide.with(context)
                .load(imagePath ?: context.resources.getIdentifier(food.imageName, "drawable", context.packageName))
                .centerCrop()
                .placeholder(R.drawable.ic_empty_image)
                .error(R.drawable.ic_empty_image)
                .into(binding.imgFood)

            binding.root.setOnClickListener { foodListListener(food) }

            binding.ivFavorite.setOnClickListener {
                val newFavoriteStatus = !food.isFavorite
                food.isFavorite = newFavoriteStatus
                binding.ivFavorite.setImageResource(
                    if (newFavoriteStatus) R.drawable.ic_filled_heart
                    else R.drawable.ic_empty_heart
                )

                val recipe = Recipe(food.id, food.title, food.ingredients,food.duration, food.category, food.imageName, newFavoriteStatus)
                recipeViewModel.updateFood(recipe)
            }

        }
    }

    private fun getCategoryColor(category: String): Int {
        return when (category.lowercase()) {
            "salad" -> context.getColor(R.color.category_salad)
            "dessert" -> context.getColor(R.color.category_dessert)
            "drinks" -> context.getColor(R.color.category_drink)
            "main dish" -> context.getColor(R.color.category_main)
            "side dish" -> context.getColor(R.color.category_snack)
            "soup" -> context.getColor(R.color.category_soup)
            else -> context.getColor(R.color.category_default)
        }
    }


    class DiffCallback : DiffUtil.ItemCallback<FoodModel>() {
        override fun areItemsTheSame(oldItem: FoodModel, newItem: FoodModel): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: FoodModel, newItem: FoodModel): Boolean = oldItem == newItem
    }
}