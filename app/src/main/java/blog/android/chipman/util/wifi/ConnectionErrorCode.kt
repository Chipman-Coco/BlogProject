package blog.android.chipman.util.wifi

/**
 * 尝试连接到wifi网络时可能发生的错误
 */
enum class ConnectionErrorCode {
    /**
     * 从Android 10开始，应用程序不再允许启用wifi，用户必须手动执行此操作
     */
    COULD_NOT_ENABLE_WIFI,

    /**
     * 从Android 9开始，在前台应用程序中每2分钟只允许扫描4次
     */
    COULD_NOT_SCAN,

    /**
     * 如果wifi网络不在范围内，则安全类型未知，WifiUtils将不支持连接到网络
     */
    DID_NOT_FIND_NETWORK_BY_SCANNING,

    /**
     * 尝试连接时发生身份验证错误
     * 密码可能不正确
     */
    AUTHENTICATION_ERROR_OCCURRED,

    /**
     * 无法在超时窗口中连接
     */
    TIMEOUT_OCCURRED,

    /**
     * 部分机型连接异常
     */
    ANDROID_10_IMMEDIATELY_DROPPED_CONNECTION,

    /**
     * 从Android 10，用户必须确认连接，但也可以取消
     */
    USER_CANCELLED,

    COULD_NOT_CONNECT
}