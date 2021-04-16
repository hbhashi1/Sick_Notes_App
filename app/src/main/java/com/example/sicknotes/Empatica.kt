package com.example.sicknotes

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.empatica.empalink.ConnectionNotAllowedException
import com.empatica.empalink.EmpaDeviceManager
import com.empatica.empalink.EmpaticaDevice
import com.empatica.empalink.config.EmpaSensorStatus
import com.empatica.empalink.config.EmpaSensorType
import com.empatica.empalink.config.EmpaStatus
import com.empatica.empalink.delegate.EmpaDataDelegate
import com.empatica.empalink.delegate.EmpaStatusDelegate
import com.squareup.okhttp.internal.framed.Settings
import java.util.jar.Manifest


open class Empatica:AppCompatActivity(), EmpaDataDelegate, EmpaStatusDelegate {
    private var deviceManager: EmpaDeviceManager? = null
    private var accel_xLabel:TextView = TODO()
    private var accel_yLabel:TextView
    private var accel_zLabel:TextView
    private var bvpLabel:TextView
    private var edaLabel:TextView
    private var ibiLabel:TextView
    private var temperatureLabel:TextView
    private var batteryLabel:TextView
    private var statusLabel:TextView
    private var deviceNameLabel:TextView
    private var dataCnt:LinearLayout
    protected fun onCreate(savedInstanceState:Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialize vars that reference UI components
        statusLabel = findViewById<TextView>(R.id.status)
        dataCnt = findViewById<LinearLayout>(R.id.dataArea)
        accel_xLabel = findViewById<TextView>(R.id.accel_x)
        accel_yLabel = findViewById<TextView>(R.id.accel_y)
        accel_zLabel = findViewById<TextView>(R.id.accel_z)
        bvpLabel = findViewById<TextView>(R.id.bvp)
        edaLabel = findViewById<TextView>(R.id.eda)
        ibiLabel = findViewById<TextView>(R.id.ibi)
        temperatureLabel = findViewById<TextView>(R.id.temperature)
        batteryLabel = findViewById<TextView>(R.id.battery)
        deviceNameLabel = findViewById<TextView>(R.id.deviceName)
        val disconnectButton = findViewById<Button>(R.id.disconnectButton)
        disconnectButton.setOnClickListener {
            deviceManager?.disconnect()
        }
        initEmpaticaDeviceManager()
    }
    override fun onRequestPermissionsResult(requestCode:Int, @NonNull permissions:Array<String>, @NonNull grantResults:IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_ACCESS_COARSE_LOCATION ->
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // Permission was granted, yay!
                    initEmpaticaDeviceManager()
                }
                else
                {
                    // Permission denied, boo!
                    val needRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    AlertDialog.Builder(this)
                        .setTitle("Permission required")
                        .setMessage("Without this permission bluetooth low energy devices cannot be found, allow it in order to connect to the device.")
                        .setPositiveButton("Retry"
                        ) { _, _ ->
                            // try again
                            if (needRationale) {
                                // the "never ask again" flash is not set, try again with permission request
                                initEmpaticaDeviceManager()
                            } else {
                                // the "never ask again" flag is set so the permission requests is disabled, try open app settings to enable the permission
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts("package", packageName, null)
                                intent.setData(uri)
                                startActivity(intent)
                            }
                        }
                        .setNegativeButton("Exit application") { _, _ -> // without permission exit is the only way
                            finish()
                        }
                            .show()
                }
        }
    }
    private fun initEmpaticaDeviceManager() {
        // Android 6 (API level 23) now require ACCESS_COARSE_LOCATION permission to use BLE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_ACCESS_COARSE_LOCATION)
        }
        else
        {
            if (TextUtils.isEmpty(EMPATICA_API_KEY))
            {
                AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage("Please insert your API KEY")
                    .setNegativeButton("Close") { _, _ -> // without permission exit is the only way
                        finish()
                    }
                        .show()
                return
            }
            // Create a new EmpaDeviceManager. MainActivity is both its data and status delegate.
            deviceManager = EmpaDeviceManager(getApplicationContext(), this, this)
            // Initialize the Device Manager using your API key. You need to have Internet access at this point.
            deviceManager!!.authenticateWithAPIKey(EMPATICA_API_KEY)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        deviceManager?.cleanUp()
    }
    protected override fun onStop() {
        super.onStop()
        deviceManager?.stopScanning()
    }
    override fun didDiscoverDevice(bluetoothDevice: EmpaticaDevice, deviceName:String, rssi:Int, allowed:Boolean) {
        // Check if the discovered device can be used with your API key. If allowed is always false,
        // the device is not linked with your API key. Please check your developer area at
        // https://www.empatica.com/connect/developer.php
        Log.i(TAG, "didDiscoverDevice" + deviceName + "allowed: " + allowed)
        if (allowed)
        {
            // Stop scanning. The first allowed device will do.
            deviceManager?.stopScanning()
            try
            {
                // Connect to the device
                deviceManager?.connectDevice(bluetoothDevice)
                updateLabel(deviceNameLabel, "To: $deviceName")
            }
            catch (e: ConnectionNotAllowedException) {
                // This should happen only if you try to connect when allowed == false.
                Toast.makeText(this@Empatica, "Sorry, you can't connect to this device", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "didDiscoverDevice" + deviceName + "allowed: " + allowed + " - ConnectionNotAllowedException", e)
            }
        }
    }
    override fun didFailedScanning(errorCode:Int) {
        /*
     A system error occurred while scanning.
     @see https://developer.android.com/reference/android/bluetooth/le/ScanCallback
     */
        when (errorCode) {
            ScanCallback.SCAN_FAILED_ALREADY_STARTED -> Log.e(TAG, "Scan failed: a BLE scan with the same settings is already started by the app")
            ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> Log.e(TAG, "Scan failed: app cannot be registered")
            ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED -> Log.e(TAG, "Scan failed: power optimized scan feature is not supported")
            ScanCallback.SCAN_FAILED_INTERNAL_ERROR -> Log.e(TAG, "Scan failed: internal error")
            else -> Log.e(TAG, "Scan failed with unknown error (errorCode=$errorCode)")
        }
    }
    override fun didRequestEnableBluetooth() {
        // Request the user to enable Bluetooth
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }
    override fun bluetoothStateChanged() {
        // E4link detected a bluetooth adapter change
        // Check bluetooth adapter and update your UI accordingly.
        val isBluetoothOn = BluetoothAdapter.getDefaultAdapter().isEnabled()
        Log.i(TAG, "Bluetooth State Changed: $isBluetoothOn")
    }
    protected fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent) {
        // The user chose not to enable Bluetooth
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED)
        {
            // You should deal with this
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    override fun didUpdateSensorStatus(@EmpaSensorStatus status:Int, type: EmpaSensorType) {
        didUpdateOnWristStatus(status)
    }
    override fun didUpdateStatus(status:EmpaStatus) {
        // Update the UI
        updateLabel(statusLabel, status.name)
        // The device manager is ready for use
        if (status === EmpaStatus.READY)
        {
            updateLabel(statusLabel, status.name + " - Turn on your device")
            // Start scanning
            deviceManager?.startScanning()
            // The device manager has established a connection
            hide()
        }
        else if (status === EmpaStatus.CONNECTED)
        {
            show()
            // The device manager disconnected from a device
        }
        else if (status === EmpaStatus.DISCONNECTED)
        {
            updateLabel(deviceNameLabel, "")
            hide()
        }
    }
    override fun didReceiveAcceleration(x:Int, y:Int, z:Int, timestamp:Double) {
        updateLabel(accel_xLabel, "" + x)
        updateLabel(accel_yLabel, "" + y)
        updateLabel(accel_zLabel, "" + z)
    }
    override fun didReceiveBVP(bvp:Float, timestamp:Double) {
        updateLabel(bvpLabel, "" + bvp)
    }
    override fun didReceiveBatteryLevel(battery:Float, timestamp:Double) {
        updateLabel(batteryLabel, String.format("%.0f %%", battery * 100))
    }
    override fun didReceiveGSR(gsr:Float, timestamp:Double) {
        updateLabel(edaLabel, "" + gsr)
    }
    override fun didReceiveIBI(ibi:Float, timestamp:Double) {
        updateLabel(ibiLabel, "" + ibi)
    }
    override fun didReceiveTemperature(temp:Float, timestamp:Double) {
        updateLabel(temperatureLabel, "" + temp)
    }
    // Update a label with some text, making sure this is run in the UI thread
    private fun updateLabel(label:TextView, text:String) {
        runOnUiThread { label.setText(text) }
    }
    override fun didReceiveTag(timestamp:Double) {
    }
    override fun didEstablishConnection() {
        show()
    }
    override fun didUpdateOnWristStatus(@EmpaSensorStatus status:Int) {
        runOnUiThread {
            if (status == EmpaSensorStatus.ON_WRIST) {
                (findViewById<TextView>(R.id.wrist_status_label)).text = "ON WRIST"
            } else {
                (findViewById<TextView>(R.id.wrist_status_label)).text = "NOT ON WRIST"
            }
        }
    }
    private fun show() {
        runOnUiThread { dataCnt.visibility = View.VISIBLE }
    }
    private fun hide() {
        runOnUiThread { dataCnt.visibility = View.INVISIBLE }
    }
    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_ENABLE_BT = 1
        private const val REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1
        private const val EMPATICA_API_KEY = "038e5d8f4c0d43d4b984b638de4deb0c" // TODO insert your API Key here
    }
}