package my.com.syazli.mychef.helpers

import android.os.Build

object GeneralHelper {

    fun android5AndAbove(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }

    fun android11Above(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }
}