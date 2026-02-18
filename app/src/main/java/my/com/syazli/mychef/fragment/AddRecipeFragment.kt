package my.com.syazli.mychef.fragment

import android.Manifest
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.nfc.Tag
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import my.com.syazli.mychef.MyChefApplication
import my.com.syazli.mychef.R
import my.com.syazli.mychef.activities.ImagePickerActivity
import my.com.syazli.mychef.adapter.StepAdapter
import my.com.syazli.mychef.database.dataclass.Recipe
import my.com.syazli.mychef.database.dataclass.RecipeDescription
import my.com.syazli.mychef.database.utility.LocalHelper
import my.com.syazli.mychef.databinding.FragmentAddRecipeBinding
import my.com.syazli.mychef.dialog.ActionListDialog
import my.com.syazli.mychef.helpers.DialogHelper
import my.com.syazli.mychef.helpers.ImageHelper
import my.com.syazli.mychef.helpers.PermissionHelper
import java.io.InputStream
import java.io.File
import java.io.FileOutputStream


class AddRecipeFragment : Fragment() {

    private val TAG = javaClass.simpleName
    private var _binding: FragmentAddRecipeBinding? = null
    private val binding get() = _binding!!

    private var permissionLauncher: ActivityResultLauncher<String>? = null
    private var imageLauncher: ActivityResultLauncher<Intent>? = null
    private var profileBitmap: Bitmap? = null

    private var isEditMode = false
    private var existingRecipe: Recipe? = null
    private var existingRecipeDesc: RecipeDescription? = null


    private lateinit var stepAdapter: StepAdapter

