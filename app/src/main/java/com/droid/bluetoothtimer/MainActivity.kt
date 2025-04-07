package com.droid.bluetoothtimer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.droid.bluetoothtimer.ui.theme.BluetoothTimerTheme

class MainActivity : ComponentActivity() {
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts
            .RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                // すべてのパーミッションが許可された場合
                startBluetoothScan()
            } else {
                // パーミッションが拒否された場合の処理
                Log.d("Permissions", "Bluetooth permissions denied")
                // 必要に応じて、UI にメッセージを表示するなどの処理を追加
            }
        }
    //  onScanToggled コールバックを保持する変数
    private var scanCallback: ScanCallback? = null
    private var isScanning by mutableStateOf(false)
    private var statusText by mutableStateOf("スキャン停止中")
    private var scanButtonText by mutableStateOf("Bluetooth スキャン開始")
    private val scannedDevices = mutableStateListOf<String>()

    //  権限リクエスト関数を追加
    private val bluetoothPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    }

    private fun handleBluetoothPermissions(onPermissionsGranted: () -> Unit) {
        if (checkBluetoothPermissions()) {
            onPermissionsGranted()
        } else {
            requestBluetoothPermissions(onPermissionsGranted)
        }
    }

    private fun checkBluetoothPermissions(): Boolean {
        return bluetoothPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestBluetoothPermissions(onPermissionsGranted: () -> Unit) {
        requestMultiplePermissions.launch(bluetoothPermissions)
        // registerForActivityResult で指定したコールバック内で onPermissionsGranted を呼び出す
    }

    private fun startBluetoothScan() {
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    val deviceName = result.device?.name
                    if (!deviceName.isNullOrEmpty() && !scannedDevices.contains(deviceName)) {
                        scannedDevices.add(deviceName)
                        Log.d("Bluetooth", "Found device: $deviceName")
                    } else if (deviceName.isNullOrEmpty()) {
//                        Log.d("Bluetooth", "Found device with no name (Unknown Device).")
                    }
                } else {
                    Log.w("Bluetooth", "BLUETOOTH_CONNECT permission not granted, cannot access device name.")
                }
            }

            override fun onBatchScanResults(results: List<ScanResult>) {
                results.forEach { onScanResult(0, it) }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("Bluetooth", "Scan failed with error: $errorCode")
                onScanStopped()
            }
        }
        scannedDevices.clear()
        startBleScan(this, scanCallback!!) { onScanStopped() }
        isScanning = true
        statusText = "スキャン中…"

    }

    private fun stopBluetoothScan() {
        stopBleScan(this, scanCallback!!)
        onScanStopped()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
//            var isScanning by remember { mutableStateOf(false) }
//            var scanButtonText by remember { mutableStateOf("Bluetooth スキャン開始") }
            val onScanToggled = {
                Log.d( "isScanning", "isScanning:$isScanning")
                if (isScanning) {
                    stopBluetoothScan()
                    statusText = "スキャンを手動で停止しました。"
                } else {
                    handleBluetoothPermissions {
                        startBluetoothScan()
                    }
                }
            }
            scanButtonText = if (isScanning) "スキャンを停止する" else "Bluetooth スキャン開始" //

            BluetoothTimerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        modifier = Modifier.padding(innerPadding),
                        statusText = statusText,
                        scanButtonText = scanButtonText,
                        scannedDevices = scannedDevices,
                        onScanToggled = onScanToggled,
                        isScanning = isScanning
                    )
                }
            }
        }
    }
    private fun onScanStopped() {
        isScanning = false
        statusText = "スキャン自動終了"
        scanCallback = null
        Log.d("Bluetooth", "Scan stopped")
    }
}



// Bluetoothスキャン開始関数
@SuppressLint("MissingPermission")
fun startBleScan(context: Context, scanCallback: ScanCallback, onScanStopped: () -> Unit) {
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bluetoothManager.adapter
    val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

    if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
        Log.e("Bluetooth", "Bluetooth adapter not available or disabled")
        return
    }

    bluetoothLeScanner.startScan(scanCallback)
    //  一定時間後にスキャンを停止 (例: 10秒)
    Handler(Looper.getMainLooper()).postDelayed({
        stopBleScan(context, scanCallback)
        //  MainActivity の onScanStopped を呼び出す
        onScanStopped()
    }, 10000)
}

// Bluetoothスキャン停止関数
@SuppressLint("MissingPermission")
fun stopBleScan(context: Context, scanCallback: ScanCallback) {
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bluetoothManager.adapter
    val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    bluetoothLeScanner.stopScan(scanCallback)
}

@Composable
fun Greeting(
    modifier: Modifier = Modifier,
    statusText: String,
    scanButtonText: String,
    scannedDevices: List<String>,
    onScanToggled: () -> Unit,
    isScanning: Boolean
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // テキスト表示
        Text(
            text = statusText,
            modifier = Modifier.padding(16.dp)
        )
        Text(
            text = "isScanning: $isScanning",
            modifier = Modifier.padding(16.dp)
        )


        Button(onClick = {
            onScanToggled()
        }) {
            Text(scanButtonText)
        }

        // スキャンされたデバイスリストを表示 (簡略化)
        if (scannedDevices.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("スキャンされたデバイス:")
            scannedDevices.forEach { deviceName ->
                Text(deviceName)
            }
        }
    }
}