package my.com.syazli.mychef.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider
import my.com.syazli.mychef.R
import my.com.syazli.mychef.adapters.FoodListAdapter
import my.com.syazli.mychef.database.viewmodel.RecipeViewModel
import my.com.syazli.mychef.models.FoodModel

class MainListViewFragment : Fragment() {

    private lateinit var btnBack: CardView
    private lateinit var rvFoods: RecyclerView
    private lateinit var etSearch: AutoCompleteTextView
    private lateinit var foodAdapter: FoodListAdapter
    private lateinit var btnFilter: CardView
    private lateinit var filterPanel: LinearLayout
    private lateinit var dimOverlay: View

    private lateinit var sliderIngredients: Slider
    private lateinit var sliderDuration: Slider
    private lateinit var tvIngredientValue: TextView
    private lateinit var tvDurationValue: TextView
    private lateinit var btnResetFilter: CardView
    private lateinit var btnApplyFilter: CardView
    private lateinit var btnClosePanel: ImageView
    private lateinit var chipGroup: LinearLayout

    private var maxIngredients: Int = 12
    private var maxDuration: Int = 90

    private val recipeViewModel: RecipeViewModel by viewModels()
    private var isFromMain = false
    private var category: String? = null
    private var foodList = listOf<FoodModel>()

    private val selectedCategories = mutableSetOf<String>()
    private lateinit var lottieLoading: LottieAnimationView


    companion object {
        fun newInstance(mIsFromMain: Boolean, mCategory: String? = null) = MainListViewFragment().apply {
            isFromMain = mIsFromMain
            category = mCategory
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lottieLoading = view.findViewById(R.id.lottieLoading)
        btnBack = view.findViewById(R.id.btnBack)
        rvFoods = view.findViewById(R.id.rvFoods)
        etSearch = view.findViewById(R.id.etSearch)
        btnFilter = view.findViewById(R.id.btnFilter)
        filterPanel = view.findViewById(R.id.filterPanel)
        dimOverlay = view.findViewById(R.id.dimOverlay)
        sliderIngredients = view.findViewById(R.id.sliderIngredients)
        sliderDuration = view.findViewById(R.id.sliderDuration)
        tvIngredientValue = view.findViewById(R.id.tvIngredientValue)
        tvDurationValue = view.findViewById(R.id.tvDurationValue)
        btnResetFilter = view.findViewById(R.id.btnResetFilter)
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter)
        btnClosePanel = view.findViewById(R.id.btnClosePanel)

        showLoading()

        setupRecyclerView()
        setupObservers()
        setupSearch()
        setupClickListeners()
        setupVisibility()
        setupFilterChips()
        setupPanelView()
        setupApplyFilterButton()
    }

    private fun setupRecyclerView() {
        foodAdapter = FoodListAdapter(requireContext(), recipeViewModel) { food ->
            openFoodRecipe(food.id)
        }

        rvFoods.layoutManager = LinearLayoutManager(requireContext())
        rvFoods.adapter = foodAdapter
    }

    private fun setupObservers() {
        recipeViewModel.allRecipes.observe(viewLifecycleOwner) { recipes ->
            foodList = recipes.map {
                FoodModel(it.id, it.title, it.ingredients, it.duration, it.category, it.imageName, it.isFavorite)
            }
            foodAdapter.submitList(foodList)
            filterBySelectedCategories()
            hideLoading()

        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    filterBySelectedCategories()
                    return
                }

                var filtered = foodList
                if (selectedCategories.isNotEmpty()) {
                    filtered = filtered.filter { it.category in selectedCategories }
                }

                category?.let { cat ->
                    filtered = filtered.filter { it.category == cat }
                }

                filtered = filtered.filter { it.ingredients <= maxIngredients }
                filtered = filtered.filter {
                    val mins = it.duration.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 0
                    mins <= maxDuration
                }

                filtered = filtered.filter {
                    it.title.contains(query, ignoreCase = true)
                }

                foodAdapter.submitList(filtered)
                rvFoods.visibility = View.VISIBLE
                updateResultCount(filtered.size)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }


    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupVisibility() {
        rvFoods.visibility = if (isFromMain) View.VISIBLE else View.GONE
    }