    companion object {
        private const val ARG_IS_EDIT = "arg_is_edit"
        private const val ARG_RECIPE = "arg_recipe"
        private const val ARG_RECIPE_DESC = "arg_recipe_desc"

        fun newInstance(recipe: Recipe?, recipeDesc: RecipeDescription?): AddRecipeFragment {
            val fragment = AddRecipeFragment()
            val args = Bundle().apply {
                putBoolean(ARG_IS_EDIT, recipe != null)
                putParcelable(ARG_RECIPE, recipe)
                putParcelable(ARG_RECIPE_DESC, recipeDesc)
            }
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("title", binding.etRecipeTitle.text.toString())
        outState.putString("duration", binding.etDuration.text.toString())
        outState.putString("description", binding.etDescription.text.toString())
        outState.putInt("scrollY", binding.scrollView.scrollY)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            binding.etRecipeTitle.setText(it.getString("title"))
            binding.etDuration.setText(it.getString("duration"))
            binding.etDescription.setText(it.getString("description"))
            binding.scrollView.post { binding.scrollView.scrollTo(0, it.getInt("scrollY")) }
        }

        arguments?.let {
            isEditMode = it.getBoolean(ARG_IS_EDIT, false)
            existingRecipe = it.getParcelable(ARG_RECIPE)
            existingRecipeDesc = it.getParcelable(ARG_RECIPE_DESC)
        }

        setupIngredients()
        setupSteps()
        setupCategoryChips()
        clickListener()
        initActivityResultLauncher()

        if (isEditMode) {
            populateFields()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initActivityResultLauncher() {
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.e(TAG, "initActivityResultLauncher: permissionLauncher granted = $granted")
            if (granted) {
                checkCameraPermission()
            } else {
                showAppSettingsDialog(getString(R.string.permission_setting_camera_permission), getString(R.string.permission_setting_camera_dialog))
            }
        }

        imageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.e(TAG, "initActivityResultLauncher: result = $result")
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val uri = result.data?.getStringExtra("path")?.toUri()
                refreshImage(uri)

            } else {
                DialogHelper().showConfirmDialog(requireActivity(), getString(R.string.error), getString(R.string.error_update_image), getString(R.string.retry), getString(R.string.cancel), object: DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        checkCameraPermission()
                    }
                }, null)
            }
        }
    }

    private fun showAppSettingsDialog(title: String, message: String) {
        DialogHelper().showConfirmDialog(requireActivity(), title, message, MyChefApplication.getAppInstance().getString(R.string.app_settings), MyChefApplication.getAppInstance().getString(R.string.cancel), { dialog, which ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireActivity().packageName, null)
            intent.setData(uri)
            startActivity(intent)
        }, null)
    }

    private fun populateFields() {
        existingRecipe?.let { recipe ->
            binding.etRecipeTitle.setText(recipe.title)
            binding.etDuration.setText(recipe.duration)
            if (recipe.imageName.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeFile(recipe.imageName)
                binding.ivRecipeImage.setImageBitmap(bitmap)
                profileBitmap = bitmap
            }

            val chipGroup = binding.chipCategoryGroup
            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as? Chip
                chip?.isChecked = chip?.text?.toString() == recipe.category
            }
        }

        existingRecipeDesc?.let { desc ->
            binding.etDescription.setText(desc.about)

            binding.ingredientsContainer.removeAllViews()
            desc.ingredients.forEach { ingredient ->
                addIngredientRow(binding.ingredientsContainer)
                val lastRow = binding.ingredientsContainer.getChildAt(binding.ingredientsContainer.childCount - 1) as LinearLayout
                lastRow.findViewById<TextInputEditText>(R.id.etIngredient)?.setText(ingredient)
            }

//            stepAdapter.clearAllSteps()
            desc.instructions.forEach { step ->
                stepAdapter.addStep(step)
            }
        }
    }

    private fun setupCategoryChips() {
        val categories = resources.getStringArray(R.array.category_type_list)
        val chipGroup = binding.chipCategoryGroup

        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
                isClickable = true
                setChipBackgroundColorResource(R.color.chip_background_selector)
                setTextColor(resources.getColorStateList(R.color.chip_text_selector, null))
            }
            chipGroup.addView(chip)
        }

        (chipGroup.getChildAt(0) as? Chip)?.isChecked = true
    }

    private fun getSelectedCategory(): String? {
        val chipGroup = binding.chipCategoryGroup
        val checkedId = chipGroup.checkedChipId
        return if (checkedId != View.NO_ID) chipGroup.findViewById<Chip>(checkedId)?.text?.toString() else null
    }

    private fun setupIngredients() {
        val container = binding.ingredientsContainer
        addIngredientRow(container)
        addIngredientRow(container)

        binding.btnAddIngredient.setOnClickListener { addIngredientRow(container) }
    }

    private fun addIngredientRow(container: LinearLayout) {
        val row = LayoutInflater.from(requireContext()).inflate(R.layout.item_ingredient_row, container, false) as LinearLayout

        row.findViewById<ImageView>(R.id.btnRemoveIngredient).setOnClickListener {
            if (container.childCount > 1) container.removeView(row)
            else row.findViewById<TextInputEditText>(R.id.etIngredient)?.text?.clear()
        }

        container.addView(row)
//        scrollToBottom(row)
    }

    private fun collectIngredients(): List<String> {
        val container = binding.ingredientsContainer
        return (0 until container.childCount).mapNotNull { i ->
            (container.getChildAt(i) as? LinearLayout)
                ?.findViewById<TextInputEditText>(R.id.etIngredient)
                ?.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        }
    }

    private fun setupSteps() {
        stepAdapter = StepAdapter()
        binding.rvSteps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = stepAdapter
            isNestedScrollingEnabled = false
        }

        stepAdapter.attachTouchHelper(binding.rvSteps)

        if (!isEditMode) {
            stepAdapter.addStep()
            stepAdapter.addStep()
        }

        binding.btnAddStep.setOnClickListener {
            stepAdapter.addStep()
            binding.scrollView.post { binding.scrollView.smoothScrollTo(0, binding.rvSteps.bottom) }
        }
    }


    private fun collectSteps(): List<String> {
        stepAdapter.syncAllText(binding.rvSteps)
        return stepAdapter.collectSteps()
    }

    private fun validateRecipe(title: String, duration: String, ingredients: List<String>, steps: List<String>): Boolean {
        var isValid = true

        if (title.isEmpty()) { binding.tilRecipeTitle.error = "Recipe title is required"; isValid = false } else binding.tilRecipeTitle.error = null
        if (duration.isEmpty()) { binding.tilDuration.error = "Duration is required"; isValid = false } else binding.tilDuration.error = null
        if (ingredients.isEmpty()) { binding.tilIngredientsError.visibility = View.VISIBLE; binding.tilIngredientsError.error = "Please add at least one ingredient"; isValid = false } else { binding.tilIngredientsError.error = null; binding.tilIngredientsError.visibility = View.GONE }
        if (steps.isEmpty()) { binding.tilStepsError.visibility = View.VISIBLE; binding.tilStepsError.error = "Please add at least one step"; isValid = false } else { binding.tilStepsError.error = null; binding.tilStepsError.visibility = View.GONE }

        return isValid
    }

    private fun saveRecipe() {
        DialogHelper().showConfirmDialog(requireActivity(), getString(R.string.confirm_title), getString(R.string.confirm_subtitle), getString(R.string.confirm), getString(R.string.cancel), object: DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                val title = binding.etRecipeTitle.text?.toString()?.trim() ?: ""
                val duration = binding.etDuration.text?.toString()?.trim() ?: ""
                val description = binding.etDescription.text?.toString()?.trim() ?: ""
                val category = getSelectedCategory() ?: ""
                val ingredients = collectIngredients()
                val steps = collectSteps()

                if (!validateRecipe(title, duration, ingredients, steps)) return

                val imagePath = profileBitmap?.let { saveImageLocally(it) } ?: existingRecipe?.imageName ?: ""

                val recipeToSave = Recipe(if (isEditMode) existingRecipe!!.id else 0, title, 0, duration, category, imagePath, existingRecipe?.isFavorite ?: false)

                val recipeDescToSave = RecipeDescription(if (isEditMode) existingRecipeDesc!!.id else 0, recipeToSave.id, description, ingredients, steps)
                LocalHelper.saveNewRecipe(requireContext(), recipeToSave, recipeDescToSave) { success ->
                    requireActivity().runOnUiThread {
                        if (success) {
                            DialogHelper().showConfirmDialog(requireActivity(), getString(R.string.saved_success), getString(R.string.save_success_subtitle), getString(R.string.confirm), getString(R.string.cancel), object: DialogInterface.OnClickListener {
                                override fun onClick(dialog: DialogInterface?, which: Int) {
                                    resetForm()
                                }
                            }, object: DialogInterface.OnClickListener {
                                override fun onClick(dialog: DialogInterface?, which: Int) {
                                    parentFragmentManager.popBackStack()
                                }
                            })
                        } else {
                            Toast.makeText(requireContext(), "Failed to save recipe.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }, null)
    }


    private fun clickListener() {
        binding.btnClose.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnProductTypeNext.setOnClickListener { saveRecipe() }
        binding.etDuration.setOnClickListener {
            val currentHour = 0
            val currentMinute = 0
            val timePicker = TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                val durationText = if (hourOfDay > 0) "${hourOfDay}hours ${minute}mins" else "${minute} mins"
                binding.etDuration.setText(durationText)
            }, currentHour, currentMinute, true)

            timePicker.setTitle("Select Duration")
            timePicker.show()
        }

        binding.ivRecipeImage.setOnClickListener { checkCameraPermission() }
        binding.etRecipeTitle.setOnFocusChangeListener { _, _ -> binding.tilRecipeTitle.error = null }
        binding.etDuration.setOnFocusChangeListener { _, _ -> binding.tilDuration.error = null }
    }

    private fun scrollToBottom(targetView: View) {
        binding.scrollView.post { binding.scrollView.smoothScrollTo(0, targetView.bottom) }
    }

    fun checkCameraPermission() {
        if (PermissionHelper().isCameraPermissionGranted(requireActivity())) {
            Log.e(TAG, "checkCameraPermission: granted")
            showImagePickerOptions()
        } else {
            Log.e(TAG, "checkCameraPermission: not granted")
            permissionLauncher?.launch(Manifest.permission.CAMERA)
        }
    }

    fun showImagePickerOptions() {
        DialogHelper().showActionListDialog(requireActivity(), getString(R.string.setting_profile_image), getString(R.string.select_an_action), R.array.profile_photo_option, "", object: ActionListDialog.ActionListener{
            override fun onSelected(actionSelected: Int) {
                if (actionSelected == 0) {
                    launchCameraIntent()
                } else if (actionSelected == 1) {
                    launchGalleryIntent()
                }
            }
        })
    }

    private fun launchCameraIntent() {
        val intent = Intent(requireActivity(), ImagePickerActivity::class.java)
        intent.putExtra(ImageHelper.INTENT_IMAGE_PICKER_OPTION, ImageHelper.REQUEST_IMAGE_CAPTURE)

        // setting aspect ratio
        intent.putExtra(ImageHelper.INTENT_LOCK_ASPECT_RATIO, false)
        intent.putExtra(ImageHelper.INTENT_ASPECT_RATIO_X, 1) // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImageHelper.INTENT_ASPECT_RATIO_Y, 1)

        // setting maximum bitmap width and height
        intent.putExtra(ImageHelper.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true)
        intent.putExtra(ImageHelper.INTENT_BITMAP_MAX_WIDTH, 1000)
        intent.putExtra(ImageHelper.INTENT_BITMAP_MAX_HEIGHT, 1000)
        imageLauncher?.launch(intent)

    }

    private fun launchGalleryIntent() {
        val intent = Intent(requireActivity(), ImagePickerActivity::class.java)
        intent.putExtra(ImageHelper.INTENT_IMAGE_PICKER_OPTION, ImageHelper.REQUEST_GALLERY_IMAGE)

        intent.putExtra(ImageHelper.INTENT_LOCK_ASPECT_RATIO, false)
        intent.putExtra(ImageHelper.INTENT_ASPECT_RATIO_X, 1) // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImageHelper.INTENT_ASPECT_RATIO_Y, 1)
        imageLauncher?.launch(intent)
    }

    private fun refreshImage(imageUri: Uri?) {
        if (imageUri != null) {
            var inputStream: InputStream? = null
            try {
                inputStream = requireActivity().contentResolver.openInputStream(imageUri)
                val options = BitmapFactory.Options()
                options.inSampleSize = ImageHelper.calculateInSampleSize(options, binding.ivRecipeImage.width, binding.ivRecipeImage.height)
                val profileImage = BitmapFactory.decodeStream(inputStream, null, options)

                binding.ivRecipeImage.setImageBitmap(profileImage)
                profileBitmap = profileImage

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveImageLocally(bitmap: Bitmap): String? {
        return try {
            val directory = requireContext().filesDir
            val imageFile = File(directory, "recipe_${System.currentTimeMillis()}.jpg")

            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()

            imageFile.absolutePath

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun resetForm() {
        binding.etRecipeTitle.text?.clear()
        binding.etDuration.text?.clear()
        binding.etDescription.text?.clear()
        binding.ivRecipeImage.setImageResource(R.drawable.ic_empty_image)
        profileBitmap = null
        binding.ingredientsContainer.removeAllViews()
        addIngredientRow(binding.ingredientsContainer)
        addIngredientRow(binding.ingredientsContainer)

        stepAdapter.clearAllSteps()
        stepAdapter.addStep()
        stepAdapter.addStep()
        existingRecipe = null
        existingRecipeDesc = null
        isEditMode = false

        val chipGroup = binding.chipCategoryGroup
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as? Chip
            chip?.isChecked = i == 0
        }

        binding.scrollView.scrollTo(0, 0)

        binding.tilRecipeTitle.error = null
        binding.tilDuration.error = null
        binding.tilIngredientsError.error = null
        binding.tilIngredientsError.visibility = View.GONE
        binding.tilStepsError.error = null
        binding.tilStepsError.visibility = View.GONE
    }


}