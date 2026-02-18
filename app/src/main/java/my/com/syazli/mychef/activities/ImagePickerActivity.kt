package my.com.syazli.mychef.activities

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.color.MaterialColors
import com.yalantis.ucrop.UCrop
import my.com.syazli.mychef.R
import my.com.syazli.mychef.helpers.GeneralHelper
import my.com.syazli.mychef.helpers.ImageHelper
import java.io.File

class ImagePickerActivity : AppCompatActivity() {
    private val TAG = javaClass.simpleName

    private var IMAGE_COMPRESSION = 80
    private var ASPECT_RATIO_X = 4
    private var ASPECT_RATIO_Y = 3
    private var bitmapMaxWidth = 430
    private var bitmapMaxHeight = 430
    private var bitmapMaxSize = 1000
    private var lockAspectRatio = false
    private var setBitmapMaxWidthHeight = false

    private var fileName: String? = null
    private var outputFileUri: Uri? = null
    private var cameraLauncher: ActivityResultLauncher<Intent>? = null
    private var galleryLauncher: ActivityResultLauncher<Intent>? = null
    private var uCropLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_picker)

        val bundle = intent.extras
        if (bundle != null) {
            ASPECT_RATIO_X = bundle.getInt(ImageHelper.INTENT_ASPECT_RATIO_X, ASPECT_RATIO_X)
            ASPECT_RATIO_Y = bundle.getInt(ImageHelper.INTENT_ASPECT_RATIO_Y, ASPECT_RATIO_Y)
            IMAGE_COMPRESSION = bundle.getInt(ImageHelper.INTENT_IMAGE_COMPRESSION_QUALITY, IMAGE_COMPRESSION)
            bitmapMaxWidth = bundle.getInt(ImageHelper.INTENT_BITMAP_MAX_WIDTH, bitmapMaxWidth)
            bitmapMaxHeight = bundle.getInt(ImageHelper.INTENT_BITMAP_MAX_HEIGHT, bitmapMaxHeight)
            lockAspectRatio = bundle.getBoolean(ImageHelper.INTENT_LOCK_ASPECT_RATIO, lockAspectRatio)
            setBitmapMaxWidthHeight = bundle.getBoolean(ImageHelper.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, setBitmapMaxWidthHeight)

            initActivityResultLauncher()

            val requestCode = bundle.getInt(ImageHelper.INTENT_IMAGE_PICKER_OPTION, -1)
            if (requestCode == ImageHelper.REQUEST_IMAGE_CAPTURE) {
                takeCameraPhoto()
            } else {
                chooseImageFromGallery()
            }
        }
    }

    private fun initActivityResultLauncher() {
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.e(TAG, "initActivityResultLauncher: cameraLauncher result = $result")
            if (result.resultCode == RESULT_OK) {
                if (GeneralHelper.android11Above()) {
                    if (outputFileUri != null) {
                        cropImage(outputFileUri!!, true)
                    }
                } else {
                    val cachedImageUri = getCacheImagePath(fileName!!)
                    if (cachedImageUri != null) {
                        cropImage(cachedImageUri, true)
                    }
                }
            } else {
                setResultCancelled()
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.e(TAG, "initActivityResultLauncher: galleryLauncher result = $result")
            if (result.resultCode == RESULT_OK) {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    cropImage(imageUri, false)
                } else {
                    setResultCancelled()
                }
            } else {
                setResultCancelled()
            }
        }

        uCropLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.e(TAG, "initActivityResultLauncher: uCropLauncher result = $result")
            if (result.resultCode == RESULT_OK) {
                handleUCropResult(result.data)
            } else {
                setResultCancelled()
            }
        }
    }

    private fun takeCameraPhoto() {
        Log.e(TAG, "takeCameraPhoto: ")
        if (GeneralHelper.android11Above()) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
            outputFileUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            //Camera intent
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
            cameraLauncher?.launch(cameraIntent)
        } else {
            fileName = System.currentTimeMillis().toString() + ".jpg"
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, getCacheImagePath(fileName!!))
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                cameraLauncher?.launch(takePictureIntent)
            }
        }
    }

    private fun getCacheImagePath(fileName: String): Uri? {
        val path = File(externalCacheDir, "camera")
        if (!path.exists()) path.mkdirs()
        val image = File(path, fileName)
        return FileProvider.getUriForFile(this, packageName, image)
    }

    private fun queryName(resolver: ContentResolver, uri: Uri): String? {
        val returnCursor = resolver.query(uri, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }

    private fun chooseImageFromGallery() {
        if (GeneralHelper.android11Above()) {
            val pickPhoto = Intent(Intent.ACTION_OPEN_DOCUMENT)
            pickPhoto.addCategory(Intent.CATEGORY_OPENABLE)
            pickPhoto.setType("image/*")
            galleryLauncher?.launch(pickPhoto)
        } else {
            val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher?.launch(pickPhoto)
        }
    }

    private fun cropImage(sourceUri: Uri, isCamera: Boolean) {
        val destinationUri = Uri.fromFile(File(cacheDir, queryName(contentResolver, sourceUri)))

        val options = UCrop.Options()
        options.setCompressionFormat(Bitmap.CompressFormat.PNG)
        options.setCompressionQuality(IMAGE_COMPRESSION)

        // applying UI theme
        options.setToolbarColor(MaterialColors.getColor(this, R.attr.color_primary, Color.BLACK))
        options.setStatusBarColor(MaterialColors.getColor(this, R.attr.color_primary, Color.BLACK))
        options.setActiveControlsWidgetColor(MaterialColors.getColor(this, R.attr.color_primary, Color.BLACK))

        if (lockAspectRatio) {
            options.withAspectRatio(ASPECT_RATIO_X.toFloat(), ASPECT_RATIO_Y.toFloat())
        } else {
            options.setFreeStyleCropEnabled(true)
        }
        if (setBitmapMaxWidthHeight) {
            options.withMaxResultSize(bitmapMaxWidth, bitmapMaxHeight)
        }
        options.setMaxBitmapSize(bitmapMaxSize)

        val intent = UCrop.of(sourceUri, destinationUri)
            .withMaxResultSize(480, 480)
            .withOptions(options)
            .getIntent(this)
        uCropLauncher?.launch(intent)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("outputFileUri", outputFileUri)
        outState.putString("fileName", fileName)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        outputFileUri = savedInstanceState.getParcelable("outputFileUri")
        fileName = savedInstanceState.getString("fileName")
    }

    private fun handleUCropResult(data: Intent?) {
        if (data == null) {
            setResultCancelled()
            return
        }
//        saveImage(UCrop.getOutput(data))
        val resultUri = UCrop.getOutput(data)
        setResultOk(resultUri!!)
    }

    private fun setResultOk(imagePath: Uri) {
        val intent = Intent()
        intent.putExtra("path", imagePath.toString())
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun setResultCancelled() {
        val intent = Intent()
        setResult(RESULT_CANCELED, intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

}