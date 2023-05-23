package blog.android.examples.util.wifi

interface WifiConnectionCallback {

    fun onConnectSuccess()

    fun onConnectError(errorCode: ConnectionErrorCode)
}