    private fun openFoodRecipe(foodId: Int) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fl_main_activity, RecipeMainFragment.newInstance(foodId))
            .addToBackStack(null)
            .commit()
    }

    private fun setupPanelView() {
        btnFilter.setOnClickListener {
            openFilterPanel()
        }
    }

    private fun openFilterPanel() {
        filterPanel.visibility = View.VISIBLE
        dimOverlay.visibility = View.VISIBLE
        filterPanel.animate()
            .translationX(0f)
            .setDuration(300)
            .start()
        dimOverlay.animate()
            .alpha(1f)
            .setDuration(300)
            .start()

        sliderIngredients.addOnChangeListener { _, value, _ ->
            maxIngredients = value.toInt()
            tvIngredientValue.text = "Up to $maxIngredients"
        }

        sliderDuration.addOnChangeListener { _, value, _ ->
            maxDuration = value.toInt()
            tvDurationValue.text = "Up to $maxDuration min"
        }

        btnClosePanel.setOnClickListener { closeFilterPanel() }

        btnResetFilter.setOnClickListener {
            selectedCategories.clear()
            maxIngredients = 12
            maxDuration = 90
            sliderIngredients.value = 12f
            sliderDuration.value = 90f
            tvIngredientValue.text = "Up to 12"
            tvDurationValue.text = "Up to 90 min"
            syncTopChipsWithPanel()
            syncPanelChipsWithTop()
            filterBySelectedCategories()
            closeFilterPanel()
        }
    }

    private fun setupApplyFilterButton() {
        btnApplyFilter.setOnClickListener {
            filterBySelectedCategories()
            closeFilterPanel()
        }
    }

    private fun setupFilterChips() {
        val categories = resources.getStringArray(R.array.category_type_list)
        chipGroup = view?.findViewById<LinearLayout>(R.id.filterChipGroup) ?: return

        chipGroup.removeAllViews()

        categories.forEach { category ->
            val chip = TextView(requireContext()).apply {
                text = category
                textSize = 13f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(Color.parseColor("#4CAF50"))
                gravity = Gravity.CENTER
                setPadding(40, 0, 40, 0)

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    40.dpToPx()
                ).also { it.marginEnd = 8.dpToPx() }

                setBackgroundResource(R.drawable.chip_unselectable_bg)

                setOnClickListener {
                    if (selectedCategories.contains(category)) {
                        selectedCategories.remove(category)
                        setBackgroundResource(R.drawable.chip_unselectable_bg)
                        setTextColor(Color.parseColor("#4CAF50"))
                    } else {
                        selectedCategories.add(category)
                        setBackgroundResource(R.drawable.chip_selectable_bg)
                        setTextColor(Color.WHITE)
                    }
                    filterBySelectedCategories()
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun filterBySelectedCategories() {
        var filtered = foodList

        category?.let { cat ->
            chipGroup.visibility = View.GONE
            filtered = filtered.filter { it.category == cat }
        }

        if (selectedCategories.isNotEmpty()) {
            filtered = filtered.filter { it.category in selectedCategories }
        }

        filtered = filtered.filter { food ->
            food.ingredients <= maxIngredients
        }

        filtered = filtered.filter { food ->
            val mins = food.duration.filter { it.isDigit() }.toIntOrNull() ?: 0
            mins <= maxDuration
        }

        foodAdapter.submitList(filtered)
        rvFoods.visibility = if (filtered.isEmpty() && selectedCategories.isEmpty() && category == null) {
            if (isFromMain) View.VISIBLE else View.GONE
        } else {
            View.VISIBLE
        }

        updateResultCount(filtered.size)
    }


    private fun updateResultCount(count: Int) {
        view?.findViewById<TextView>(R.id.tv_showing_result)?.text =
            getString(R.string.showing_results, count.toString())
    }

    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()

    private fun syncTopChipsWithPanel() {}
    private fun syncPanelChipsWithTop() {}
    private fun closeFilterPanel() {
        filterPanel.visibility = View.GONE
        dimOverlay.visibility = View.GONE
    }

    private fun showLoading() {
        lottieLoading.visibility = View.VISIBLE
        lottieLoading.playAnimation()
    }

    private fun hideLoading() {
        lottieLoading.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                lottieLoading.cancelAnimation()
                lottieLoading.visibility = View.GONE
                lottieLoading.alpha = 1f
            }
            .start()
    }


}
