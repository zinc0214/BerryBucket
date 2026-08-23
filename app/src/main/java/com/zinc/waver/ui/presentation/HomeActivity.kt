package com.zinc.waver.ui.presentation

import android.Manifest
import android.content.Intent
import android.content.Intent.createChooser
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.zinc.domain.models.BillingCycle
import com.zinc.waver.R
import com.zinc.waver.model.AddImageType
import com.zinc.waver.model.LoadedImageInfo
import com.zinc.waver.ui.presentation.login.JoinScreen
import com.zinc.waver.ui.presentation.login.LoginScreen
import com.zinc.waver.ui.presentation.login.WelcomePopupScreen
import com.zinc.waver.ui.presentation.model.ActionWithActivity
import com.zinc.waver.ui.presentation.model.WaverPlusType
import com.zinc.waver.ui.presentation.screen.billing.ChooseSubscription
import com.zinc.waver.ui.util.CheckPermissionView
import com.zinc.waver.ui_detail.model.ShowParentScreenType
import com.zinc.waver.ui_more.screen.MyWaveManageScreen
import com.zinc.waver.util.CropImageCustomContract
import com.zinc.waver.util.FileUtil.getFileFromUri
import com.zinc.waver.util.createImageFile
import com.zinc.waver.util.dp2px
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException

