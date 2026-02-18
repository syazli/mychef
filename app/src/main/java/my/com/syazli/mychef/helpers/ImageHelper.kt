package my.com.syazli.mychef.helpers

import android.graphics.BitmapFactory


object ImageHelper {
    private val TAG = javaClass.simpleName
    private var targetList = HashMap<String, Target>()

    const val INTENT_IMAGE_PICKER_OPTION = "image_picker_option"
    const val INTENT_ASPECT_RATIO_X = "aspect_ratio_x"
    const val INTENT_ASPECT_RATIO_Y = "aspect_ratio_Y"
    const val INTENT_LOCK_ASPECT_RATIO = "lock_aspect_ratio"
    const val INTENT_IMAGE_COMPRESSION_QUALITY = "compression_quality"
    const val INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT = "set_bitmap_max_width_height"
    const val INTENT_BITMAP_MAX_WIDTH = "max_width"
    const val INTENT_BITMAP_MAX_HEIGHT = "max_height"
    const val REQUEST_IMAGE_CAPTURE = 10
    const val REQUEST_GALLERY_IMAGE = 11


    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
            val halfHeight = options.outHeight / 2
            val halfWidth = options.outWidth / 2
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    interface ImageProcessListener {
        fun onSuccess(newImageName: String, newImageUrl: String)
        fun onError(message: String, isRetry: Boolean)
    }

}