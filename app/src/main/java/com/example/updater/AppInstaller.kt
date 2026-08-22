package com.example.updater

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.*
import java.io.File

object AppInstaller {

    /**
     * التحقق مما إذا كان التطبيق يملك صلاحية تثبيت التطبيقات من مصادر غير معروفة
     */
    fun canRequestPackageInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * توجيه المستخدم إلى شاشة إعدادات السماح بالتثبيت
     */
    fun openInstallPermissionSetting(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    /**
     * تشغيل شاشة تثبيت الـ APK عبر FileProvider
     */
    fun installApk(context: Context, apkFile: File) {
        try {
            if (!apkFile.exists()) {
                Toast.makeText(context, "ملف التحديث غير موجود", Toast.LENGTH_SHORT).show()
                return
            }

            val authority = "${context.packageName}.fileprovider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, apkFile)

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "تعذر فتح شاشة التثبيت: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * بدء تنزيل ملف الـ APK عبر DownloadManager ومتابعة النسبة المئوية
     */
    fun downloadAndInstall(
        context: Context,
        downloadUrl: String,
        version: String,
        onProgress: (Int) -> Unit,
        onStatusChange: (DownloadStatus) -> Unit
    ): Job {
        val scope = CoroutineScope(Dispatchers.Main + Job())

        val fileName = "QR-Pro-$version.apk"
        val destinationDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.filesDir
        val apkFile = File(destinationDir, fileName)

        // إذا كان الملف موجوداً مسبقاً، نحذفه لضمان تنزيل نظيف
        if (apkFile.exists()) {
            apkFile.delete()
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setTitle("تحديث QR Pro $version")
            setDescription("جاري تنزيل ملف التحديث...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationUri(Uri.fromFile(apkFile))
            setMimeType("application/vnd.android.package-archive")
        }

        val downloadId = try {
            downloadManager.enqueue(request)
        } catch (e: Exception) {
            onStatusChange(DownloadStatus.Error(e.localizedMessage ?: "فشل بدء التنزيل"))
            return scope.launch { }
        }

        onStatusChange(DownloadStatus.Downloading(0))

        // الاستماع لاكتمال التنزيل عبر BroadcastReceiver
        val onCompleteReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
                if (id == downloadId) {
                    try {
                        context.unregisterReceiver(this)
                    } catch (_: Exception) {}

                    if (apkFile.exists() && apkFile.length() > 0) {
                        onStatusChange(DownloadStatus.Downloaded(apkFile))
                        if (canRequestPackageInstalls(context)) {
                            installApk(context, apkFile)
                        } else {
                            onStatusChange(DownloadStatus.NeedPermission(apkFile))
                        }
                    } else {
                        onStatusChange(DownloadStatus.Error("الملف المنزّل غير صالح"))
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                onCompleteReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED
            )
        } else {
            context.registerReceiver(
                onCompleteReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }

        // Coroutine لمتابعة تقدم نسبة التحميل
        return scope.launch(Dispatchers.IO) {
            var downloading = true
            while (downloading) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)
                if (cursor != null && cursor.moveToFirst()) {
                    val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)

                    val bytesDownloaded = if (bytesDownloadedIndex >= 0) cursor.getLong(bytesDownloadedIndex) else 0L
                    val bytesTotal = if (bytesTotalIndex >= 0) cursor.getLong(bytesTotalIndex) else 0L
                    val status = if (statusIndex >= 0) cursor.getInt(statusIndex) else -1

                    cursor.close()

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false
                        withContext(Dispatchers.Main) {
                            onProgress(100)
                        }
                        break
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        downloading = false
                        withContext(Dispatchers.Main) {
                            onStatusChange(DownloadStatus.Error("فشل التنزيل من الخادم"))
                        }
                        break
                    }

                    if (bytesTotal > 0) {
                        val progress = ((bytesDownloaded * 100L) / bytesTotal).toInt()
                        withContext(Dispatchers.Main) {
                            onProgress(progress)
                            onStatusChange(DownloadStatus.Downloading(progress))
                        }
                    }
                } else {
                    cursor?.close()
                }
                delay(350)
            }
        }
    }
}

sealed class DownloadStatus {
    object Idle : DownloadStatus()
    data class Downloading(val progress: Int) : DownloadStatus()
    data class Downloaded(val file: File) : DownloadStatus()
    data class NeedPermission(val file: File) : DownloadStatus()
    data class Error(val message: String) : DownloadStatus()
}