@AndroidEntryPoint
class HomeActivity : AppCompatActivity(),
    com.zinc.waver.ui_more.components.RequestPermissionCallbackProvider {

    private val viewModel by viewModels<HomeViewModel>()

    private var photoUri: Uri? = null
    private var imageCount = 0
    private lateinit var takePhotoAction: ActionWithActivity.AddImage

    private var isNeedToShowPermission = false
    private lateinit var checkPermissionAction: ActionWithActivity.CheckPermission

    // 권한 요청 콜백 저장
    private val permissionCallbacks =
        mutableMapOf<Int, com.zinc.waver.ui_more.components.RequestPermissionCallback>()

    private val cropImage = registerForActivityResult(CropImageCustomContract()) { result ->
        if (result.isSuccessful) {
            // Use the cropped image URI.
            Log.e("ayhan", "croppedImageUri : ${result.cropRect}")
            photoUri = result.uriContent
            getFile(result)
        } else {
            Log.e("ayhan", "Crop image failed: ${result.error}")
        }
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                photoUri?.let {
                    cropImage(it)
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
        MobileAds.initialize(this)
        checkInAppBilling()
        viewModel.loadProfileInfo()
        viewModel.checkUserLimit()

        requestNotificationPermission()

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM_TOKEN", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Log.d("FCM_TOKEN", "FCM Token: $token")
        })

        setContent {
            enableEdgeToEdge()

            val retryEmail: MutableState<String> = remember {
                mutableStateOf("")
            }
            val showScreenType: MutableState<ShowParentScreenType> =
                remember {
                    mutableStateOf(ShowParentScreenType.Login)
                }

            // 웰컴 팝업은 가입 직후에만 노출한다.
            // 참으로 만드는 곳은 아래 JoinScreen 의 goToMain 하나뿐이다. 로그인으로 들어온 홈,
            // 배지 화면에서 돌아온 홈은 이 값을 건드리지 않으므로 false 로 남아 팝업이 뜨지 않는다.
            val showWelcomePopup = remember { mutableStateOf(false) }

            // 알림 클릭 시 알림 화면으로 이동
            val notificationClickAction = intent.getStringExtra("notification_click_action")
            if (notificationClickAction == "OPEN_ALARM_SCREEN") {
                showScreenType.value = ShowParentScreenType.Alarm
            }

            when (showScreenType.value) {
                ShowParentScreenType.Join -> {
                    JoinScreen(goToMain = {
                        // 가입 완료. 홈으로 넘긴 뒤 홈 위에서 웰컴 팝업을 띄운다.
                        showWelcomePopup.value = true
                        showScreenType.value =
                            ShowParentScreenType.Main
                    }, goToBack = {
                        finish()
                    }, goToLogin = {
                        retryEmail.value = it.email
                        showScreenType.value =
                            ShowParentScreenType.Login
                    },
                        addImageAction = {
                        takePhotoAction = it
                        if (it.type == AddImageType.CAMERA) {
                            takePhoto()
                        } else {
                            goToGallery()
                        }
                    })
                }

                ShowParentScreenType.Login -> {
                    LoginScreen(
                        retryLoginEmail = retryEmail.value,
                        goToMainHome = {
                            showScreenType.value =
                                ShowParentScreenType.Main
                        }, goToJoin = {
                            showScreenType.value =
                                ShowParentScreenType.Join
                        }, goToFinish = {
                            finish()
                        })
                }

                ShowParentScreenType.Main -> {
                    HomeScreen(action = { actionWithActivity ->
                        when (actionWithActivity) {
                            is ActionWithActivity.AddImage -> {
                                takePhotoAction = actionWithActivity
                                if (actionWithActivity.type == AddImageType.CAMERA) {
                                    takePhoto()
                                } else {
                                    goToGallery()
                                }
                            }

                            is ActionWithActivity.CheckPermission -> {
                                checkPermissionAction = actionWithActivity
                                isNeedToShowPermission = true

                            }

                            ActionWithActivity.AppFinish -> {
                                finish()
                            }

                            ActionWithActivity.GoToQNAEmail -> {
                                goToContactToMyBuryByEmail()
                            }

                            ActionWithActivity.Logout -> {
                                viewModel.logout()
                                finish()
                            }

                            is ActionWithActivity.InAppBilling -> {
                                setUpBilling(actionWithActivity.type, true)
                            }
                        }
                    })

                    if (isNeedToShowPermission) {
                        CheckPermissionView(isAvailable = {
                            checkPermissionAction.isAllGranted.invoke(it)
                            isNeedToShowPermission = false
                        })
                    }

                    if (showWelcomePopup.value) {
                        WelcomePopupScreen(
                            goToBadgeInfo = {
                                // 배지 화면에서 뒤로가면 다시 Main 으로 돌아오므로,
                                // 여기서 내려두지 않으면 팝업이 재노출된다.
                                showWelcomePopup.value = false
                                showScreenType.value = ShowParentScreenType.BadgeInfo
                            },
                            gotoStart = {
                                showWelcomePopup.value = false
                            }
                        )
                    }
                }

                ShowParentScreenType.Alarm -> {
                    HomeScreen(
                        startDestination = com.zinc.waver.util.nav.AlarmDestinations.GO_TO_ALARM,
                        action = { actionWithActivity ->
                            when (actionWithActivity) {
                                is ActionWithActivity.AddImage -> {
                                    takePhotoAction = actionWithActivity
                                    if (actionWithActivity.type == AddImageType.CAMERA) {
                                    takePhoto()
                                } else {
                                    goToGallery()
                                }
                            }

                            is ActionWithActivity.CheckPermission -> {
                                checkPermissionAction = actionWithActivity
                                isNeedToShowPermission = true

                            }

                            ActionWithActivity.AppFinish -> {
                                finish()
                            }

                            ActionWithActivity.GoToQNAEmail -> {
                                goToContactToMyBuryByEmail()
                            }

                            ActionWithActivity.Logout -> {
                                viewModel.logout()
                                finish()
                            }

                            is ActionWithActivity.InAppBilling -> {
                                setUpBilling(actionWithActivity.type, true)
                            }
                        }
                    })

                    if (isNeedToShowPermission) {
                        CheckPermissionView(isAvailable = {
                            checkPermissionAction.isAllGranted.invoke(it)
                            isNeedToShowPermission = false
                        })
                    }
                }

                ShowParentScreenType.BadgeInfo -> {
                    MyWaveManageScreen(
                        onBackPressed = {
                            showScreenType.value = ShowParentScreenType.Main
                        }
                    )
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
            Toast.makeText(
                this,
                resources.getString(R.string.failToGetImage),
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
        }

        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(
                this,
                "com.zinc.waver.fileprovider",  // authorities를 수정된 값으로 변경
                photoFile
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
        Log.e("ayhan", "cropImage : $photoUri")
        @Suppress("DEPRECATION")
        cropImage.launch(
            com.canhub.cropper.CropImageContractOptions(
                uri = photoUri,
                cropImageOptions = CropImageOptions(
                    maxZoom = 3,
                    showCropLabel = true,
                    showCropOverlay = true,
                    guidelines = CropImageView.Guidelines.ON,
                    outputCompressFormat = Bitmap.CompressFormat.JPEG, // JPEG 사용 (더 작은 파일 크기)
                    outputCompressQuality = 30, // 압축 품질 설정 (낮을수록 작음)
                    outputRequestWidth = dp2px(100), // 100dp로 설정
                    outputRequestHeight = dp2px(100), // 100dp로 설정
                    cropMenuCropButtonTitle = getString(R.string.imageCropSave),
                    activityTitle = getString(R.string.imageCropperTitle),
                    aspectRatioX = 1,
                    aspectRatioY = 1,
                    fixAspectRatio = true,
                    initialCropWindowPaddingRatio = 0f
                )
            )
        )
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }

    private fun getFile(result: CropImageView.CropResult) {
        if (photoUri == null) {
            Toast.makeText(
                this,
                resources.getString(R.string.failToGetImage),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            handleImageResult(result)
        }
    }

    private fun handleImageResult(result: CropImageView.CropResult) {
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                try {
                    val file = getFileFromUri(this, uri)
                    if (file != null) {
                        val imageInfo = LoadedImageInfo(
                            key = imageCount++,
                            uri = uri,
                            file = file,
                            path = file.absolutePath  // absolutePath 사용
                        )
                        takePhotoAction.succeed(imageInfo)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "이미지 처리 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun goToContactToMyBuryByEmail() {
        val send = Intent(Intent.ACTION_SENDTO)
        val uriText = "mailto:" + Uri.encode("mybury.info@gmail.com") +
                "?subject=" + Uri.encode("<" + resources.getString(R.string.goToWaveQna) + ">")
        val uri = uriText.toUri()

        send.data = uri
        startActivity(
            createChooser(send, resources.getString(R.string.goToWaveQna)),
            null
        )
    }

    private fun checkInAppBilling() {
        WaverPlusType.entries.forEach { type ->
            setUpBilling(type, false)
        }
    }

    private fun setUpBilling(type: WaverPlusType, isForPurchase: Boolean) {
        val subs = ChooseSubscription(
            this,
            isForPurchase = isForPurchase,
            subsDone = { subscribeId ->
                // 서버에 구독 시작을 알린다
                viewModel.notifySubscriptionStarted(
                    billingCycle = when (type) {
                        WaverPlusType.YEAR -> BillingCycle.YEARLY
                        WaverPlusType.MONTH -> BillingCycle.MONTHLY
                    },
                    subscribeId = subscribeId
                )
                Toast.makeText(this, "구매완료", Toast.LENGTH_SHORT).show()
            },
            alreadyPurchased = { purchased, subscribeId ->
                if (purchased && subscribeId != null) {
                    // 활성 구독 감지 → 서버에 알림 (세션당 1회, best-effort 주기)
                    viewModel.notifySubscriptionStarted(
                        billingCycle = when (type) {
                            WaverPlusType.YEAR -> BillingCycle.YEARLY
                            WaverPlusType.MONTH -> BillingCycle.MONTHLY
                        },
                        subscribeId = subscribeId
                    )
                }
            })

        subs.billingSetup(waverPlusType = type)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // 권한 요청 결과 처리
        val granted =
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        notifyPermissionResult(requestCode, granted)
    }

    override fun registerPermissionCallback(
        requestCode: Int,
        callback: com.zinc.waver.ui_more.components.RequestPermissionCallback
    ) {
        permissionCallbacks[requestCode] = callback
    }

    override fun unregisterPermissionCallback(requestCode: Int) {
        permissionCallbacks.remove(requestCode)
    }

    override fun notifyPermissionResult(requestCode: Int, granted: Boolean) {
        permissionCallbacks[requestCode]?.onPermissionResult(granted)
    }
}
