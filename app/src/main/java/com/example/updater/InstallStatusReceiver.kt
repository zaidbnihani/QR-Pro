package com.example.updater

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.widget.Toast

/**
 * BroadcastReceiver لاستقبال نتيجة التثبيت بالخلفية عبر PackageInstaller
 */
class InstallStatusReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                // في حال تطلب النظام تأكيداً من المستخدم (Fallback تلقائي)
                val confirmationIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_INTENT)
                }

                confirmationIntent?.let {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(it)
                }
            }

            PackageInstaller.STATUS_SUCCESS -> {
                Toast.makeText(context, "تم تثبيت التحديث الجديد بنجاح!", Toast.LENGTH_LONG).show()
            }

            PackageInstaller.STATUS_FAILURE,
            PackageInstaller.STATUS_FAILURE_ABORTED,
            PackageInstaller.STATUS_FAILURE_BLOCKED,
            PackageInstaller.STATUS_FAILURE_CONFLICT,
            PackageInstaller.STATUS_FAILURE_INCOMPATIBLE,
            PackageInstaller.STATUS_FAILURE_INVALID,
            PackageInstaller.STATUS_FAILURE_STORAGE -> {
                Toast.makeText(
                    context,
                    "فشل التثبيت التلقائي: ${message ?: "رمز الخطأ $status"}",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {
                // حالة غير معروفة أو قيد المعالجة
            }
        }
    }

    companion object {
        const val ACTION_INSTALL_STATUS = "com.example.updater.INSTALL_STATUS"
    }
}
