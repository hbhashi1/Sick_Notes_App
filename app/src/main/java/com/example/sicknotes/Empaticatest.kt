package com.empatica.sample

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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

class MainActivity : AppCompatActivity(), EmpaDataDelegate, EmpaStatusDelegate {
    private var deviceManager: EmpaDeviceManager? = null
    private var accel_xLabel: TextView? = null
    private var accel_yLabel: TextView? = null
    private var accel_zLabel: TextView? = null
    private var bvpLabel: TextView? = null
    private var edaLabel: TextView? = null
    private var ibiLabel: TextView? = null
    private var temperatureLabel: TextView? = null
    private var batteryLabel: TextView? = null
    private var statusLabel: TextView? = null
    private var deviceNameLabel: TextView? = null
    private var dataCnt: LinearLayout? = null
    @Override
    protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize vars that reference UI components
        statusLabel = findViewById(R.id.status) as TextView?
        dataCnt = findViewById(R.id.dataArea) as LinearLayout?
        accel_xLabel = findViewById(R.id.accel_x) as TextView?
        accel_yLabel = findViewById(R.id.accel_y) as TextView?
        accel_zLabel = findViewById(R.id.accel_z) as TextView?
        bvpLabel = findViewById(R.id.bvp) as TextView?
        edaLabel = findViewById(R.id.eda) as TextView?
        ibiLabel = findViewById(R.id.ibi) as TextView?
        temperatureLabel = findViewById(R.id.temperature) as TextView?
        batteryLabel = findViewById(R.id.battery) as TextView?
        deviceNameLabel = findViewById(R.id.deviceName) as TextView?
        val disconnectButton: Button = findViewById(R.id.disconnectButton)
        disconnectButton.setOnClickListener(object : OnClickListener() {
            @Override
            fun onClick(v: View?) {
                if (deviceManager != null) {
                    deviceManager.disconnect()
                }
            }
        })
        initEmpaticaDeviceManager()
    }

    @Override
    fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String?>?, @NonNull grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_ACCESS_COARSE_LOCATION ->                 // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, yay!
                    initEmpaticaDeviceManager()
                } else {
                    // Permission denied, boo!
                    val needRationale: Boolean = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    Builder(this)
                            .setTitle("Permission required")
                            .setMessage("Without this permission bluetooth low energy devices cannot be found, allow it in order to connect to the device.")
                            .setPositiveButton("Retry", object : OnClickListener() {
                                fun onClick(dialog: DialogInterface?, which: Int) {
                                    // try again
                                    if (needRationale) {
                                        // the "never ask again" flash is not set, try again with permission request
                                        initEmpaticaDeviceManager()
                                    } else {
                                        // the "never ask again" flag is set so the permission requests is disabled, try open app settings to enable the permission
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        val uri: Uri = Uri.fromParts("package", getPackageName(), null)
                                        intent.setData(uri)
                                        startActivity(intent)
                                    }
                                }
                            })
                            .setNegativeButton("Exit application", object : OnClickListener() {
                                fun onClick(dialog: DialogInterface?, which: Int) {
                                    // without permission exit is the only way
                                    finish()
                                }
                            })
                            .show()
                }
        }
    }

    private fun initEmpaticaDeviceManager() {
        // Android 6 (API level 23) now require ACCESS_COARSE_LOCATION permission to use BLE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_ACCESS_COARSE_LOCATION)
        } else {
            if (TextUtils.isEmpty(EMPATICA_API_KEY)) {
                Builder(this)
                        .setTitle("Warning")
                        .setMessage("Please insert your API KEY")
                        .setNegativeButton("Close", object : OnClickListener() {
                            fun onClick(dialog: DialogInterface?, which: Int) {
                                // without permission exit is the only way
                                finish()
                            }
                        })
                        .show()
                return
            }

            // Create a new EmpaDeviceManager. MainActivity is both its data and status delegate.
            deviceManager = EmpaDeviceManager(getApplicationContext(), this, this)

            // Initialize the Device Manager using your API key. You need to have Internet access at this point.
            deviceManager.authenticateWithAPIKey(EMPATICA_API_KEY)
        }
    }

    @Override
    protected fun onPause() {
        super.onPause()
    }

    @Override
    protected fun onDestroy() {
        super.onDestroy()
        if (deviceManager != null) {
            deviceManager.cleanUp()
        }
    }

    @Override
    protected fun onStop() {
        super.onStop()
        if (deviceManager != null) {
            deviceManager.stopScanning()
        }
    }

    @Override
    fun didDiscoverDevice(bluetoothDevice: EmpaticaDevice?, deviceName: String, rssi: Int, allowed: Boolean) {
        // Check if the discovered device can be used with your API key. If allowed is always false,
        // the device is not linked with your API key. Please check your developer area at
        // https://www.empatica.com/connect/developer.php
        Log.i(TAG, "didDiscoverDevice" + deviceName + "allowed: " + allowed)
        if (allowed) {
            // Stop scanning. The first allowed device will do.
            deviceManager.stopScanning()
            try {
                // Connect to the device
                deviceManager.connectDevice(bluetoothDevice)
                updateLabel(deviceNameLabel, "To: $deviceName")
            } catch (e: ConnectionNotAllowedException) {
                // This should happen only if you try to connect when allowed == false.
                Toast.makeText(this@MainActivity, "Sorry, you can't connect to this device", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "didDiscoverDevice" + deviceName + "allowed: " + allowed + " - ConnectionNotAllowedException", e)
            }
        }
    }

    @Override
    fun didFailedScanning(errorCode: Int) {

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

    @Override
    fun didRequestEnableBluetooth() {
        // Request the user to enable Bluetooth
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    @Override
    fun bluetoothStateChanged() {
        // E4link detected a bluetooth adapter change
        // Check bluetooth adapter and update your UI accordingly.
        val isBluetoothOn: Boolean = BluetoothAdapter.getDefaultAdapter().isEnabled()
        Log.i(TAG, "Bluetooth State Changed: $isBluetoothOn")
    }

    @Override
    protected fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // The user chose not to enable Bluetooth
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            // You should deal with this
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @Override
    fun didUpdateSensorStatus(@EmpaSensorStatus status: Int, type: EmpaSensorType?) {
        didUpdateOnWristStatus(status)
    }

    @Override
    fun didUpdateStatus(status: EmpaStatus) {
        // Update the UI
        updateLabel(statusLabel, status.name())

        // The device manager is ready for use
        if (status === EmpaStatus.READY) {
            updateLabel(statusLabel, status.name().toString() + " - Turn on your device")
            // Start scanning
            deviceManager.startScanning()
            // The device manager has established a connection
            hide()
        } else if (status === EmpaStatus.CONNECTED) {
            show()
            // The device manager disconnected from a device
        } else if (status === EmpaStatus.DISCONNECTED) {
            updateLabel(deviceNameLabel, "")
            hide()
        }
    }

    @Override
    fun didReceiveAcceleration(x: Int, y: Int, z: Int, timestamp: Double) {
        updateLabel(accel_xLabel, "" + x)
        updateLabel(accel_yLabel, "" + y)
        updateLabel(accel_zLabel, "" + z)
    }

    @Override
    fun didReceiveBVP(bvp: Float, timestamp: Double) {
        updateLabel(bvpLabel, "" + bvp)
    }

    @Override
    fun didReceiveBatteryLevel(battery: Float, timestamp: Double) {
        updateLabel(batteryLabel, String.format("%.0f %%", battery * 100))
    }

    @Override
    fun didReceiveGSR(gsr: Float, timestamp: Double) {
        updateLabel(edaLabel, "" + gsr)
    }

    @Override
    fun didReceiveIBI(ibi: Float, timestamp: Double) {
        updateLabel(ibiLabel, "" + ibi)
    }

    @Override
    fun didReceiveTemperature(temp: Float, timestamp: Double) {
        updateLabel(temperatureLabel, "" + temp)
    }

    // Update a label with some text, making sure this is run in the UI thread
    private fun updateLabel(label: TextView?, text: String) {
        runOnUiThread(object : Runnable() {
            @Override
            fun run() {
                label.setText(text)
            }
        })
    }

    @Override
    fun didReceiveTag(timestamp: Double) {
    }

    @Override
    fun didEstablishConnection() {
        show()
    }

    @Override
    fun didUpdateOnWristStatus(@EmpaSensorStatus status: Int) {
        runOnUiThread(object : Runnable() {
            @Override
            fun run() {
                if (status == EmpaSensorStatus.ON_WRIST) {
                    (findViewById(R.id.wrist_status_label) as TextView).setText("ON WRIST")
                } else {
                    (findViewById(R.id.wrist_status_label) as TextView).setText("NOT ON WRIST")
                }
            }
        })
    }

    fun show() {
        runOnUiThread(object : Runnable() {
            @Override
            fun run() {
                dataCnt.setVisibility(View.VISIBLE)
            }
        })
    }

    fun hide() {
        runOnUiThread(object : Runnable() {
            @Override
            fun run() {
                dataCnt.setVisibility(View.INVISIBLE)
            }
        })
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_ENABLE_BT = 1
        private const val REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1
        private const val EMPATICA_API_KEY = "" // TODO insert your API Key here
    }
}