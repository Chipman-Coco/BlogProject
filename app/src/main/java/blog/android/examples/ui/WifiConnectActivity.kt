package blog.android.examples.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import blog.android.examples.R
import blog.android.examples.databinding.ActivityWifiConnectionBinding
import blog.android.examples.util.WifiUtils

class WifiConnectActivity : AppCompatActivity() {

    private val TAG = WifiConnectActivity::class.java.simpleName
    private lateinit var mViewBinding: ActivityWifiConnectionBinding
    private lateinit var mAdapter: WifiAdapter

    private val locationRequestPermissionLauncher =
        registerForActivityResult(RequestPermission()) { isGranted ->
            Log.d(TAG, "locationRequestPermissionLauncher: isGranted = $isGranted")
            if (isGranted) {
                WifiUtils.scanWifiNetworks()
            } else {
                showRequestPermissionRationale()
            }
        }

    private val mWifiReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.M)
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                // WiFi状态改变
                WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN
                    )
                    Log.d(TAG, "onReceive: wifi state = $state")
                    if (state == WifiManager.WIFI_STATE_ENABLED) {
                        WifiUtils.scanWifiNetworks()
                    }
                }
                // 扫描结果
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION -> {
                    val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                    Log.d(TAG, "onReceive: wifi scan results state: $success")
                    refreshWifiList()
//                    if (success) {
//
//                    } else {
//                        Toast.makeText(this@WifiConnectActivity, "扫描失败", Toast.LENGTH_SHORT).show()
//                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding = ActivityWifiConnectionBinding.inflate(layoutInflater)
        setContentView(mViewBinding.root)

        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) { isGranted ->
            if (isGranted) {
                WifiUtils.scanWifiNetworks()
            } else {
                requestLocationPermission()
            }
        }

        initView()
        registerWifiReceive()
    }

    private fun initView() {
        mAdapter = WifiAdapter { position ->
            connectNetwork(position)
        }

        with(mViewBinding) {
            btScan.setOnClickListener {
                WifiUtils.scanWifiNetworks()
            }
            wifiList.apply {
                adapter = mAdapter
                layoutManager = LinearLayoutManager(context)
                addItemDecoration(
                    DividerItemDecoration(
                        this@WifiConnectActivity,
                        LinearLayoutManager.VERTICAL
                    ).apply {
                        ActivityCompat.getDrawable(
                            this@WifiConnectActivity,
                            R.drawable.shape_custom_item_divider
                        )?.let {
                            setDrawable(it)
                        }
                    }
                )
            }
        }
    }

    /**
     * 连接热点
     */
    private fun connectNetwork(position: Int) {
        val scanResult = mAdapter.dataList[position]
        WifiUtils.connectToNetwork(scanResult, "")
    }

    /**
     * 刷新WiFi列表
     */
    @SuppressLint("MissingPermission")
    private fun refreshWifiList() {
        val filterResults = WifiUtils.getScanResults()
            .filter { it.SSID.isNotEmpty() }
            .distinctBy { it.SSID }
        Log.d(TAG, "refreshWifiList: scanResults size = ${filterResults.size}")
        mAdapter.updateData(filterResults)
    }

    /**
     * 显示请求权限的理由，并引导用户手动开启权限
     */
    private fun showRequestPermissionRationale() {
        val dialog = TipsDialogFragment.builder(this)
            .setMessage("未同意定位权限，请前往设置中手动同意权限")
            .setTitle("警告")
            .setPositiveButton("前往") { _, _ ->
                openLocationSetting()
            }
            .create()
        dialog.show(supportFragmentManager, "TipsAlertDialog")
    }

    /**
     * 检查权限
     */
    private fun checkPermission(
        permission: String,
        action: (Boolean) -> Unit
    ) {
        action.invoke(
            ActivityCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    /**
     * 申请定位权限
     */
    private fun requestLocationPermission() {
        locationRequestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    /**
     * 注册广播
     */
    private fun registerWifiReceive() {
        val intentFilter = IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)         // wifi开启关闭状态
            addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)     // 监听wifi列表变化（开启一个热点或者关闭一个热点）
        }
        registerReceiver(mWifiReceiver, intentFilter)
    }

    private fun openLocationSetting() {
        startActivity(Intent(Settings.ACTION_SETTINGS))
    }

    override fun onDestroy() {
        super.onDestroy()
//        WifiUtils.disconnect()
    }

}