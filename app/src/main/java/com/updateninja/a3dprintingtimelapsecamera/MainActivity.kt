package com.updateninja.a3dprintingtimelapsecamera

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.updateninja.a3dprintingtimelapsecamera.ui.theme._3DPrintingTimeLapseCameraTheme
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException


class MainActivity : ComponentActivity() {

    private val handler = Handler()
    private val delay: Long = 1000 // delay in milliseconds
    private var previousZ = 0.0
    private var layerCounter = 0
    private lateinit var viewModel: MyViewModel
    private var jobName:String = "Default"
    private var imageCount = 0



    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[MyViewModel::class.java]




        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(
                this, CAMERAX_PERMISSIONS, 0
            )
        }

            setContent {

                _3DPrintingTimeLapseCameraTheme {
                    // A surface container using the 'background' color from the theme
                    val scope = rememberCoroutineScope()
                    val scaffoldState = rememberBottomSheetScaffoldState()
                    var jobName_info by remember { mutableStateOf("") }
                    var pictures_taken by remember { mutableStateOf(0) }
                    val openAlertDialog = remember { mutableStateOf(false) }
                    val controller = remember {
                        LifecycleCameraController(applicationContext).apply {
                            setEnabledUseCases(
                                CameraController.IMAGE_CAPTURE
                            )
                        }
                    }

                    viewModel.data.observe(this) { detailedData ->
                        when (detailedData.connectionStatus) {
                            FetchDataState.SUCCESS -> {
                                Log.d("MainActivity", "Success from MainActivity")
                                // Access additional details if needed
                                jobName = ("Job " + detailedData.jobName) ?: "Default"
                                jobName_info = jobName
                                val axisZ = detailedData.axisZ?:0.0
                                val flow = detailedData.flow
                                val printerState = detailedData.printerState

                                if (!viewModel.handlerStarted) {
                                    handler.postDelayed(object : Runnable {
                                        override fun run() {
                                            viewModel.fetchData(
                                                this@MainActivity,
                                                detailedData.ipAddress,
                                                detailedData.apiKey
                                            )
                                            handler.postDelayed(this, delay)
                                        }
                                    }, delay)
                                    viewModel.handlerStarted = true
                                }


                            }

                            FetchDataState.TAKE_PHOTO -> {
                                Log.d("MainActivity", "Take Photo from MainActivity after 500 milliseconds")
                                handler.postDelayed({
                                    takePhoto(controller = controller)
                                },500)
                                pictures_taken++

                            }

                            FetchDataState.FAILED -> {
                                Log.d("MainActivity", "Failed from MainActivity")
                            }

                            FetchDataState.CONNECTING -> {
                                Log.d("MainActivity", "Connecting from MainActivity")
                            }

                            else -> {}
                        }
                    }

                    when {
                        // ...
                        openAlertDialog.value -> {
                            AlertDialogInfo(
                                onDismissRequest = { openAlertDialog.value = false },
                                onConfirmation = {
                                    openAlertDialog.value = false
                                },
                                dialogTitle = "Info",
                                icon = Icons.Default.Info
                            )
                        }
                    }
                    
                    BottomSheetScaffold(
                        scaffoldState = scaffoldState,
                        sheetPeekHeight = 0.dp,
                        sheetContent = {
                            SettingsBottomSheetContent(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                viewModel,this@MainActivity
                            )
                        }
                    ) { padding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                        ) {

                            CameraPreview(
                                controller = controller,
                                modifier = Modifier
                                    .fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .background(colorResource(id = R.color.grey_background))
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ){
                                Column {
                                    Text(text = "Job name : $jobName_info", fontSize = 16.sp, color = Color.White)
                                    Text(text = "Pictures taken : $pictures_taken", fontSize = 16.sp, color = Color.White)
                                }

                            }


                            Box(modifier = Modifier
                                .background(colorResource(id = R.color.grey_background))
                                .fillMaxWidth()
                                .padding(16.dp)
                                .align(Alignment.BottomCenter)){
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                scaffoldState.bottomSheetState.expand()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings, "Open Settings", tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            takePhoto(
                                                controller = controller
                                            )
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoCamera, "Take Photo", tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            openAlertDialog.value = !openAlertDialog.value
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info, "Info", tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }

                                }
                            }

                        }
                    }

                }
            }
        }


    private fun takePhoto(
        controller: LifecycleCameraController
    ){
        if(!hasRequiredPermissions()){
            return
        }

        controller.takePicture(
            ContextCompat.getMainExecutor(applicationContext),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)

                    val matrix = Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                    }
                    val rotatedBitmap = Bitmap.createBitmap(
                        image.toBitmap(),
                        0,
                        0,
                        image.width,
                        image.height,
                        matrix,
                        true

                    )

                    saveBitmapToFile(rotatedBitmap,jobName)
                    image.close()

                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Camera","Couldn't take photo: ",exception)
                }
            }
        )
    }

    private fun saveBitmapToFile(bitmap: Bitmap,directoryName: String) {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + directoryName)
                put(MediaStore.Images.Media.DISPLAY_NAME, "my-photo$imageCount.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }
            imageCount++

            val contentResolver = contentResolver
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                val outputStream = contentResolver.openOutputStream(it)
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
                outputStream?.close()

                Toast.makeText(
                    applicationContext,
                    "Photo saved: $uri",
                    Toast.LENGTH_LONG
                ).show()
            } ?: run {
                Toast.makeText(
                    applicationContext,
                    "Failed to save photo",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: IOException) {
            Log.e("Camera", "Error saving photo: ", e)
            Toast.makeText(
                applicationContext,
                "Error saving photo",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                applicationContext,it
            )== PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private val CAMERAX_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDialogInfo(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Example Icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Column(){
                Text(text = "This is open-source project created by UpdateNinja")
                Spacer(modifier = Modifier.padding(16.dp))
                Text(text = "1. Connect your Prusa printer using Credentials(Settings)")
                Text(text = "2. Modify your custom G-Code instructions in github readme")
                Text(text = "3. Start print normally")
                Spacer(modifier = Modifier.padding(16.dp))
                Text(text = "Bug reports : Contact@updateninja.fi")
            }

        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Close")
            }
        },
        dismissButton = {

        }
    )
}
