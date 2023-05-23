package blog.android.examples.util.wifi

enum class WifiSecurityType {
    OPEN,       // 无密码
    WEP,        // WEP密码
    WPA,        // WPA密码
    WPA2,       // WPA2密码
    WPA_WPA2    // 同时支持WPA和WPA2密码
}
