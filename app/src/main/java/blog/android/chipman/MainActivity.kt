package blog.android.chipman

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) { isGranted ->
            if (isGranted) {
                refreshData()
            } else {
                requestLocationPermission()
            }
        }
    }

    private val locationRequestPermissionLauncher =
        registerForActivityResult(RequestPermission()) { isGranted ->
            Log.d(TAG, "locationRequestPermissionLauncher: isGranted = $isGranted")
            if (isGranted) {
                refreshData()
            } else {
                showRequestPermissionRationale()
            }
        }

    /**
     * 刷新数据
     */
    private fun refreshData() {

    }

    /**
     * 显示请求权限的理由，并引导用户手动开启权限
     */
    private fun showRequestPermissionRationale() {

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

}