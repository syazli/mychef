package my.com.syazli.mychef.activities

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import my.com.syazli.mychef.database.dataclass.Recipe
import my.com.syazli.mychef.database.viewmodel.RecipeDescriptionViewModel
import my.com.syazli.mychef.database.viewmodel.RecipeViewModel
import my.com.syazli.mychef.databinding.ActivitySplashBinding
import java.io.File
import java.io.FileOutputStream

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val recipeViewModel: RecipeViewModel by viewModels()
    private val recipeDescriptionViewModel: RecipeDescriptionViewModel by viewModels()

    private var isAnimationDone = false
    private var isDataLoaded = false
    private var isMigrationStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lottieView.playAnimation()

        recipeViewModel.allRecipes.observe(this) { recipes ->
            if (!recipes.isNullOrEmpty() && !isMigrationStarted) {
                recipeDescriptionViewModel.allRecipes.observe(this) {
                    val needsMigration = recipes.any { !it.imageName.contains("/") }

                    if (needsMigration) {
                        migrateImagesToInternal(recipes)
                    } else {
                        isDataLoaded = true
                        navigateIfReady()
                    }
                }

            }
        }

        binding.lottieView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                isAnimationDone = true
                navigateIfReady()
            }
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    private fun migrateImagesToInternal(recipes: List<Recipe>) {
        isMigrationStarted = true

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                recipes.forEach { recipe ->
                    if (!recipe.imageName.contains("/")) {
                        val resId = resources.getIdentifier(recipe.imageName, "drawable", packageName)

                        if (resId != 0) {
                            val file = File(filesDir, "${recipe.imageName}.webp")
                            if (!file.exists()) {
                                resources.openRawResource(resId).use { input ->
                                    FileOutputStream(file).use { output ->
                                        input.copyTo(output)
                                    }
                                }
                            }

                            recipe.imageName = file.absolutePath
                            recipeViewModel.updateFood(recipe)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    isDataLoaded = true
                    navigateIfReady()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isDataLoaded = true
                    navigateIfReady()
                }
            }
        }
    }

    private fun navigateIfReady() {
        if (isAnimationDone && isDataLoaded) {
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}