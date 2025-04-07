package com.droid.bluetoothtimer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.droid.bluetoothtimer.ui.theme.BluetoothTimerTheme


//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    BluetoothTimerTheme {
//        Greeting(
//            statusText= "scan start",
//            isScanning = false,
//            scannedDevices = emptyList(),
//            onScanToggled = {},
//        )
//    }
//}