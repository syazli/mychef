package my.com.syazli.mychef.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import my.com.syazli.mychef.R
import my.com.syazli.mychef.database.dataclass.Recipe
import my.com.syazli.mychef.database.dataclass.RecipeDescription
import my.com.syazli.mychef.database.viewmodel.RecipeDescriptionViewModel
import my.com.syazli.mychef.database.viewmodel.RecipeViewModel
import my.com.syazli.mychef.databinding.FragmentMainRecipesBinding
import my.com.syazli.mychef.helpers.DialogHelper
import java.io.File
import kotlin.random.Random

class RecipeMainFragment : Fragment() {

    private var _binding: FragmentMainRecipesBinding? = null
    private val binding get() = _binding!!

    private var recipeData: Recipe? = null
    private var recipeDescriptionData: RecipeDescription? = null
    private val recipeViewModel: RecipeViewModel by viewModels()
    private val recipeDescriptionViewModel: RecipeDescriptionViewModel by viewModels()

    private var recipeId: Int = -1

    companion object {
        private const val ARG_RECIPE_ID = "recipe_id"

        fun newInstance(recipeId: Int): RecipeMainFragment {
            return RecipeMainFragment().apply {
                arguments = Bundle().apply { putInt(ARG_RECIPE_ID, recipeId) }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recipeId = arguments?.getInt(ARG_RECIPE_ID) ?: return
        setupClickListeners()
        observeRecipe()
        observeRecipeDescription()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnDelete.setOnClickListener { showDeleteConfirmationDialog() }
        binding.btnEdit.setOnClickListener { editRecipe() }
        binding.btnFavorite.setOnClickListener { changeFavouriteStatus() }
    }

    private fun observeRecipe() {
        recipeViewModel.getRecipeById(recipeId).observe(viewLifecycleOwner) { recipe ->
            recipe ?: return@observe
            recipeData = recipe
            bindRecipe(recipe)
        }
    }

    private fun observeRecipeDescription() {
        recipeDescriptionViewModel.getDescriptionByRecipeId(recipeId).observe(viewLifecycleOwner) { detail ->
            detail ?: return@observe
            recipeDescriptionData = detail
            bindRecipeDescription(detail)
        }
    }

    private fun bindRecipe(recipe: Recipe) {
        val file = File(recipe.imageName)
        val imagePath = if (file.exists()) file else null

        val favoriteRes = if (recipe.isFavorite) R.drawable.ic_filled_heart else R.drawable.ic_empty_heart
        binding.btnFavorite.setImageResource(favoriteRes)

        Glide.with(this)
            .load(imagePath ?: requireContext().resources.getIdentifier(recipe.imageName, "drawable", requireContext().packageName))
            .centerCrop()
            .placeholder(R.drawable.ic_empty_image)
            .error(R.drawable.ic_empty_image)
            .into(binding.ivFoodImage)

        binding.tvTitle.text = recipe.title
        binding.tvServing.text = "${recipe.ingredients} ingredients"
        binding.tvDuration.text = recipe.duration
    }

    private fun bindRecipeDescription(detail: RecipeDescription) {
        binding.tvAbout.text = if (detail.about.isNotEmpty()) detail.about else "No About Added"
        setupIngredients(detail.ingredients)
        setupInstructions(detail.instructions)
    }

    private fun setupIngredients(ingredients: List<String>) {
        binding.ingredientsContainer.removeAllViews()

        ingredients.forEach { ingredient ->
            val cardView = CardView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.ingredient_card_size),
                    resources.getDimensionPixelSize(R.dimen.ingredient_card_size)
                ).also { it.marginEnd = 10.dpToPx() }
                radius = 14.dpToPx().toFloat()
                cardElevation = 0f
                setCardBackgroundColor(resources.getColor(R.color.ingredient_card_bg, null))
            }

            val textView = TextView(requireContext()).apply {
                text = ingredient
                gravity = android.view.Gravity.CENTER
                setTextColor(resources.getColor(R.color.black, null))
                textSize = 12f
                setPadding(6.dpToPx(), 6.dpToPx(), 6.dpToPx(), 6.dpToPx())
            }

            cardView.addView(textView)
            binding.ingredientsContainer.addView(cardView)
        }
    }

    private fun setupInstructions(instructions: List<String>) {
        binding.instructionsContainer.removeAllViews()

        instructions.forEachIndexed { index, step ->
            val stepRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 16.dpToPx() }
            }

            val stepNumber = TextView(requireContext()).apply {
                text = (index + 1).toString()
                gravity = android.view.Gravity.CENTER
                setTextColor(resources.getColor(android.R.color.white, null))
                textSize = 13f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setBackgroundResource(R.drawable.step_number_bg)
                layoutParams = LinearLayout.LayoutParams(28.dpToPx(), 28.dpToPx()).also {
                    it.marginEnd = 12.dpToPx()
                    it.topMargin = 2.dpToPx()
                }
            }

            val stepText = TextView(requireContext()).apply {
                text = step
                setTextColor(resources.getColor(R.color.step_text_color, null))
                textSize = 14f
                setLineSpacing(0f, 1.5f)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            stepRow.addView(stepNumber)
            stepRow.addView(stepText)
            binding.instructionsContainer.addView(stepRow)
        }
    }

    private fun showDeleteConfirmationDialog() {
        DialogHelper().showConfirmDialog(requireActivity(), getString(R.string.delete_title), getString(R.string.delete_subtitle), getString(R.string.delete), getString(R.string.cancel), object: DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                deleteRecipe()
            }
        }, null)
    }

    private fun deleteRecipe() {
        recipeViewModel.deleteRecipe(recipeId)
        DialogHelper().showWarningDialog(requireContext(), getString(R.string.success_delete), getString(R.string.success_delete_sub), object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                parentFragmentManager.popBackStack()
            }
        })
    }

    private fun editRecipe() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fl_main_activity, AddRecipeFragment.newInstance(recipeData,  recipeDescriptionData))
            .addToBackStack(null)
            .commit()
    }

    private fun changeFavouriteStatus() {
        recipeData?.let { data ->
            val newFavoriteStatus = !data.isFavorite
            data.isFavorite = newFavoriteStatus
            binding.btnFavorite.setImageResource(
                if (newFavoriteStatus) R.drawable.ic_filled_heart else R.drawable.ic_empty_heart
            )

            val recipe = Recipe(data.id, data.title, data.ingredients, data.duration, data.category, data.imageName, newFavoriteStatus)
            recipeViewModel.updateFood(recipe)
        }


    }
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}