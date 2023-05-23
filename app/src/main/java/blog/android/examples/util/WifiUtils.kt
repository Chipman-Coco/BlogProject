package blog.android.examples.util

import android.Manifest.permission
import android.content.Context
import android.location.LocationManager
import android.net.*
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.PatternMatcher
import android.text.format.Formatter
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import blog.android.examples.App
import blog.android.examples.util.wifi.ConnectionErrorCode
import blog.android.examples.util.wifi.WifiConnectionCallback
import blog.android.examples.util.wifi.WifiSecurityType

/**
 * WiFi 相关的工具类
 */
object WifiUtils {

    private val TAG = WifiUtils::class.java.simpleName
    private val mContext: Context by lazy { App.instance }

    private val wifiManager: WifiManager by lazy {
        mContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private val connectivityManager: ConnectivityManager by lazy {
        mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val locationManager: LocationManager by lazy {
        mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var mConnectionCallback: WifiConnectionCallback? = null

    /**
     * 检查是否已启用Wi-Fi.
     */
    fun isWifiEnabled(): Boolean {
        return wifiManager.isWifiEnabled
    }

    /**
     * 启用或禁用Wi-Fi.
     */
    fun setWifiEnabled(enabled: Boolean) {
        wifiManager.isWifiEnabled = enabled
    }

    fun setConnectionCallback(callback: WifiConnectionCallback?) {
        this.mConnectionCallback = callback
    }

    /**
     * 连接到特定的Wi-Fi网络.
     */
    fun connectToNetwork(
        scanResult: ScanResult,
        password: String
    ): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectToNetworkAndroidQ(scanResult.SSID, password)
        } else {
            val wifiConfig = createWifiConfiguration(
                scanResult.SSID,
                password,
                getSecurity(scanResult.capabilities)
            )
            val networkId = wifiManager.addNetwork(wifiConfig)
            if (networkId != -1) {
                wifiManager.enableNetwork(networkId, true)
            } else {
                false
            }
        }
    }

    /**
     * 断开与当前连接的Wi-Fi网络的连接.
     */
    fun disconnect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (networkCallback != null) {
                connectivityManager.unregisterNetworkCallback(networkCallback!!)
            }
        } else {
            wifiManager.disconnect()
        }
    }

    /**
     * 获取当前连接的Wi-Fi网络的SSID.
     */
    fun getConnectedSSID(): String? {
        val connectionInfo = wifiManager.connectionInfo
        if (connectionInfo != null && !connectionInfo.ssid.isNullOrEmpty()) {
            return connectionInfo.ssid.replace("\"", "")
        }
        return null
    }

    /**
     * 删除已配置的Wi-Fi网络.
     */
    fun removeNetwork(networkId: Int): Boolean {
        return wifiManager.removeNetwork(networkId)
    }

    /**
     * 获取配置的Wi-Fi网络.
     */
    @RequiresPermission(allOf = [permission.ACCESS_FINE_LOCATION, permission.ACCESS_WIFI_STATE])
    fun getConfiguredNetworks(): List<WifiConfiguration> {
        return wifiManager.configuredNetworks ?: emptyList()
    }

    /**
     * 扫描可用的Wi-Fi网络.
     */
    fun scanWifiNetworks() {
        wifiManager.startScan()
    }

    /**
     * 获取可用Wi-Fi网络的列表.
     */
    @RequiresPermission(allOf = [permission.ACCESS_FINE_LOCATION, permission.ACCESS_WIFI_STATE])
    fun getScanResults(): List<ScanResult> {
        return wifiManager.scanResults
    }

    /**
     * 获取 Wi-Fi 路由器的 IP 地址
     */
    fun getWifiRouterIPAddress(): String {
        val dhcpInfo: DhcpInfo = wifiManager.dhcpInfo
        return Formatter.formatIpAddress(dhcpInfo.gateway)
    }

    /**
     * 检查位置服务是否启用（Android Q 及以上需要位置权限）
     */
    fun isLocationServiceEnabled(): Boolean {
        val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return gps || network
    }

    /**
     * 检查设备当前是否连接到Wi-Fi网络.
     */
    fun isConnected(): Boolean {
        val networkInfo = wifiManager.connectionInfo
        return networkInfo != null && networkInfo.networkId != -1
    }

    /**
     * 获取当前的Wi-Fi信号强度，单位为dBm.
     */
    fun getSignalStrength(): Int {
        val networkInfo = wifiManager.connectionInfo
        return networkInfo?.rssi ?: 0
    }

    /**
     * 连接到Android Q（API 29级）及以上版本上的特定Wi-Fi网络.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectToNetworkAndroidQ(
        ssid: String,
        password: String,
        bssid: String? = null
    ): Boolean {
        val networkSpecifier = WifiNetworkSpecifier.Builder().apply {
            setWpa2Passphrase(password)
            if (bssid == null) {
                setSsidPattern(PatternMatcher(ssid, PatternMatcher.PATTERN_PREFIX))
            } else {
                setSsid(ssid)
                setBssid(MacAddress.fromString(ssid))
            }
        }.build()

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(networkSpecifier)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)   // 网络不受限
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                connectivityManager.bindProcessToNetwork(network)   // 绑定当前进程到此网络
                mConnectionCallback?.onConnectSuccess()
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                disconnect()
            }

            override fun onUnavailable() {
                super.onUnavailable()
                mConnectionCallback?.onConnectError(ConnectionErrorCode.USER_CANCELLED)
            }
        }

        connectivityManager.requestNetwork(networkRequest, networkCallback!!)
        return true
    }

    /**
     * 获取网络安全类型
     */
    private fun getSecurity(capabilities: String): WifiSecurityType {
        var security = WifiSecurityType.OPEN
        if (capabilities.contains(WifiSecurityType.WEP.name)) {
            security = WifiSecurityType.WEP
        }
        if (capabilities.contains(WifiSecurityType.WPA.name)) {
            security = WifiSecurityType.WPA
        }
        if (capabilities.contains(WifiSecurityType.WPA2.name)) {
            security = WifiSecurityType.WPA2
        }
        if (capabilities.contains(WifiSecurityType.WPA_WPA2.name)) {
            security = WifiSecurityType.WPA_WPA2
        }
        Log.d(TAG, "getSecurity: security = $security")
        return security
    }

    /**
     * 为具有给定SSID、密码和安全类型的网络创建Wi-Fi配置.
     */
    private fun createWifiConfiguration(
        ssid: String,
        password: String,
        securityType: WifiSecurityType
    ): WifiConfiguration {
        val wifiConfig = WifiConfiguration()
        wifiConfig.SSID = "\"$ssid\""

        when (securityType) {
            WifiSecurityType.OPEN -> {
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            }
            WifiSecurityType.WEP -> {
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
                val wepKey = "\"$password\""
                wifiConfig.wepKeys[0] = wepKey
                wifiConfig.wepTxKeyIndex = 0
            }
            WifiSecurityType.WPA -> {
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                wifiConfig.preSharedKey = "\"$password\""
            }
            WifiSecurityType.WPA2 -> {
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                wifiConfig.preSharedKey = "\"$password\""
            }
            WifiSecurityType.WPA_WPA2 -> {
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                wifiConfig.preSharedKey = "\"$password\""
            }
        }
        return wifiConfig
    }

}
