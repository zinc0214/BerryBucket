package com.zinc.berrybucket.ui.presentation

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.zinc.berrybucket.model.WriteImageInfo
import com.zinc.berrybucket.util.createImageFile
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private val addImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val file = result.data?.getStringExtra("file")
                val url = result.data?.getStringExtra("url")

            }
        }

    private var imageType: ActionWithActivity.AddImageType? = null
    private var photoUri: Uri? = null
    private lateinit var takePhotoAction: ActionWithActivity.AddImage

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                photoUri?.let {
                    cropImage(it)
                    MediaScannerConnection.scanFile(
                        this, arrayOf(it.path), null
                    ) { _, _ -> }
                }
            }
        }

    private val imageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.let {
                cropImage(it)
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BerryBucketApp(action = {
                when (it) {
                    is ActionWithActivity.AddImage -> {
                        // AddImageActivity.startWithLauncher(this, it.type)
                        takePhotoAction = it
                        if (it.type == ActionWithActivity.AddImageType.CAMERA) {
                            takePhoto()
                        } else {
                            goToGallery()
                        }
                    }
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    result.uri?.let {
                        photoUri = result.uri
                        getFile()
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    val error = result.error
                    Toast.makeText(this, "???????????? ??????????????? ??????????????????1.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        var photoFile: File? = null

        try {
            photoFile = createImageFile(this)
        } catch (e: IOException) {
            Toast.makeText(this, "????????? ?????? ??????! ?????? ??????????????????.", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }

        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(
                this, "BerryBucketApplication.provider", photoFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            cameraLauncher.launch(intent)
        }
    }

    private fun goToGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra("crop", true)
        intent.action = Intent.ACTION_GET_CONTENT
        imageLauncher.launch(intent)
    }

    private fun cropImage(photoUri: Uri) {
        CropImage.activity(photoUri).setGuidelines(CropImageView.Guidelines.ON)
            .setAllowFlipping(false).setAspectRatio(1, 1)
            .setScaleType(CropImageView.ScaleType.CENTER_CROP)
            .setCropShape(CropImageView.CropShape.RECTANGLE).start(this)
    }

    private fun getFile() {
        val src = BitmapFactory.decodeFile(photoUri?.path)
        val resized = Bitmap.createScaledBitmap(src, 700, 700, true)
        val imageFile = saveBitmapAsFile(resized, photoUri?.path!!)
        if (photoUri != null) {
            takePhotoAction.succeed(WriteImageInfo(uri = photoUri!!, file = imageFile))
        } else {
            Toast.makeText(this, "???????????? ??????????????? ??????????????????.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmapAsFile(bitmap: Bitmap?, filePath: String): File {
        val file = File(filePath)
        val os: OutputStream
        try {
            file.createNewFile()
            os = FileOutputStream(file)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 80, os)
            os.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file
    }

}

sealed class ActionWithActivity {
    data class AddImage(
        val type: AddImageType, val failed: () -> Unit, val succeed: (WriteImageInfo) -> Unit
    ) : ActionWithActivity()

    enum class AddImageType {
        CAMERA, GALLERY
    }
}