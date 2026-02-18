package my.com.syazli.mychef

import android.app.Application
import android.content.Context
import my.com.syazli.mychef.database.db.AppDatabase

class MyChefApplication : Application() {

    companion object {
        @JvmStatic
        @Volatile
        lateinit var instance: MyChefApplication
            private set

        @JvmStatic
        @Synchronized
        fun getAppInstance(): MyChefApplication {
            return instance
        }

        const val PREFERENCE_MIGRATION_DONE = "MIGRATION_DONE"
        const val KEY_IMAGE_MIGRATION_DONE = "KEY_IMAGE_MIGRATION_DONE"

    }

    override fun onCreate() {
        super.onCreate()
        instance = this

    }

    fun setPreference(key: String, value: Boolean) {
        getSharedPreferences(PREFERENCE_MIGRATION_DONE, Context.MODE_PRIVATE).edit()
            .putBoolean(key, value)
            .apply()
    }

    fun getPreference(key: String, defaultValue: Boolean): Boolean {
        return getSharedPreferences(PREFERENCE_MIGRATION_DONE, Context.MODE_PRIVATE)
            .getBoolean(key, defaultValue)
    }

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }
}