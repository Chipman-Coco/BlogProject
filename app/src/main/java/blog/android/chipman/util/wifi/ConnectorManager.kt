package blog.android.chipman.util.wifi

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.PatternMatcher
import androidx.annotation.RequiresApi
import blog.android.chipman.App

object ConnectorManager {

    private val mContext: Context by lazy { App.instance }

    private val connectivityManager: ConnectivityManager by lazy {
        mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    /**
     * 连接到特定的Wi-Fi网络.
     */
    fun connectToNetwork(
        wifiManager: WifiManager,
        ssid: String,
        securityType: WifiSecurityType,
        password: String
    ): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectToNetworkAndroidQ(wifiManager, ssid)
        } else {
            val wifiConfig = createWifiConfiguration(ssid, password, securityType)
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
    fun disconnect(wifiManager: WifiManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (networkCallback != null) {
                connectivityManager.unregisterNetworkCallback(networkCallback!!)
            }
        } else {
            wifiManager.disconnect()
        }
    }

    /**
     * 连接到Android Q（API 29级）及以上版本上的特定Wi-Fi网络.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectToNetworkAndroidQ(wifiManager: WifiManager, ssid: String): Boolean {
        val networkSpecifier = WifiNetworkSpecifier.Builder()
            .setSsidPattern(PatternMatcher(ssid, PatternMatcher.PATTERN_PREFIX))
            .build()

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(networkSpecifier)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)   // 网络不受限
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                connectivityManager.bindProcessToNetwork(network)   // 绑定当前进程到此网络
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                disconnect(wifiManager)
            }

            override fun onUnavailable() {
                super.onUnavailable()
            }
        }

        connectivityManager.requestNetwork(networkRequest, networkCallback!!)

        return true
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