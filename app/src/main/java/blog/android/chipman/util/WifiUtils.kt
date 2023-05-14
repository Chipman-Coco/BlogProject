package blog.android.chipman.util

import android.Manifest.permission
import android.content.Context
import android.location.LocationManager
import android.net.DhcpInfo
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.text.format.Formatter
import androidx.annotation.RequiresPermission
import blog.android.chipman.App
import blog.android.chipman.util.wifi.ConnectorManager

/**
 * WiFi 相关的工具类
 */
object WifiUtils {

    private val mContext: Context by lazy { App.instance }

    private val wifiManager: WifiManager by lazy {
        mContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private val locationManager: LocationManager by lazy {
        mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

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

    /**
     * 断开与当前连接的Wi-Fi网络的连接.
     */
    fun disconnect() {
        ConnectorManager.disconnect(wifiManager)
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
}
