package my.com.syazli.mychef.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import my.com.syazli.mychef.R
import my.com.syazli.mychef.databinding.ActivityMainBinding
import my.com.syazli.mychef.fragment.RecipePageViewFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var pageViewFragment: RecipePageViewFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showFragment()
    }

    private fun showFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        pageViewFragment = RecipePageViewFragment()
        transaction.replace(R.id.fl_main_activity, pageViewFragment!!, "RecipePageViewFragment")
        transaction.commitNow()
    }


}
