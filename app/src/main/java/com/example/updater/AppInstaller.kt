package com.example.updater

import android.app.DownloadManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream

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
     * دالة التثبيت الرئيسية:
     * - تحاول التثبيت في الخلفية (Silent Update) بدون تدخل المستخدم على Android 12 (API 31)+
     * - ترجع تلقائياً للأسلوب التقليدي (Fallback via FileProvider) على الإصدارات الأقدم أو عند الحاجة
     */
    fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists() || apkFile.length() == 0L) {
            Toast.makeText(context, "ملف التحديث غير موجود أو غير صالح", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. إذا كان الجهاز يعمل على Android 12 (API 31) فما فوق، نستخدم PackageInstaller مع USER_ACTION_NOT_REQUIRED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val success = installPackageViaSession(context, apkFile)
            if (success) {
                Toast.makeText(context, "جاري تثبيت التحديث في الخلفية...", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // 2. الرجوع التلقائي (Fallback) للأسلوب التقليدي عبر FileProvider للإصدارات الأقدم من Android 12
        installLegacyViaFileProvider(context, apkFile)
    }

    /**
     * تثبيت التحديث عبر PackageInstaller.Session مع USER_ACTION_NOT_REQUIRED (Android 12+)
     */
    private fun installPackageViaSession(context: Context, apkFile: File): Boolean {
        var session: PackageInstaller.Session? = null
        return try {
            val packageInstaller = context.packageManager.packageInstaller
            val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
                setAppPackageName(context.packageName)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    setPackageSource(PackageInstaller.PACKAGE_SOURCE_OTHER)
                }
            }

            val sessionId = packageInstaller.createSession(params)
            session = packageInstaller.openSession(sessionId)

            val inputStream: InputStream = FileInputStream(apkFile)
            val outputStream: OutputStream = session.openWrite("package_session", 0, apkFile.length())

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                    session.fsync(output)
                }
            }

            // إعداد Intent للـ BroadcastReceiver لاستقبال النتيجة
            val receiverIntent = Intent(context, InstallStatusReceiver::class.java).apply {
                action = InstallStatusReceiver.ACTION_INSTALL_STATUS
            }

            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                receiverIntent,
                flags
            )

            session.commit(pendingIntent.intentSender)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            session?.abandon()
            false
        } finally {
            session?.close()
        }
    }

    /**
     * الأسلوب التقليدي للتثبيت عبر FileProvider (Android 11 فما دون)
     */
    private fun installLegacyViaFileProvider(context: Context, apkFile: File) {
        try {
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
            setDescription("جاري تنزيل وتثبيت التحديث...")
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
