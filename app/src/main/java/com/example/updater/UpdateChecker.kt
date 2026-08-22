package com.example.updater

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface GitHubService {
    @Headers("User-Agent: QR-Pro-Android-App")
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GitHubRelease
}

object UpdateChecker {

    private const val GITHUB_OWNER = "zaidbnihani"
    private const val GITHUB_REPO = "QR-Pro"

    private val api: GitHubService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubService::class.java)
    }

    /**
     * تفحص الإصدار بشكل غير متزامن وتتجاهل أي أخطاء بهدوء
     */
    suspend fun checkForUpdates(currentVersionName: String): UpdateInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val release = api.getLatestRelease(GITHUB_OWNER, GITHUB_REPO)
                val remoteVersion = release.tagName.removePrefix("v").trim()
                val currentVersion = currentVersionName.removePrefix("v").trim()

                if (isNewerVersion(remoteVersion, currentVersion)) {
                    // البحث عن أول ملف بصيغة .apk في المرفقات
                    val apkDownloadUrl = release.assets
                        ?.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
                        ?.browserDownloadUrl
                        ?: release.htmlUrl // رابط صفحة الـ Release كحل بديل

                    UpdateInfo(
                        latestVersion = release.tagName,
                        releaseNotes = release.body?.takeIf { it.isNotBlank() } ?: "تحسينات عامة وإصلاحات وتحديثات جديدة.",
                        downloadUrl = apkDownloadUrl
                    )
                } else {
                    null // التطبيق محدث لآخر إصدار
                }
            } catch (e: Exception) {
                // تجاهل بصمت عند عدم وجود إنترنت أو خطأ في الـ API
                Log.d("UpdateChecker", "Check skipped or network unavailable: ${e.message}")
                null
            }
        }
    }

    /**
     * مقارنة Semantic Versioning دقيقة (Major.Minor.Patch)
     * تُرجع true إذا كانت remoteVersion أحدث من currentVersion
     */
    private fun isNewerVersion(remote: String, current: String): Boolean {
        return try {
            val remoteParts = remote.split("-")[0].split(".").map { it.toIntOrNull() ?: 0 }
            val currentParts = current.split("-")[0].split(".").map { it.toIntOrNull() ?: 0 }

            val maxLength = maxOf(remoteParts.size, currentParts.size)
            for (i in 0 until maxLength) {
                val r = remoteParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
            false
        } catch (e: Exception) {
            false
        }
    }
}
