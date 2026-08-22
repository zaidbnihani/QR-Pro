package com.example.updater

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

/**
 * فاحص التحديثات التلقائي - ينزل الـ APK مباشرة ويثبته داخل التطبيق بدون متصفح
 */
@Composable
fun AutoUpdateChecker(currentVersionName: String) {
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var downloadStatus by remember { mutableStateOf<DownloadStatus>(DownloadStatus.Idle) }
    var downloadProgress by remember { mutableStateOf(0) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val result = UpdateChecker.checkForUpdates(currentVersionName)
        updateInfo = result
    }

    updateInfo?.let { info ->
        AlertDialog(
            onDismissRequest = {
                if (downloadStatus !is DownloadStatus.Downloading) {
                    updateInfo = null
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color(0xFF141923),
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFF00E5FF).copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (downloadStatus) {
                            is DownloadStatus.NeedPermission -> Icons.Default.Security
                            is DownloadStatus.Downloaded -> Icons.Default.InstallMobile
                            else -> Icons.Default.NewReleases
                        },
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(30.dp)
                    )
                }
            },
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (downloadStatus) {
                            is DownloadStatus.Downloading -> "جاري تنزيل التحديث..."
                            is DownloadStatus.NeedPermission -> "مطلوب إذن التثبيت"
                            is DownloadStatus.Downloaded -> "اكتمل التنزيل!"
                            else -> "يتوفر تحديث جديد!"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Surface(
                        color = Color(0xFF00E5FF).copy(alpha = 0.18f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "الإصدار ${info.latestVersion}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (val status = downloadStatus) {
                        is DownloadStatus.Downloading -> {
                            Text(
                                text = "يتم تنزيل حزمة التحديث مباشرة لتثبيتها...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFA0AEC0)
                            )
                            LinearProgressIndicator(
                                progress = { downloadProgress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = Color(0xFF00E5FF),
                                trackColor = Color(0xFF1E293B)
                            )
                            Text(
                                text = "$downloadProgress%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        is DownloadStatus.NeedPermission -> {
                            Text(
                                text = "لتثبيت التحديث مباشرة، يرجى تفعيل خيار 'السماح بتثبيت التطبيقات غير المعروفة' للتطبيق من شاشة الإعدادات ثم الضغط على تثبيت.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFCBD5E1)
                            )
                        }

                        is DownloadStatus.Error -> {
                            Text(
                                text = "حدث خطأ أثناء التنزيل: ${status.message}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFFF5252)
                            )
                        }

                        else -> {
                            Text(
                                text = "ملاحظات التحديث:",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFA0AEC0)
                            )
                            Surface(
                                color = Color(0xFF1A2230),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = info.releaseNotes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE2E8F0),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                when (val status = downloadStatus) {
                    is DownloadStatus.Idle, is DownloadStatus.Error -> {
                        Button(
                            onClick = {
                                AppInstaller.downloadAndInstall(
                                    context = context,
                                    downloadUrl = info.downloadUrl,
                                    version = info.latestVersion.removePrefix("v"),
                                    onProgress = { downloadProgress = it },
                                    onStatusChange = { downloadStatus = it }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E5FF),
                                contentColor = Color(0xFF06080E)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تنزيل وتثبيت الآن", fontWeight = FontWeight.Bold)
                        }
                    }

                    is DownloadStatus.Downloading -> {
                        // لا شيء أثناء التنزيل لمنع تكرار التحميل
                    }

                    is DownloadStatus.NeedPermission -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    AppInstaller.openInstallPermissionSetting(context)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00E5FF),
                                    contentColor = Color(0xFF06080E)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("فتح الإعدادات", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    AppInstaller.installApk(context, status.file)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("تثبيت الآن", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    is DownloadStatus.Downloaded -> {
                        Button(
                            onClick = {
                                AppInstaller.installApk(context, status.file)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E5FF),
                                contentColor = Color(0xFF06080E)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.InstallMobile,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تثبيت التحديث", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            dismissButton = {
                if (downloadStatus !is DownloadStatus.Downloading) {
                    TextButton(
                        onClick = { updateInfo = null },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("لاحقاً", color = Color(0xFFA0AEC0))
                    }
                }
            }
        )
    }
}
