package my.com.syazli.mychef.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import my.com.syazli.mychef.R
import my.com.syazli.mychef.adapters.CategoryAdapter
import my.com.syazli.mychef.adapters.FoodListAdapter
import my.com.syazli.mychef.database.viewmodel.RecipeViewModel
import my.com.syazli.mychef.models.CategoryModel
import my.com.syazli.mychef.models.FoodModel

class RecipePageViewFragment : Fragment() {

    private lateinit var rvFoods: RecyclerView
    private lateinit var gvCategories: GridView
    private lateinit var llSeeAll: LinearLayout
    private lateinit var tlSearch: MaterialCardView
    private lateinit var fabAdd: FloatingActionButton

    private val recipeViewModel: RecipeViewModel by viewModels()
    private lateinit var foodAdapter: FoodListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_main_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupCategories()
        setupFoods()
        setupClickListeners()
    }

    private fun initViews(view: View) {
        rvFoods = view.findViewById(R.id.rvFoods)
        gvCategories = view.findViewById(R.id.gv_product_type)
        llSeeAll = view.findViewById(R.id.ll_see_all)
        tlSearch = view.findViewById(R.id.tl_search)
        fabAdd = view.findViewById(R.id.fabAddRecipe)
    }

    private fun setupCategories() {
        val categoryNames = resources.getStringArray(R.array.category_type_list)
        val categoryImages = resources.obtainTypedArray(R.array.category_type_drawable)
        val categoryList = mutableListOf<CategoryModel>()

        for (i in categoryNames.indices) {
            val imageRes = categoryImages.getResourceId(i, 0)
            categoryList.add(CategoryModel(categoryNames[i], imageRes))
        }
        categoryImages.recycle()

        val adapter = CategoryAdapter(requireContext(), categoryList)
        gvCategories.adapter = adapter

        gvCategories.setOnItemClickListener { _, _, position, _ ->
            val selectedCategory = categoryList[position].name
            navigateToAllRecipes(true,  selectedCategory)
        }
    }


    private fun setupFoods() {
        foodAdapter = FoodListAdapter(requireContext(), recipeViewModel) { food ->
            openFoodRecipe(food.id)
        }

        rvFoods.layoutManager = LinearLayoutManager(requireContext())
        rvFoods.adapter = foodAdapter

        recipeViewModel.allRecipes.observe(viewLifecycleOwner) { recipes ->
            val favoriteFoods = recipes
                .filter { it.isFavorite }
                .map {
                    FoodModel(it.id, it.title, it.ingredients, it.duration, it.category, it.imageName, it.isFavorite)
                }
            foodAdapter.submitList(favoriteFoods)
        }
    }

    private fun navigateToAllRecipes(fromMain: Boolean, category: String? = null) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fl_main_activity, MainListViewFragment.newInstance(fromMain, category))
            .addToBackStack(null)
            .commit()
    }
    private fun setupClickListeners() {
        llSeeAll.setOnClickListener { navigateToAllRecipes(true) }
        tlSearch.setOnClickListener { navigateToAllRecipes(false) }
        fabAdd.setOnClickListener { navigateToAddRecipe() }
    }

    private fun navigateToAddRecipe() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fl_main_activity, AddRecipeFragment.newInstance(null,  null))
            .addToBackStack(null)
            .commit()
    }

    private fun openFoodRecipe(foodId: Int) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fl_main_activity, RecipeMainFragment.newInstance(foodId))
            .addToBackStack(null)
            .commit()
    }
}
