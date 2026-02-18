package my.com.syazli.mychef.helpers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class PermissionHelper {

    fun isCameraPermissionGranted(context: Context?): Boolean {
        try {
            if (context != null) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//                    Log.e(TAG, "isCameraPermissionGranted: true")
                    return true
                } else {
//                    Log.e(TAG, "isCameraPermissionGranted: false")
                }
            }
        } catch (e: Exception) {
            //
        }
        return false
    }
}