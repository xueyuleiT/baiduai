package com.baidu.aip.asrwakeup3.util

import androidx.appcompat.app.AppCompatActivity
import com.hjq.permissions.OnPermission
import com.hjq.permissions.XXPermissions

class PermissionRequestUtil {
    companion object {
        fun requestPermission(
                activity: AppCompatActivity,
                permissions: List<String>,
                grant: ((List<String>) -> Unit),
                denied: ((List<String>) -> Unit)
        ) {

            XXPermissions.with(activity)
                    .permission(permissions) //不指定权限则自动获取清单中的危险权限
                    .request(object : OnPermission {
                        override fun hasPermission(
                                list: List<String>,
                                isAll: Boolean
                        ) {
                            grant.invoke(list)
                        }

                        override fun noPermission(
                                denied: List<String>,
                                quick: Boolean
                        ) {
                            denied(denied)
                        }
                    })
        }

    }
}