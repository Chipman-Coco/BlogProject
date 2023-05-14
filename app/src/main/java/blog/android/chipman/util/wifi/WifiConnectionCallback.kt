package blog.android.chipman.util.wifi

interface WifiConnectionCallback {

    fun onConnectSuccess()

    fun onConnectError(errorCode: ConnectionErrorCode)
}