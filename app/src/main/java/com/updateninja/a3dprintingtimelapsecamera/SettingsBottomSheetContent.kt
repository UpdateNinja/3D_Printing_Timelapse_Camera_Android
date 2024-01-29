package com.updateninja.a3dprintingtimelapsecamera

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider



@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsBottomSheetContent(
    modifier: Modifier = Modifier,
    viewModel: MyViewModel,
    lifecycleOwner: LifecycleOwner
){


    var ipAddress by rememberSaveable { mutableStateOf("") }
    var apiKey by rememberSaveable { mutableStateOf("") }

    var text by remember { mutableStateOf("") }

    viewModel.data.observe(lifecycleOwner) { detailedData ->

        when (detailedData.connectionStatus) {
            FetchDataState.SUCCESS -> {
                Log.d("BottomSheet", "Success from BottomSheet")
                text = "Connect Success"
            }

            FetchDataState.TAKE_PHOTO -> {
                Log.d("BottomSheet", "Take Photo from BottomSheet")
            }

            FetchDataState.FAILED -> {
                Log.d("BottomSheet", "Failed from BottomSheet")
                text = "Failed"
            }

            FetchDataState.CONNECTING -> {
                Log.d("BottomSheet", "Connecting from BottomSheet")
                text = "Connecting"
            }

            else -> {}
        }
    }

    // Use LaunchedEffect to trigger a side effect when text changes
    LaunchedEffect(text) {
        // Update the UI after recomposition
        // This should help ensure that the Text composable reflects the updated text value
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {

            Text(text = "Status: $text")

        SimpleOutlinedTextFieldSample(true,"IP-Address"){
            ipAddress = it
        }
        SimpleOutlinedTextFieldSample(false,"API-Key"){
            apiKey = it
        }
        Button(onClick = {
                Log.d("Scaffold","Clicked Login")

            viewModel.fetchData(lifecycleOwner,ipAddress,apiKey)
            }
        ) {
            Text(text = "Connect")
            Icon(imageVector = Icons.Default.Login,
                contentDescription = "Switch Camera"
            )
        }
    }

}

@Composable
fun SimpleOutlinedTextFieldSample(
    isIp:Boolean,
    placeholder : String,
    onValueChange: (String) -> Unit
) {
    var text by rememberSaveable { mutableStateOf("") }

    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onValueChange(it) },
        label = { Text(placeholder)
        }
        , keyboardOptions = if (isIp) KeyboardOptions(keyboardType = KeyboardType.Decimal) else
        KeyboardOptions(keyboardType = KeyboardType.Password)

    )
}